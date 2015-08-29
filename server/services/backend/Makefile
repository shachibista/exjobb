CC=g++-4.9
CFLAGS=-Wall -g `pkg-config --cflags opencv` -std=c++11
LIBS=`pkg-config --libs opencv` -lm

all:
	$(CC) $(CFLAGS) imgproc.cpp -o imgproc $(LIBS)
