import subprocess
import csv
import StringIO

from flask.views import MethodView
from flask import json, request, abort

from backend import ImageProcessor

class UploadService(MethodView):
	"""
		Handler for the upload end-point
	"""
	def post(self):
		if 'image' in request.files:
			# if image exists in the request, save it
			f = request.files["image"]
			fileLocation = "/tmp/uploaded.png"
			f.save(fileLocation)
			
			status, metrics = ImageProcessor.process(fileLocation)

			print status
			print metrics

			if status:
				return (json.dumps(metrics), 200, {
					'Content-Type': 'application/json'
				})
			else:
				abort(500)
		else:
			abort(406)
