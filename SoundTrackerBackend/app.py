from flask import Flask, render_template

app = Flask("app")
API_KEY="AIzaSyAsq4sV3KXA6rmWuqKTx4vUGQnTF7-g9yA"

@app.route("/")
def main():
    return render_template("index.html", key=API_KEY)