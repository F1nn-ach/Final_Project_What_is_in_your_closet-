<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Register - What is in your closet?</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/register.css">
</head>

<body>
  <!-- Header with navigation -->
  <%@ include file="includes/header.jsp" %>

  <main>
    <div class="register-card">
      <div class="card-header">
        <div class="card-logo">สมัครสมาชิก</div>
        <div class="card-subtitle">สมัครสมาชิกเพื่อค้นหาสีมงคลประจำวันของคุณ</div>
      </div>

      <form action="${pageContext.request.contextPath}/user/register" method="post" id="registerForm">
        <div class="form-group">
          <label for="username">ชื่อผู้ใช้</label>
          <input type="text" id="username" name="username" placeholder="กรอกชื่อผู้ใช้ที่ต้องการ">
          <div id="usernameError" class="error"></div>
        </div>

        <div class="form-group">
          <label for="password">รหัสผ่าน</label>
          <input type="password" id="password" name="password" placeholder="กรอกรหัสผ่านของคุณ">

          <div class="password-strength">
            <div class="strength-meter"></div>
          </div>
          <div class="strength-text"></div>
          <div id="passwordError" class="error"></div>
        </div>

        <div class="form-group">
          <label for="confirmPassword">ยืนยันรหัสผ่าน</label>
          <input type="password" id="confirmPassword" name="confirmPassword" placeholder="ยืนยันรหัสผ่านของคุณ">
          <div id="confirmPasswordError" class="error"></div>
        </div>

        <div class="form-group">
          <label for="email">อีเมล</label>
          <input type="email" id="email" name="email" placeholder="example@email.com">
          <div id="emailError" class="error"></div>
        </div>

        <div class="form-group">
          <label for="phone">เบอร์โทรศัพท์</label>
          <input type="tel" id="phone" name="phone" placeholder="0xx-xxx-xxxx">
          <div id="phoneError" class="error"></div>
        </div>

        <div class="checkbox-group">
          <div class="checkbox-flex">
            <input type="checkbox" id="terms" name="terms">
            <label for="terms">
              ฉันยอมรับ <a href="#" class="auth-link">เงื่อนไขการใช้งาน</a> และ <a href="#" class="auth-link">นโยบายความเป็นส่วนตัว</a> ของเว็บไซต์
            </label>
          </div>
          <div id="termsError" class="error"></div>
        </div>

        <button type="submit" class="btn btn-primary">สมัครสมาชิก</button>

        <div class="auth-links">
          มีบัญชีอยู่แล้ว? <a href="${pageContext.request.contextPath}/user-login" class="auth-link">เข้าสู่ระบบ</a>
        </div>
      </form>
    </div>
  </main>
  
  <!-- Footer -->
  <%@ include file="includes/footer.jsp" %>

  <script src="${pageContext.request.contextPath}/static/js/register.js"></script>
</body>

</html>