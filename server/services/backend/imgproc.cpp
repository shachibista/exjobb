#include <iostream>
#include <cmath>
#include <vector>
#include <queue>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/imgproc_c.h>

// #define DEBUG true // flag to turn on debugging
#define EPSILON 1e-5

using namespace cv;
using namespace std;

/*
 * Helper to initialise and show windows.
 * Not necessary but tremendously useful in a tiled-window setting
 */
void imshow(string name, Mat img){
	namedWindow(name, WINDOW_NORMAL);
	cv::imshow(name, img);
}

/*
 * Enhances image contrast using the CLAHE algorithm
 */
Mat enhanceContrast(Mat img){
	// we convert to L*a*b colour space since
	// RGB isn't well suited for colour operations
	Mat labImage;
	cvtColor(img, labImage, COLOR_BGR2Lab);

	vector<Mat> lab;
	split(labImage, lab);

	Ptr<CLAHE> clahe = createCLAHE();
	clahe->setClipLimit(4); // we set a clip-limit of 4, arbitrary

	Mat lEnh;
	clahe->apply(lab[0], lEnh);

	vector<Mat> enhLabComp;
	enhLabComp.push_back(lEnh);
	enhLabComp.push_back(lab[1]);
	enhLabComp.push_back(lab[2]);
	
	// combine and convert back to RGB
	Mat enhLab;
	merge(enhLabComp, enhLab);
	cvtColor(enhLab, enhLab, COLOR_Lab2BGR);

	return enhLab;
}

/*
 * Finds the circular border mask at the edge of the images.
 *
 * 1) Projects the image onto a log-polar coordinate. This makes circular
 *		regions planar
 *
 * 2) Determines a "crop-area", by thresholding pixels under a certain 
 *		value using the 3-channel pixel average.
 *
 * 3) Selects the area within the crop-area and re-projects it onto 
 *		cartesian coordinates (this causes some artefacts due to 
 *		interpolation)
 *
 * 4) Applies a threshold to mask out the valid pixels
 *
 */
Mat findBorderMask(Mat img){
	Size s = img.size();
	int w = s.width;
	int h = s.height;
	int rB = 1;
	int R = 120;
	
	Point2i center(w >> 1, h >> 1);
	
	// project the image with center at img.w/2 and img.h/2
	LogPolar_Interp nearest(w, h, center, R, rB);
	Mat cortical = nearest.to_cortical(img);
	
	// calculate RGB pixel average
	cvtColor(cortical, cortical, COLOR_BGR2GRAY);
	
	// calculate crop-area
	// the log-polar projection (this particular implementation)
	// pushes the edge pixels to the right side.
	//
	// We start counting the number of dark pixels from the right edge
	// for every row. And set a crop-threshold at the row with the greatest
	// number of dark-pixels from the right edge.
	int maxCrop = 0;
	for(int j = 0;j < cortical.rows;j++){
		int crop = 0;
		int l = (cortical.cols >> 1);
		for(int i = cortical.cols;i >= l;i--){
			if(cortical.at<uchar>(j, i) == 0) crop++;
		}
		if(crop > maxCrop) maxCrop = crop;
	}
	
	// perform the crop,
	// The 0.5 here is an adjustable parameter and must be adjusted for
	// the amount of "crop" desired. Larger crop-factor results in a smaller
	// window and loss of data but increases the success rate. 0.5 seems to work
	// for most cases in the PH2 database.
	Rect ROI(0, 0, cortical.cols - (maxCrop * 0.5), cortical.rows);
	
	// reproject the image to cartesian
	Mat cartesian = nearest.to_cartesian(cortical(ROI));
	
	// threshold so that we only get the mask
	return cartesian > 0;
}

/*
 * Simply applies meanshift on the image
 */
Mat presegmentLesion(Mat img){
	Mat meanShifted;
	pyrMeanShiftFiltering(img, meanShifted, 30, 50, 1);

	return meanShifted;
}

/*
 * Finds the hair mask. We use a similar algorithm to Dullrazor.
 *
 *
 * 1) convert the image to grayscale
 *
 * 2) blur the grayscale image with a median filter
 *
 * 3) apply a morphological closing operation on the blurred image
 *
 * 4) apply a difference operation closed - gray
 *
 * 5) threshold the mask at some low value
 *
 * 6) clean the mask with structural analysis
 *
 * 7) dilate the mask
 */
