<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>หน้าแรก - What is in your closet?</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/home.css">
</head>

<body>
  <!-- Header with navigation -->
  <%@ include file="includes/header.jsp" %>

  <main>
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

    <section class="hero-section">
      <div class="hero-container">
        <div class="hero-content">
          <h1 class="hero-title">ค้นพบสีมงคลประจำวันของคุณ</h1>
          <p class="hero-subtitle">
            เลือกสีเสื้อผ้าให้เข้ากับโชคลาภประจำวันด้วยระบบจับคู่สีที่ฉลาด
            เพื่อชีวิตที่เป็นมงคลในทุกๆ วัน
          </p>
          <div class="hero-buttons">
            <c:choose>
              <c:when test="${not empty sessionScope.user}">
                <a href="${pageContext.request.contextPath}/api/matching/page" class="btn btn-primary">
                  เริ่มจับคู่สีเลย
                </a>
                <a href="${pageContext.request.contextPath}/cloth/list" class="btn btn-secondary">
                  เสื้อผ้าของฉัน
                </a>
              </c:when>
              <c:otherwise>
                <a href="${pageContext.request.contextPath}/registration" class="btn btn-primary">
                  เริ่มต้นใช้งาน
                </a>
                <a href="${pageContext.request.contextPath}/user-login" class="btn btn-secondary">
                  เข้าสู่ระบบ
                </a>
              </c:otherwise>
            </c:choose>
          </div>
        </div>
        <div class="hero-image">
          <div class="color-palette">
            <div class="color-circle" style="background: #ff6b6b;"></div>
            <div class="color-circle" style="background: #4ecdc4;"></div>
            <div class="color-circle" style="background: #45b7d1;"></div>
            <div class="color-circle" style="background: #f9ca24;"></div>
            <div class="color-circle" style="background: #6c5ce7;"></div>
            <div class="color-circle" style="background: #fd79a8;"></div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features-section">
      <div class="container">
        <h2 class="section-title">ทำไมต้องเลือกเรา?</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">🎨</div>
            <h3>จับคู่สีอัจฉริยะ</h3>
            <p>ระบบ AI ที่ช่วยเลือกสีเสื้อผ้าให้เข้ากับโชคลาภและบุคลิกของคุณ</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📅</div>
            <h3>สีมงคลประจำวัน</h3>
            <p>อัพเดทสีมงคลประจำวันตามหลักโหราศาสตร์และความเชื่อโบราณ</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">👔</div>
            <h3>จัดการตู้เสื้อผ้า</h3>
            <p>บันทึกและจัดการเสื้อผ้าในตู้ของคุณให้เป็นระเบียบ</p>
          </div>
        </div>
      </div>
    </section>

    <section class="today-color-section">
      <div class="container">
        <div class="today-color-card">
          <h2>สีมงคลวันนี้</h2>
          <div class="today-date">
            <script>
              document.write(new Date().toLocaleDateString('th-TH', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric'
              }));
            </script>
          </div>
          <div class="lucky-color-display">
            <c:choose>
              <c:when test="${not empty selectedItem}">
                <div class="lucky-color" style="background: ${selectedItem.colorCategory.colorCategoryHex}"></div>
                <div class="color-info">
                  <h3>สี${selectedItem.colorCategory.colorCategoryName}</h3>
                  <p>โห้โชคด้าน${selectedItem.luckyColor.luckyColorType.luckyColorTypeName}</p>
                </div>
              </c:when>
              <c:otherwise>
                <div class="lucky-color" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);"></div>
                <div class="color-info">
                  <h3>สีน้ำเงินอินดิโก</h3>
                  <p>สีแห่งความสงบและสติปัญญา เสริมดวงการงานและการเรียนรู้</p>
                </div>
              </c:otherwise>
            </c:choose>
          </div>

          <c:choose>
            <c:when test="${not empty sessionScope.user}">
              <a href="${pageContext.request.contextPath}/api/matching/page" class="btn btn-primary auth-link">
                ดูเสื้อผ้าที่เหมาะสม
              </a>
            </c:when>
            <c:otherwise>
              <p class="login-prompt">
                <a href="${pageContext.request.contextPath}/user-login" class="auth-link">เข้าสู่ระบบ</a>
                เพื่อดูเสื้อผ้าที่เหมาะสมกับสีมงคลวันนี้
              </p>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </section>

    <section class="how-it-works-section">
      <div class="container">
        <h2 class="section-title">วิธีการใช้งาน</h2>
        <div class="steps-grid">
          <div class="step-card">
            <div class="step-number">1</div>
            <h3>สมัครสมาชิก</h3>
            <p>สร้างบัญชีและใส่ข้อมูลส่วนตัวเพื่อรับคำแนะนำที่เหมาะสม</p>
          </div>
          <div class="step-card">
            <div class="step-number">2</div>
            <h3>เพิ่มเสื้อผ้า</h3>
            <p>ถ่ายรูปหรืออัพโหลดเสื้อผ้าในตู้ของคุณเข้าสู่ระบบ</p>
          </div>
          <div class="step-card">
            <div class="step-number">3</div>
            <h3>รับคำแนะนำ</h3>
            <p>ระบบจะแนะนำเสื้อผ้าที่เหมาะสมกับสีมงคลประจำวัน</p>
          </div>
        </div>
      </div>
    </section>
  </main>

  <!-- Footer -->
  <%@ include file="includes/footer.jsp" %>

  <script src="${pageContext.request.contextPath}/static/js/home.js"></script>
</body>

</html>