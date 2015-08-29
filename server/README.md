This is a simple REST-api written in python with the flask library.

It serves two endpoints:
* _GET /questions_ which serves the questions that is displayed in the client application
* _POST /upload_ which receives the image upload

# Questions endpoint

The questions endpoint returns a JSON response adhering to the following schema:

```json
{
	"$schema": "http://json-schema.org/draft-04/schema#",
	"title": "Question set",
	"type": "array",
	"items": {
		"title": "Question",
		"type": "object",
		"properties": {
			"id": {
				"description": "The unique identifier of a question item",
				"type": "string"
			},
			"question": {
				"description": "Human-readable interpretation of the question",
				"type": "string"
			},
			"type": {
				"description": "The type of field",
				"enum": ["option", "number"]
			},
			"options": {
				"type": "array",
				"items": {
					"type": "string"
				},
				"minItems": 1,
				"uniqueItems": true
			}
		},
		"required": ["id", "question", "type"]
	}
}
```

In its current implementation the questions are hardcoded into the `DummyExpertSystem` class and defined by the `ExpertSystem` interface. Since python does not have "interfaces" these are simply shadowed in the `backend/__init__.py` file --- and while interfaces do not define static methods, the methods have been declared static for simplicity. These "interfaces" represent more of a conceptual contract than a language-enforced one. 

# Upload endpoint

The upload endpoint accepts an image and any additional data (currently the additional data is not handled) as a multipart/form-data. The image is referenced using the "image" key in the request. The additional data must adhere to the following schema:

```json
{
	"$schema": "http://json-schema.org/draft-04/schema#",
	"title": "Answers",
	"type": "array",
	"items": {
		"title": "Answer",
		"type": "object",
		"properties": {
			"id": {
				"description": "The unique question identifier",
				"type": "string"
			},
			"value": {
				"description": "The answer to this question. A number for numeric types and an index for option types.",
				"type": "string"
			}
		},
		"required": ["id", "value"]
	}
}
```

On request, it passes on the image to the image processor. The current implementation of the image processor is a simple C++ script using OpenCV 2.4.9. It is a command-line application that accepts a single argument: the path of the image. The `ImageProcessorCLI` class provides a rudimentary abstraction on the details of how this script is invoked and it is shadowed as `ImageProcessor`. The `ImageProcessor` abstraction has a single static method (see above on static methods) `process` that accepts the path to the image on disk. It returns a tuple of the form `(status, payload)` where status represents whether the processing succeeded or not (due to various reasons). The payload contains a dict of metrics if processing was successful, or an error message on failure.

# imgproc.cpp

The backend directory contains a c++ file and a Makefile. The `ImageProcessorCLI` depends on this binary, thus it is recommended to run `make` in the directory. It depends on g++ and Opencv 2.4.9. It's usage is:

	./imgproc [filename]

It's output is a comma-separated list of the form: "Perimeter,Area,Compactness,ColorVariegation". 

It contains a `DEBUG` macro flag that can be `#define`d to enable introspection.
