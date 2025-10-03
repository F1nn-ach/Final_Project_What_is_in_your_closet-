<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Login - What is in your closet?</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/login.css">
</head>

<body>
  <!-- Header with navigation -->
  <%@ include file="includes/header.jsp" %>

  <main>
    <div class="login-card">
      <div class="card-header">
        <div class="card-logo">เข้าสู่ระบบ</div>
        <div class="card-subtitle">เข้าสู่ระบบเพื่อดูสีมงคลประจำวันของคุณ</div>
      </div>

      <c:if test="${not empty error}">
        <div class="error-message-box">
          ${error}
        </div>
      </c:if>

      <form id="loginForm" action="${pageContext.request.contextPath}/login-check" method="post">
        <div class="form-group">
          <label for="username">ชื่อผู้ใช้ หรือ อีเมล</label>
          <input type="text" id="username" name="username" placeholder="กรอกอีเมลหรือชื่อผู้ใช้ของคุณ">
          <div id="usernameError" class="error"></div>
        </div>

        <div class="form-group">
          <label for="password">รหัสผ่าน</label>
          <input type="password" id="password" name="password" placeholder="กรอกรหัสผ่านของคุณ">
          <div id="passwordError" class="error"></div>
        </div>

        <div class="checkbox-group">
          <input type="checkbox" id="remember" name="rememberMe">
          <label for="remember">จดจำฉันไว้</label>
        </div>

        <button type="submit" class="btn btn-primary">เข้าสู่ระบบ</button>

        <div class="auth-links">
          <a href="#" class="auth-link">ลืมรหัสผ่าน?</a>
          <a href="${pageContext.request.contextPath}/registration" class="auth-link">สมัครสมาชิกใหม่</a>
        </div>
      </form>
    </div>
  </main>
  
  <!-- Footer -->
  <%@ include file="includes/footer.jsp" %>

  <script src="${pageContext.request.contextPath}/static/js/login.js"></script>
</body>

</html>