Mat findHairMask(Mat img){
	// we use the RGB averaging method to convert to grayscale
	Mat gray;
	cvtColor(img, gray, COLOR_BGR2GRAY);
	
	// and blur the image with a radius of 5
	Mat blurred;
	medianBlur(gray, blurred, 5);
	
	Mat closed;
	morphologyEx(blurred, closed, MORPH_CLOSE, getStructuringElement(MORPH_ELLIPSE, Size(9, 9)));

	Mat msk = closed - gray;

#ifdef DEBUG
	imshow("msk", msk);
#endif
	
	// the threshold 20, is arbitrary but seemed to work on most cases
	Mat thresh;
	threshold(msk, thresh, 20, 255, THRESH_BINARY);
	
	Mat temp = thresh.clone();
	
	// find contours of the thresholded image
	vector<vector<Point>> contours;
	vector<Vec4i> hierarchy;
	findContours(temp, contours, hierarchy, RETR_CCOMP, CHAIN_APPROX_SIMPLE);
	
	// if no contours were found, it implies there were no hair pixels
	// so simply return an empty 8-bit unsigned, single channel image
	if(contours.size() == 0) return Mat(img.size(), CV_8UC1);

	int idx = 0;

	for(; idx >= 0; idx = hierarchy[idx][0]){
		const vector<Point>& c = contours[idx];
		double area = contourArea(Mat(c));
		// only include contours whose area is greater than 10
		// this rejects most noisy blobs
		if(area < 10){
			drawContours(thresh, contours, idx, Scalar(0, 0, 0), FILLED, 8);
		}
	}
	
	// dilate the mask so that there aren't "edge" artefacts when we
	// inpaint
	dilate(thresh, thresh, Mat(), Point(-1, -1));

	return thresh;
}

/*
 * Finds the lesion and cleans up the thresholded image
 *
 * 1) Finds the contours
 *
 * 2) Filters the contour with the largest area
 */
Mat findLesion(Mat thresh){
	Mat temp = thresh.clone();

	vector<vector<Point>> contours;
	vector<Vec4i> hierarchy;

	findContours(temp, contours, hierarchy, RETR_CCOMP, CHAIN_APPROX_SIMPLE);

	if(contours.size() == 0) return thresh;

	int idx = 0;

	double maxArea = 0;
	int maxIdx = 0;

	for(; idx >= 0;idx = hierarchy[idx][0]){
		const vector<Point>& c = contours[idx];
		double area = contourArea(Mat(c));

		if(area > maxArea){
			maxArea = area;
			maxIdx = idx;
		}
	}

	Mat ret(thresh.size(), thresh.type(), Scalar::all(0));

	drawContours(ret, contours, maxIdx, Scalar(255, 255, 255), FILLED, 8);

	return ret;
}

/*
 * Finds the border of a lesion mask
 *
 * Consists of simply dilation and erosion of the original set and a difference
 */
Mat findBorder(Mat thresh){
	Mat dilated;
	Mat erod;
	Mat strel = getStructuringElement(MORPH_ELLIPSE, Size(20, 20));
	erode(thresh, erod, strel);
	dilate(thresh, dilated, strel);
	return dilated - erod;
}

/*
 * Calculates the Hue component of HSV
 *
 * https://en.wikipedia.org/wiki/Hue
 */
double calculateHue(Vec3f bgr){
	double M = max(bgr[0], max(bgr[1], bgr[2])); // max
	double m = min(bgr[0], min(bgr[1], bgr[2])); // min
	double C = M - m; // chrominance

	double Hp = 0; // H'

	if(C < EPSILON){ // the hue is undefined for chrominance = 0
		return -1.0;
	} else if(M == bgr[2]){
		Hp = fmod((bgr[1] - bgr[0]) / C, 6);
	} else if(M == bgr[1]){
		Hp = ((bgr[0] - bgr[2]) / C) + 2;
	} else if(M == bgr[0]){
		Hp = ((bgr[2] - bgr[1]) / C) + 4;
	}

	return Hp * 60;
}

/*
 * Calculates number of colours in the lesion.
 *
 * This is a difficult problem since colour is subjective, the algorithm used
 * here is very rudimentary and can be improved.
 *
 * 0) Mask the mean-shifted image to get only the lesion pixels
 *
 * 1) apply a k-means clustering algorithm to the mean-shifted image
 * in RGB colour space.
 * 
 * 2) take the centroids and calculate the Hue for each centroid (RGB isn't
 * a very good metric for perceptual colour)
 *
 * 3) Sort the hues and find the median hue
 *		- we refrain from using the mean because the masking operation at (0)
 *			results in a lot of dark pixels and the mean can be skewed by these
 *			extremas
 *
 * 4) for all hues calculate the distance to the median hue
 *
 * 5) if the residual is greater than 3 degrees, consider it a new colour
 *
 */
