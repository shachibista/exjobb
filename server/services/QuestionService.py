from flask.views import MethodView
from flask import json, abort

from backend import ExpertSystem

class QuestionService(MethodView):
	"""
		Handler for the questions end-point
	"""
	def get(self, question_id):
		if question_id is None:
			return (json.dumps(ExpertSystem.getQuestions()), 200, {
				'Content-Type': 'application/json'
			})
		else:
			abort(406)
