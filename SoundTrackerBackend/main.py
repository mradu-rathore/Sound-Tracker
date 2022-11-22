from flask import Flask, render_template

app = Flask("app")
API_KEY="AIzaSyAsq4sV3KXA6rmWuqKTx4vUGQnTF7-g9yA"

@app.route("/")
def main():
    return render_template("index.html", key=API_KEY)

if __name__ == "__main__":
    app.run(host="0.0.0.0", port="5000", debug=True)