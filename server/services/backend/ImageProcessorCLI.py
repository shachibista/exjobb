import subprocess
import csv
import os.path

class ImageProcessor:
	@staticmethod
	def process(path):
		process = subprocess.Popen([os.path.dirname(__file__) + "/imgproc", path], stdout=subprocess.PIPE, stderr=subprocess.PIPE) # ideally, this should go to a message queue ...
		output, retcode = process.communicate()
		
		try:
			if retcode == 0:
				# convert string to a reader object, python csv module doesn't support strings, and splitting at "\n" is not so elegant
				shandle = StringIO.StringIO(output)
				reader = csv.reader(shandle)
				
				# import magic
				metrics = dict(zip(reader.next(), reader.next()))

				return (True, metrics)
			else:
				return (False, output)
		except OSError as e:
			print e
