<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="th">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial=1.0">
    <title>จับคู่สีเสื้อผ้า - What is in your closet</title>
    <link href="https://fonts.googleapis.com/css2?family=Prompt:wght@300;400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/matching-cloth.css">
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        const imageBaseUrl = '${imageBaseUrl}';
        const allLuckyColorsData = [
            <c:forEach var="lc" items="${allLuckyColors}" varStatus="status">
            {
                astrologerId: ${lc.astrologer.astrologerId},
                luckyColorTypeId: ${lc.luckyColorType.luckyColorTypeId}
            }${!status.last ? ',' : ''}
            </c:forEach>
        ];
    </script>
</head>

<body>
    <%@ include file="includes/header.jsp" %>

    <main>
        <div class="container">
            <div class="page-header">
                <h1 class="page-title">จับคู่สีเสื้อผ้า</h1>
                <p class="page-subtitle">เลือกเสื้อผ้าและสีโชคดีเพื่อจับคู่ชุดที่เหมาะสม</p>
            </div>

            <div class="matching-layout">
                <div class="left-column">
                    <div class="panel">
                        <h2 class="panel-title">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"></path>
                            </svg>
                            เลือกนักโหราศาสตร์
                        </h2>

                        <div class="form-group">
                            <label class="form-label">นักโหราศาสตร์:</label>
                            <select class="form-control" id="astrologer-select">
                                <option value="">-- เลือกนักโหราศาสตร์ --</option>
                                <c:forEach var="astrologer" items="${astrologers}">
                                    <option value="${astrologer.astrologerId}">${astrologer.astrologerName}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group" id="day-group" style="display: none;">
                            <label class="form-label">วันในสัปดาห์:</label>
                            <select class="form-control" id="day-select">
                                <c:forEach var="day" items="${daysOfWeek}">
                                    <option value="${day}" ${currentDay == day ? 'selected' : ''}>${day}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group" id="lucky-types-group" style="display: none;">
                            <label class="form-label">ประเภทโชค:</label>
                            <div class="checkbox-group">
                                <c:forEach var="luckyType" items="${luckyColorTypes}">
                                    <label class="checkbox-item">
                                        <input type="checkbox" class="lucky-type-checkbox" value="${luckyType.luckyColorTypeId}">
                                        <span class="checkbox-label">${luckyType.luckyColorTypeName}</span>
                                    </label>
                                </c:forEach>
                            </div>
                        </div>
                    </div>

                    <div class="panel">
                        <h2 class="panel-title">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <circle cx="13.5" cy="6.5" r="2.5"></circle>
                                <circle cx="19" cy="17" r="2"></circle>
                                <circle cx="5" cy="17" r="2"></circle>
                                <path d="m9 17-2-3h10l-2 3"></path>
                                <path d="M13.5 9 8 11"></path>
                                <path d="m13.5 9 5.5 2"></path>
                            </svg>
                            ทฤษฎีการจับคู่สี
                        </h2>

                        <div class="form-group">
                            <label class="form-label">เลือกทฤษฎีสี:</label>
                            <select class="form-control" id="color-theory-select">
                                <option value="">-- เลือกทฤษฎีก่อน --</option>
                                <c:forEach var="theory" items="${colorTheories}">
                                    <option value="${theory.colorTheoryId}"
                                            data-description="${theory.description}"
                                            data-theory-name="${theory.colorTheoryName}">${theory.colorTheoryName}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div id="color-theory-description" class="theory-description" style="display: none;">
                            <div class="description-content"></div>
                        </div>
                    </div>
                </div>

                <div class="center-column">
                    <div class="panel">
                        <h2 class="panel-title">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M20.38 3.46L16 2a4 4 0 0 1-8 0L3.62 3.46a2 2 0 0 0-1.34 2.23l.58 3.47a1 1 0 0 0 .99-.84H6v10c0 1.1.9 2 2 2h8a2 2 0 0 0 2-2V10h2.15a1 1 0 0 0 .99-.84l.58-3.47a2 2 0 0 0-1.34-2.23z"></path>
                            </svg>
                            รายการเสื้อผ้าทั้งหมด
                        </h2>

                        <div class="clothing-filters">
                            <div class="cloth-type-buttons">
                                <button class="cloth-type-btn active" data-type="all">ทั้งหมด</button>
                                <c:forEach var="clothingType" items="${clothingTypes}">
                                    <button class="cloth-type-btn" data-type-id="${clothingType.clothingTypeId}">${clothingType.clothingTypeName}</button>
                                </c:forEach>
                            </div>
                        </div>

                        <div class="clothing-grid-container">
                            <div class="clothing-grid" id="clothing-grid">
                                <c:choose>
                                    <c:when test="${not empty clothes}">
                                        <c:forEach var="cloth" items="${clothes}">
                                            <div class="clothing-item"
                                                 data-cloth-id="${cloth.clothingId}"
                                                 data-type-id="${cloth.clothingType.clothingTypeId}"
                                                 data-type-name="${cloth.clothingType.clothingTypeName}"
                                                 data-cloth-image="${cloth.clothingImage}"
                                                 data-username="${cloth.user.username}"
                                                 data-color-hex="${cloth.dominantColor != null ? cloth.dominantColor.colorCategoryHex : '#000000'}"
                                                 data-color-name="${cloth.dominantColor != null ? cloth.dominantColor.colorCategoryName : ''}">
                                                <div class="clothing-image">
                                                    <img src="${imageBaseUrl}${cloth.user.username}/${cloth.clothingImage}" alt="${cloth.clothingType.clothingTypeName}" loading="lazy">
                                                </div>
                                                <div class="clothing-info">
                                                    <h4>${cloth.clothingType.clothingTypeName}</h4>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="empty-state">
                                            <div class="empty-icon">👔</div>
                                            <h3>ยังไม่มีเสื้อผ้าในตู้ของคุณ</h3>
                                            <p>เริ่มต้นเพิ่มเสื้อผ้าในตู้ของคุณเพื่อค้นหาสีที่เหมาะกับคุณ</p>
                                            <a href="${pageContext.request.contextPath}/add-item" class="btn btn-primary">เพิ่มเสื้อผ้า</a>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Right Column: Results -->
                <div class="right-column">
                    <div class="panel">
                        <h2 class="panel-title">ผลการจับคู่</h2>

                        <div class="matching-controls">
                            <label class="switch-container">
                                <input type="checkbox" id="include-jacket-toggle">
                                <span class="switch-slider"></span>
                                <span class="switch-label">รวมเสื้อคลุมด้วย</span>
                            </label>
                        </div>

                        <div class="matching-results">
                            <div id="jacket-result" class="result-item" style="display: none;">
                                <label class="result-label">เสื้อคลุม:</label>
                                <div class="result-with-nav">
                                    <button class="nav-btn nav-prev" id="jackets-nav-prev" style="display: none;" aria-label="Previous">←</button>
                                    <div class="result-slot large" id="jacket-slot">
                                        <div class="slot-placeholder">เลือกเสื้อคลุมก่อน</div>
                                    </div>
                                    <button class="nav-btn nav-next" id="jackets-nav-next" style="display: none;" aria-label="Next">→</button>
                                </div>
                                <div class="result-counter" id="jackets-counter" style="display: none;">0 / 0</div>
                            </div>

                            <div class="result-item">
                                <label class="result-label">เสื้อ:</label>
                                <div class="result-with-nav">
                                    <button class="nav-btn nav-prev" id="tops-nav-prev" style="display: none;" aria-label="Previous">←</button>
                                    <div class="result-slot large" id="top-result-slot">
                                        <div class="slot-placeholder">เลือกเสื้อก่อน</div>
                                    </div>
                                    <button class="nav-btn nav-next" id="tops-nav-next" style="display: none;" aria-label="Next">→</button>
                                </div>
                                <div class="result-counter" id="tops-counter" style="display: none;">0 / 0</div>
                            </div>

                            <div class="result-item">
                                <label class="result-label">กางเกง:</label>
                                <div class="result-with-nav">
                                    <button class="nav-btn nav-prev" id="bottoms-nav-prev" style="display: none;" aria-label="Previous">←</button>
                                    <div class="result-slot large" id="bottom-result-slot">
                                        <div class="slot-placeholder">เลือกกางเกงก่อน</div>
                                    </div>
                                    <button class="nav-btn nav-next" id="bottoms-nav-next" style="display: none;" aria-label="Next">→</button>
                                </div>
                                <div class="result-counter" id="bottoms-counter" style="display: none;">0 / 0</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Error Popup -->
        <div id="error-popup" class="popup-overlay">
            <div class="popup-content">
                <p id="error-popup-message"></p>
                <button id="error-popup-close" class="btn">ปิด</button>
            </div>
        </div>
    </main>

    <%@ include file="includes/footer.jsp" %>
    <script src="${pageContext.request.contextPath}/static/js/matching-cloth.js"></script>
</body>

</html>