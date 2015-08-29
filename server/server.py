from flask import Flask

from services import *

app = Flask(__name__)

# Questions end-point
question_view = QuestionService.as_view('question_api')
app.add_url_rule('/questions', defaults={'question_id': None}, view_func=question_view, methods=['GET'])

# Upload end-point
upload_view = UploadService.as_view('upload')
app.add_url_rule('/upload', view_func=upload_view, methods=['POST'])

if __name__ == '__main__':
	app.run(debug=True, host='0.0.0.0')
