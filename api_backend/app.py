from flask import Flask, request, jsonify, send_from_directory, render_template
from remove_bg.remove_bg import process_image_for_bg_removal
from matching_color.matching_logic2 import match_colors_logic
from classify_color.color_classifier import extract_color_palette
from classify_color.color_group import classify_all_colors
import os
from flask_cors import CORS

app = Flask(__name__, static_folder="basic_client", static_url_path="")
CORS(app)

NO_BG_FOLDER = os.path.join(os.path.dirname(os.path.abspath(__file__)), "no_bg")


@app.route("/cloth/remove-background", methods=["POST"])
def remove_background_route():
    if "file" not in request.files or "username" not in request.form:
        return jsonify({"error": "Missing file or username"}), 400

    file = request.files["file"]
    username = request.form["username"]

    if file.filename == "":
        return jsonify({"error": "No selected file"}), 400

    image_path = process_image_for_bg_removal(file, username)
    image_url = f"{request.host_url}no_bg/{username}/{os.path.basename(image_path)}"

    return jsonify({"success": True, "imagePath": image_path, "imageUrl": image_url})


@app.route("/no_bg/<username>/<filename>")
def serve_no_bg_file(username, filename):
    user_folder = os.path.join(NO_BG_FOLDER, username)
    return send_from_directory(user_folder, filename)


@app.route("/cloth/classify-color", methods=["POST"])
def classify_color_route():
    data = request.get_json()
    if not data or "imageUrl" not in data:
        return jsonify({"error": "No imageUrl provided"}), 400

    image_url = data["imageUrl"]
    if not image_url:
        return jsonify({"error": "Empty imageUrl"}), 400

    result = extract_color_palette(image_url)
    return jsonify(result)


@app.route("/cloth/match-colors", methods=["POST"])
def match_colors_route():
    data = request.get_json()
    if not data:
        return jsonify({"error": "No data provided"}), 400

    result = match_colors_logic(data)
    return jsonify(result)


@app.route("/")
def serve_index():
    return send_from_directory("basic_client", "index.html")


@app.route("/list-colors")
def cloth_list():
    all_colors = classify_all_colors()
    grouped = {}
    for c in all_colors:
        grouped.setdefault(c["main_group"], []).append(c)

    return render_template("index.html", colors=grouped)


if __name__ == "__main__":
    app.run(debug=True)
