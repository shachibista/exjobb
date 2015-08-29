"""
	Just a dummy hard-coded questions database until we get a real expert system
"""

Database = {
	"questions": [
		  {
			"id": "skin_type",
			"question": "Skin type",
			"type": "option",
			"options": [
				"Type I",
				"Type 2",
				"Type 3",
				"Type 4"
			]
		  },
		  {
			"id": "age",
			"question": "Age",
			"type": "number"
		  },
		  {
			"id": "gender",
			"question": "Gender",
			"type": "option",
			"options": [
				"Male",
				"Female"
			]
		  },
		  {
			"id": "pob",
			"question": "Part of Body",
			"type": "option",
			"options": [
				"Head and neck",
				"Trunk",
				"Arm",
				"Leg"
			]
		}
	]
};

class ExpertSystem:
	@staticmethod
	def getQuestions():
		return Database["questions"]
