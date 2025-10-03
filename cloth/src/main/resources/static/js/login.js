function showError(id, msg) {
    document.getElementById(id).innerText = msg;
}

document.getElementById("username").addEventListener("blur", function () {
    let username = this.value.trim();
    showError("usernameError", username === "" ? "กรุณากรอกชื่อผู้ใช้" : "");
});

document.getElementById("password").addEventListener("blur", function () {
    let password = this.value.trim();
    showError("passwordError", password === "" ? "กรุณากรอกรหัสผ่าน" : "");
});

document.getElementById("loginForm").addEventListener("submit", function (e) {
    e.preventDefault();
    ["username", "password"].forEach(id => {
        document.getElementById(id).dispatchEvent(new Event("blur"));
    });

    let errors = ["usernameError", "passwordError"];
    let hasError = errors.some(id => document.getElementById(id).innerText !== "");
    if (!hasError) {
        this.submit();
    }
});