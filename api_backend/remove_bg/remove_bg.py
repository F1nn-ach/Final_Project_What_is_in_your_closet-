from rembg import remove
from PIL import Image
from werkzeug.utils import secure_filename
import io
import os

UPLOAD_FOLDER = "uploads"
NO_BG_FOLDER = "no_bg"

def remove_background(input_path, output_path):
    with open(input_path, 'rb') as input_file:
        input_data = input_file.read()

    output_data = remove(input_data)
    output_image = Image.open(io.BytesIO(output_data))
    output_path = os.path.splitext(output_path)[0] + ".png"
    output_image.save(output_path, format="PNG")

    return output_path

def process_image_for_bg_removal(file, username):
    if not os.path.exists(UPLOAD_FOLDER):
        os.makedirs(UPLOAD_FOLDER)

    user_no_bg_folder = os.path.join(NO_BG_FOLDER, username)
    os.makedirs(user_no_bg_folder, exist_ok=True)

    filename = secure_filename(file.filename)
    uploaded_image_path = os.path.join(UPLOAD_FOLDER, filename)
    file.save(uploaded_image_path)

    no_bg_filename = f"{os.path.splitext(filename)[0]}.png"
    no_bg_image_path = os.path.join(user_no_bg_folder, no_bg_filename)

    remove_background(uploaded_image_path, no_bg_image_path)

    return no_bg_filename