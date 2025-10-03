<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add Cloth</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/add-cloth.css">
</head>
<body>
    <%@ include file="includes/header.jsp" %>

    <main>
        <div class="content">

        <!-- Notice Messages -->
        <c:if test="${not empty successMessage}">
            <div class="notice notice-success">
                <div class="notice-icon">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M9 12l2 2 4-4"/>
                        <circle cx="12" cy="12" r="10"/>
                    </svg>
                </div>
                <div class="notice-content">
                    <h4 class="notice-title">สำเร็จ!</h4>
                    <p class="notice-message">${successMessage}</p>
                </div>
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="notice notice-error">
                <div class="notice-icon">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"/>
                        <line x1="15" y1="9" x2="9" y2="15"/>
                        <line x1="9" y1="9" x2="15" y2="15"/>
                    </svg>
                </div>
                <div class="notice-content">
                    <h4 class="notice-title">เกิดข้อผิดพลาด!</h4>
                    <p class="notice-message">${errorMessage}</p>
                </div>
            </div>
        </c:if>

        <!-- Page header with title and back button -->
        <div class="page-header">
            <h1 class="page-title">เพิ่มเสื้อผ้าใหม่</h1>
            <a href="${pageContext.request.contextPath}/list-item" class="back-link">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M19 12H5M5 12L12 19M5 12L12 5"></path>
            </svg>
            กลับไปหน้ารายการเสื้อผ้า
            </a>
        </div>

        <!-- Add clothes form -->
        <div class="form-card">
            <form id="addClothForm" method="post" action="${pageContext.request.contextPath}/add-item">
            <input type="hidden" id="imageFile" name="imageFile">
            <input type="hidden" id="classifiedColor" name="classifiedColor">
            <input type="hidden" id="username" name="username" value="${username}">

            <!-- Image upload -->
            <div class="upload-preview upload-area" id="imagePreview">
                <div id="upload-prompt" style="display: ${not empty clothImage ? 'none' : 'flex'}">
                    <div class="upload-icon">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                        <polyline points="17 8 12 3 7 8"></polyline>
                        <line x1="12" y1="3" x2="12" y2="15"></line>
                    </svg>
                    </div>
                    <div class="upload-text">คลิกเพื่ออัพโหลดรูปภาพ</div>
                    <div class="upload-format">รองรับไฟล์ JPG, PNG (ไม่เกิน 5MB)</div>
                </div>
                <img src="${pageContext.request.contextPath}/${clothImage}" alt="Image Preview" id="preview-image" style="display: ${not empty clothImage ? 'block' : 'none'}">

                <!-- Processing Overlay -->
                <div id="processing-overlay" class="processing-overlay">
                    <div class="processing-content">
                        <div class="modern-spinner"></div>
                        <h4 class="processing-title">กำลังประมวลผล...</h4>
                        <p class="processing-description">กรุณารอสักครู่ เรากำลังลบพื้นหลังและวิเคราะห์สีของเสื้อผ้า</p>
                    </div>
                </div>

                <!-- Error Overlay -->
                <div id="error-overlay" class="processing-overlay error-overlay">
                    <div class="processing-content">
                        <div class="error-icon">✕</div>
                        <h4 class="processing-title">เกิดข้อผิดพลาด</h4>
                        <p class="processing-description">ไม่สามารถอัพโหลดรูปภาพได้ กรุณาลองใหม่อีกครั้ง</p>
                    </div>
                </div>
            </div>
            <input type="file" id="clothImage" name="file" style="display: none;" accept="image/*">
            <!-- Removed duplicate processedImageUrl input -->

            <div class="form-group">
                <label for="clothType">ประเภทเสื้อผ้า</label>
                <select id="clothType" name="clothType" required>
                    <option value="" disabled selected>เลือกประเภทเสื้อผ้า</option>
                <c:forEach var="clothType" items="${clothTypes}">
                                <option value="${clothType.clothTypeId}">${clothType.typeName}</option>
                            </c:forEach>
                </select>
            </div>
            <input type="hidden" id="classifiedColorName" name="classifiedColorName">

            <div id="colorClassificationResults" style="display: none;">
                <h3>ผลการจำแนกสี</h3>
                <div id="colorPaletteContainer" class="color-list">
                </div>
            </div>

            <input type="hidden" name="imageUrl" id="imageUrlInput">
            <input type="hidden" name="colorData" id="colorDataInput">

            <div class="button-group">
                <button type="submit" class="btn btn-primary">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M19 21H5a2 2 0 0 1-2 2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
                    <polyline points="17 21 17 13 7 13 7 21"></polyline>
                    <polyline points="7 3 7 8 15 8"></polyline>
                </svg>
                บันทึกเสื้อผ้า
                </button>
                <button type="reset" class="btn btn-outline" id="resetButton">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M19 12H5"></path>
                </svg>
                ล้างฟอร์ม
                </button>
            </div>
            </form>
        </div>
        </div>
    </main>

    <%@ include file="includes/footer.jsp" %>
    <script>
        var imageBaseUrl = "${imageBaseUrl}";
        var hasSuccessMessage = ${not empty successMessage};
    </script>
    <script src="${pageContext.request.contextPath}/static/js/add-cloth.js"></script>
</body>
</html>

