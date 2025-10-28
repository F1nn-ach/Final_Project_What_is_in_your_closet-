document.addEventListener("DOMContentLoaded", function () {
    const clothImageInput = document.getElementById("clothImage");
    const previewImage = document.getElementById("preview-image");
    const uploadPrompt = document.getElementById("upload-prompt");
    const processingOverlay = document.getElementById("processing-overlay");
    const errorOverlay = document.getElementById("error-overlay");
    const addClothForm = document.getElementById("addClothForm");
    const imageFileInput = document.getElementById("imageFile");
    const imageUrlInput = document.getElementById("imageUrlInput")
    const classifiedColorInput = document.getElementById("classifiedColor");
    const username = document.getElementById("username").value;
    const resetButton = document.getElementById('resetButton');

    const colorClassificationResults = document.getElementById("colorClassificationResults");
    const colorPaletteContainer = document.getElementById("colorPaletteContainer");

    let classificationPromise = null;
    let classificationResultData = null;

    // Reset form if there's a success message
    if (typeof hasSuccessMessage !== 'undefined' && hasSuccessMessage) {
        resetForm();
    }

    // Auto-hide notices after 5 seconds
    const notices = document.querySelectorAll('.notice');
    notices.forEach(notice => {
        setTimeout(() => {
            notice.style.animation = 'fadeOutNotice 0.5s ease-in forwards';
            setTimeout(() => {
                notice.remove();
            }, 500);
        }, 5000);
    });

    function resetForm() {
        addClothForm.reset();
        previewImage.src = '';
        previewImage.style.display = 'none';
        uploadPrompt.style.display = 'flex';
        imageFileInput.value = '';
        classifiedColorInput.value = '';
        imageUrlInput.value = '';
        classificationPromise = null;
        classificationResultData = null;
        clearClassificationResults();
    }


    function showProcessingOverlay() {
        if (processingOverlay) {
            processingOverlay.style.display = '';
            processingOverlay.classList.add('show');
        }
    }

    function hideProcessingOverlay() {
        if (processingOverlay) {
            processingOverlay.classList.remove('show');
        }
    }

    function updateProcessingMessage(title, description) {
        const titleElement = processingOverlay.querySelector('.processing-title');
        const descElement = processingOverlay.querySelector('.processing-description');

        if (titleElement) titleElement.textContent = title;
        if (descElement) descElement.textContent = description;
    }

    function showErrorOverlay(title, description) {
        if (errorOverlay) {
            const titleElement = errorOverlay.querySelector('.processing-title');
            const descElement = errorOverlay.querySelector('.processing-description');

            if (titleElement) titleElement.textContent = title;
            if (descElement) descElement.textContent = description;

            errorOverlay.style.display = '';
            errorOverlay.classList.add('show');

            // Auto hide after 3 seconds
            setTimeout(() => {
                errorOverlay.classList.remove('show');
            }, 3000);
        }
    }

    function removeColor(hexToRemove) {
        if (classificationResultData && classificationResultData.colors) {
            const currentColors = classificationResultData.colors;
            if (currentColors.length <= 1) {
                return;
            }

            classificationResultData.colors = currentColors.filter(
                color => color.hex !== hexToRemove
            );
            displayClassificationResults(classificationResultData);
            classifiedColorInput.value = JSON.stringify(classificationResultData);
        }
    }

    function displayClassificationResults(classifyResult) {
        if (classifyResult && classifyResult.colors && Array.isArray(classifyResult.colors)) {
            colorClassificationResults.style.display = "block";
            colorPaletteContainer.innerHTML = "";

            let dominantColor = null;
            if (classifyResult.colors.length > 0) {
                dominantColor = classifyResult.colors.reduce((prev, current) =>
                    (prev.percentage > current.percentage) ? prev : current
                );
            }

            classifyResult.colors.forEach(color => {
                const colorItemDiv = document.createElement("div");
                colorItemDiv.className = "color-grid-item";

                const colorBlock = document.createElement("div");
                colorBlock.className = "color-block-small";
                colorBlock.style.backgroundColor = color.hex;

                const colorNameSpan = document.createElement("span");
                colorNameSpan.textContent = color.color_name;

                const removeButton = document.createElement("button");
                removeButton.className = "remove-color-btn";
                removeButton.textContent = "X";
                removeButton.onclick = () => removeColor(color.hex);

                if (dominantColor && dominantColor.hex === color.hex && classifyResult.colors.length <= 1) {
                    removeButton.disabled = true;
                    removeButton.title = "Cannot remove the dominant or only color.";
                } else if (dominantColor && dominantColor.hex === color.hex) {
                    removeButton.title = "This is the dominant color.";
                }


                colorItemDiv.appendChild(colorBlock);
                colorItemDiv.appendChild(colorNameSpan);
                colorItemDiv.appendChild(removeButton);
                colorPaletteContainer.appendChild(colorItemDiv);
            });
        }
    }

    function clearClassificationResults() {
        colorClassificationResults.style.display = "none";
        colorPaletteContainer.innerHTML = "";
    }

    if (uploadPrompt) {
        uploadPrompt.addEventListener("click", () => {
            if (clothImageInput) {
                clothImageInput.click();
            }
        });
    }

    if (clothImageInput) {
        clothImageInput.addEventListener("change", async (event) => {
            const file = event.target.files[0];
            if (!file) {
                return;
            }

            showProcessingOverlay();
            updateProcessingMessage("กำลังประมวลผล...", "กรุณารอสักครู่ เรากำลังลบพื้นหลังและวิเคราะห์สีของเสื้อผ้า");
            clearClassificationResults();
            classificationPromise = null;
            classificationResultData = null;

            const formData = new FormData();
            formData.append("file", file);
            formData.append("username", username);

            try {
                const response = await fetch("/cloth/api/analyze-image", {
                    method: "POST",
                    body: formData,
                });

                if (!response.ok) {
                    if (response.status === 413) {
                        throw new Error(`HTTP error! status: 413`);
                    }
                    const responseText = await response.text();
                    throw new Error(`HTTP error! status: ${response.status}, message: ${responseText}`);
                }

                const responseText = await response.text();

                let result;
                try {
                    result = JSON.parse(responseText);
                } catch (jsonError) {
                    throw new Error(`Failed to parse JSON response from /api/analyze-image. Raw response: ${responseText.substring(0, 200)}...`);
                }

                if (result.success && result.imagePath && result.colors) {
                    uploadPrompt.style.display = "none";
                    previewImage.src = imageBaseUrl + username + "/" + result.imagePath;
                    previewImage.style.display = "block";
                    imageFileInput.value = result.imagePath;
                    imageUrlInput.value = imageBaseUrl + username + "/" + result.imagePath;

                    // Process color classification results
                    classificationResultData = { colors: result.colors };
                    displayClassificationResults(classificationResultData);
                    classifiedColorInput.value = JSON.stringify(classificationResultData);
                    hideProcessingOverlay();

                } else {
                    throw new Error(result.message || "Invalid API response format for analyze-image.");
                }
            } catch (error) {
                hideProcessingOverlay();
                console.error("Upload error:", error);

                // Reset file input to allow re-uploading
                clothImageInput.value = "";

                // Display error popup overlay
                let errorTitle = "เกิดข้อผิดพลาด";
                let errorDescription = "ไม่สามารถอัพโหลดรูปภาพได้ กรุณาลองใหม่อีกครั้ง";

                if (error.message.includes("413")) {
                    errorTitle = "ไฟล์ใหญ่เกินไป";
                    errorDescription = "รูปภาพมีขนาดใหญ่เกินไป กรุณาเลือกไฟล์ที่มีขนาดเล็กกว่า";
                }

                showErrorOverlay(errorTitle, errorDescription);
            }
        });
    }

    // Handle form submission
    addClothForm.addEventListener("submit", (event) => {
        // Form can be submitted directly as analysis is already completed
        // The classifiedColorInput value is already populated during image upload
    });


    if (resetButton) {
        resetButton.addEventListener('click', (event) => {
            event.preventDefault();
            resetForm();
        });
    }

});