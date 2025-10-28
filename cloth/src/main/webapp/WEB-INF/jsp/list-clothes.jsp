<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>รายการเสื้อผ้า - What is in your closet?</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/list-clothes.css">
</head>

<body>
  <%@ include file="includes/header.jsp" %>

  <main>
    <div class="content">
      <div class="page-header">
        <h1 class="page-title">รายการเสื้อผ้าของคุณ</h1>
        <a href="${pageContext.request.contextPath}/cloth/add" class="add-btn">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          เพิ่มเสื้อผ้า
        </a>
      </div>

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

      <c:choose>
        <c:when test="${empty clothes}">
          <!-- Empty State -->
          <div class="empty-state-container">
            <div class="empty-state-card">
              <div class="empty-state-icon">👔</div>
              <h3 class="empty-title">ยังไม่มีเสื้อผ้าในตู้ของคุณ</h3>
              <p class="empty-desc">เริ่มต้นเพิ่มเสื้อผ้าในตู้ของคุณเพื่อค้นหาสีที่เหมาะกับคุณ</p>
              <a href="${pageContext.request.contextPath}/cloth/add" class="add-btn">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="12" y1="5" x2="12" y2="19"></line>
                  <line x1="5" y1="12" x2="19" y2="12"></line>
                </svg>
                เพิ่มเสื้อผ้า
              </a>
            </div>
          </div>
        </c:when>
        <c:otherwise>

      <div class="filter-section">
        <h2 class="filter-title">กรองตามสี</h2>
        <div class="color-palette">
          <c:forEach var="color" items="${colorCategories}">
            <div class="color-circle"
                 data-color-id="${color.colorCategoryId}"
                 style="background-color: ${color.colorCategoryHex};"
                 title="${color.colorCategoryName}">
            </div>
          </c:forEach>
        </div>
        <div class="filter-tags"></div>

        

        <div class="category-section" data-category-group="upper">
          <h2 class="category-title">👕 เสื้อผ้าส่วนบน (Tops)</h2>
          <div class="cloth-type-buttons" id="upper-cloth-type-buttons">
            <c:forEach var="clothingType" items="${clothingTypes}">
              <c:if test="${clothingType.clothingTypeName == 'เสื้อ' || clothingType.clothingTypeName == 'เสื้อคลุม'}">
                <button class="cloth-type-btn" data-type-id="${clothingType.clothingTypeId}">${clothingType.clothingTypeName}</button>
              </c:if>
            </c:forEach>
          </div>
          <div class="clothes-row" id="upper-clothes-row">
            <c:forEach var="cloth" items="${clothes}">
              <c:if test="${cloth.clothingType.clothingTypeName == 'เสื้อ' || cloth.clothingType.clothingTypeName == 'เสื้อคลุม'}">
                <div class="cloth-item"
                     data-cloth-id="${cloth.clothingId}"
                     data-clothing-type-id="${cloth.clothingType.clothingTypeId}"
                     data-dominant-color-id="${cloth.dominantColor != null ? cloth.dominantColor.colorCategoryId : ''}"
                     data-color-name="${cloth.dominantColor != null ? cloth.dominantColor.colorCategoryName : ''}">
                  <div class="cloth-image">
                    <button class="delete-btn" data-cloth-id="${cloth.clothingId}">
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash" viewBox="0 0 16 16">
                        <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                        <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H9.5a1 1 0 0 1 1 1v1H14a1 1 0 0 1 1 1v1zM2.5 3V2h11v1h-11z"/>
                      </svg>
                    </button>
                    <img src="${imageBaseUrl}${cloth.user.username}/${cloth.clothingImage}" alt="${cloth.clothingType.clothingTypeName}" />
                  </div>
                </div>
              </c:if>
            </c:forEach>
          </div>
        </div>

        <div class="category-section" data-category-group="lower">
          <h2 class="category-title">👖 เสื้อผ้าส่วนล่าง (Bottoms)</h2>
          <div class="cloth-type-buttons" id="lower-cloth-type-buttons">
            <c:forEach var="clothingType" items="${clothingTypes}">
              <c:if test="${clothingType.clothingTypeName == 'กางเกง' || clothingType.clothingTypeName == 'กระโปรง'}">
                <button class="cloth-type-btn" data-type-id="${clothingType.clothingTypeId}">${clothingType.clothingTypeName}</button>
              </c:if>
            </c:forEach>
          </div>
          <div class="clothes-row" id="lower-clothes-row">
            <c:forEach var="cloth" items="${clothes}">
              <c:if test="${cloth.clothingType.clothingTypeName == 'กางเกง' || cloth.clothingType.clothingTypeName == 'กระโปรง'}">
                <div class="cloth-item"
                     data-cloth-id="${cloth.clothingId}"
                     data-clothing-type-id="${cloth.clothingType.clothingTypeId}"
                     data-dominant-color-id="${cloth.dominantColor != null ? cloth.dominantColor.colorCategoryId : ''}"
                     data-color-name="${cloth.dominantColor != null ? cloth.dominantColor.colorCategoryName : ''}">
                  <div class="cloth-image">
                    <button class="delete-btn" data-cloth-id="${cloth.clothingId}">
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash" viewBox="0 0 16 16">
                        <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                        <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H9.5a1 1 0 0 1 1 1v1H14a1 1 0 0 1 1 1v1zM2.5 3V2h11v1h-11z"/>
                      </svg>
                    </button>
                    <img src="${imageBaseUrl}${cloth.user.username}/${cloth.clothingImage}" alt="${cloth.clothingType.clothingTypeName}" />
                  </div>
                </div>
              </c:if>
            </c:forEach>
          </div>
        </div>

        <div class="empty-state" style="display: none;">
          <h3 class="empty-title">ไม่พบเสื้อผ้าที่ตรงกับเงื่อนไขที่เลือก</h3>
          <p class="empty-desc">ลองเปลี่ยนเงื่อนไขการกรองหรือเพิ่มเสื้อผ้าใหม่</p>
          <a href="${pageContext.request.contextPath}/cloth/add" class="add-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
            เพิ่มเสื้อผ้า
          </a>
        </div>
      </div>
        </c:otherwise>
      </c:choose>
    </div>
  </main>

  <!-- Image Modal -->
  <div id="imageModal" class="image-modal">
    <img id="modalImage" src="" alt="Full size image" />
  </div>

  <!-- Custom Confirmation Popup -->
  <div id="confirmationPopup" class="popup-overlay" style="display: none;">
    <div class="popup-container">
      <div class="popup-header">
        <div class="popup-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="9" y1="9" x2="15" y2="15"/>
            <line x1="15" y1="9" x2="9" y2="15"/>
          </svg>
        </div>
        <h3 class="popup-title">ยืนยันการลบ</h3>
      </div>
      <div class="popup-content">
        <p class="popup-message">คุณแน่ใจที่จะลบเสื้อผ้าชิ้นนี้?</p>
        <p class="popup-description">การดำเนินการนี้ไม่สามารถยกเลิกได้</p>
      </div>
      <div class="popup-actions">
        <button type="button" class="popup-btn popup-btn-cancel" id="cancelDelete">ยกเลิก</button>
        <button type="button" class="popup-btn popup-btn-confirm" id="confirmDelete">ลบเสื้อผ้า</button>
      </div>
    </div>
  </div>

  <!-- Hidden form for delete operations -->
  <form id="deleteForm" action="${pageContext.request.contextPath}/cloth/delete" method="post" style="display: none;">
    <input type="hidden" id="deleteClothId" name="clothId" value="">
  </form>

  <%@ include file="includes/footer.jsp" %>
  <script>
    // Set context path and image base URL for JavaScript
    contextPath = '${pageContext.request.contextPath}';
    imageBaseUrl = '${imageBaseUrl}';
  </script>
  <script src="${pageContext.request.contextPath}/static/js/list-clothes.js"></script>
</body>
</html>