int calculateColourVar(Mat img, Mat mask){
	Mat labels, centers;
	
	/* (0) */

	Mat maskedImg;
	bitwise_and(img, img, maskedImg, mask);

	/* (1) */

	Mat linear = maskedImg.reshape(1, maskedImg.rows * maskedImg.cols); // prepare the image for k-means
	linear.convertTo(linear, CV_32F); // convert from 8-bit unsigned to 32-bit floats

	const int K = 10; // we decide to partition the data into 10 clusters, this is quite arbitrary but 10 seems like a good estimate since > 6 colours usually implies a possible case of MM
	
	kmeans(
		linear, K, labels,
		TermCriteria(TermCriteria::EPS | TermCriteria::MAX_ITER, 10, 1.0),
		10,
		KMEANS_RANDOM_CENTERS,
		centers
	);

	/* (2) */
	
	vector<double> hues;
	for(int i = 0;i < K;i++){
		hues.push_back(calculateHue(centers.at<Vec3f>(i)));
	}

	/* (3) */

	sort(hues.begin(), hues.end());

	double medianHue = 0;

	if(K % 2 == 0){
		int halfK = K >> 1;
		medianHue = (hues[halfK] + hues[halfK + 1]) / 2;
	} else { 
		medianHue = hues[ceil(K / 2)];
	}

	/* (4) */

	int colors = 0;

	for(vector<double>::iterator it = hues.begin();it != hues.end();it++){
		if(*it == -1.0) continue; // skip undefined values

		double residual = abs(*it - medianHue);

		if(residual > 3) colors++;
	}

	return colors;
}

/*
 * Calculates simple features using structural analysis, and prints them
 * out on screen in CSV format.
 */
void calculateFeatures(Mat image, Mat flat, Mat mask){
	Mat border = findBorder(mask);
	
	// find the contours to calculate structural metrics
	Mat tmp = mask.clone();
	vector<vector<Point>> contours;
	vector<Vec4i> hierarchy;
	
	// at this stage, the mask contains only one contour so there is no
	// need for hierarchy

	findContours(tmp, contours, hierarchy, RETR_LIST, CHAIN_APPROX_SIMPLE);
	
	double perimeter = arcLength(contours[0], true);
	double area = contourArea(contours[0]);
	
	// calculate compactness as per the paper:
	//    "Measurements of digitized objects with fuzzy borders in 2D and 3D"
	//      -- Sladoje, et. al
	double compactNess = (perimeter * perimeter) / ((4 * M_PI) * area);

	Rect lesionBoundingRect = boundingRect(contours[0]);

	int cvary = calculateColourVar(flat(lesionBoundingRect), mask(lesionBoundingRect));
	
	// print the metrics out in a simple way
	// this is not the best-approach but is simple enough to fit
	// within the time-frame of this project
	cout << "Perimeter,Area,Compactness,ColorVariegation" << endl;
	cout << perimeter << "," << area << "," << compactNess << "," << cvary << endl;

#ifdef DEBUG
	imshow("image", image);
	imshow("mask", mask);
	imshow("border", border);
#endif
}

void process(char* filename){
	string fname(filename);

	Mat image = imread(filename);
	Mat contrastEnh = enhanceContrast(image);
	
#ifdef DEBUG
	imwrite("ceh.png", contrastEnh);
#endif

	Mat borderMask = findBorderMask(image);

#ifdef DEBUG
	imwrite("bmask.png", borderMask);
#endif
	
	// pass the image through a mean-shift operation to simplify
	// the texture
	Mat segmented = presegmentLesion(contrastEnh);

#ifdef DEBUG
	imwrite("segment.png", segmented);
#endif

	Mat hairMask = findHairMask(image);

#ifdef DEBUG
	imwrite("hmask.png", hairMask);
#endif
	
	// inpainting is simpler with a reduction of texture
	// we could use a simple bilinear-interpolation method to inpaint
	// after the mean-shift operation but opencv did not have simpler
	// inpainting operations and I do not yet have the knowledge to implement
	// it myself
	Mat segRep;
	inpaint(segmented, hairMask, segRep, 10, INPAINT_NS);

#ifdef DEBUG
	imwrite("inpaint.png", segRep);
#endif
	
	Mat grayRep;
	cvtColor(segRep, grayRep, COLOR_BGR2GRAY);
	
	Mat thresh;
	threshold(grayRep, thresh, 0, 255, THRESH_BINARY + THRESH_OTSU);
	
	// invert the image
	thresh = 255 - thresh;
	
	// resize the threshold to the same size as the border mask
	// the projection- and reprojection- of the image into log-polar
	// coordinates loses some pixels (interpolation artefact)
	thresh = thresh(Rect(0, 0, borderMask.cols, borderMask.rows));
	
	// combine the border mask and lesion threshold
	Mat th;
	bitwise_and(thresh, thresh, th, borderMask);
	
	Mat lesion = findLesion(th);

#ifdef DEBUG
	imwrite("lmask.png", lesion);
#endif

	calculateFeatures(image, segRep, lesion);
}

int main(int argc, char** argv){
	cerr << "Please provide a filename" << endl;

	if(argc < 2) return 1;

	process(argv[1]);

#ifdef DEBUG
	int k = 0;
	while(true){
		k = waitKey(0) & 0xFF;
		if(k == 27){
			return 1;
		} else if(k == 32){
			break;
		}
	}
#endif

	return 0;
}
