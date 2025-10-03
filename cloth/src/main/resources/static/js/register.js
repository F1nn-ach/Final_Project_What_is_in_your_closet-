function showError(id, msg) {
    document.getElementById(id).innerText = msg;
}

function debounce(fn, delay) {
    let timer;
    return function (...args) {
        clearTimeout(timer);
        timer = setTimeout(() => fn.apply(this, args), delay);
    };
}

async function checkExists(type, value) {
    try {
        let res = await fetch(`/check-${type}?${type}=${encodeURIComponent(value)}`);
        return await res.json();
    } catch (err) {
        console.error(err);
        return false;
    }
}

document.getElementById("username").addEventListener("blur", debounce(async function () {
    let username = this.value.trim();
    let regex = /^[a-zA-Z0-9_]{3,20}$/;
    if (!regex.test(username)) {
        showError("usernameError", "ชื่อผู้ใช้ 3-20 ตัวอักษร ใช้เฉพาะ a-z, 0-9, _");
    } else if (await checkExists("username", username)) {
        showError("usernameError", "ชื่อผู้ใช้นี้ถูกใช้แล้ว");
    } else {
        showError("usernameError", "");
    }
}, 300));

document.getElementById("password").addEventListener("blur", function () {
    let password = this.value.trim();
    showError("passwordError", password === "" ? "กรุณากรอกรหัสผ่าน" : "");
});

document.getElementById("confirmPassword").addEventListener("blur", function () {
    let password = document.getElementById("password").value.trim();
    let confirmPassword = this.value.trim();
    showError("confirmPasswordError", password !== confirmPassword ? "รหัสผ่านไม่ตรงกัน" : "");
});

document.getElementById("password").addEventListener('input', function () {
    const password = this.value;
    const confirmPasswordInput = document.getElementById("confirmPassword");
    const strengthMeter = document.querySelector('.strength-meter');
    const strengthText = document.querySelector('.strength-text');
    let strength = 0;

    if (password.length >= 8) strength += 1;
    if (password.match(/[0-9]+/)) strength += 1;
    if (password.match(/[a-z]+/)) strength += 1;
    if (password.match(/[A-Z]+/)) strength += 1;
    if (password.match(/[^a-zA-Z0-9]+/)) strength += 1;

    // Update strength meter
    strengthMeter.className = 'strength-meter';

    if (strength <= 2) {
        strengthMeter.classList.add('weak');
        strengthText.textContent = 'รหัสผ่านอ่อนแอ - ควรเพิ่มความซับซ้อน';
    } else if (strength <= 3) {
        strengthMeter.classList.add('medium');
        strengthText.textContent = 'รหัสผ่านปานกลาง - เพิ่มความซับซ้อนอีกนิด';
    } else {
        strengthMeter.classList.add('strong');
        strengthText.textContent = 'รหัสผ่านแข็งแรง!';
    }

    console.log(strengthText);

    if (confirmPasswordInput.value !== '') {
        showError("confirmPasswordError", password !== confirmPasswordInput.value.trim() ? "รหัสผ่านไม่ตรงกัน" : "");
    }
});

document.getElementById("email").addEventListener("blur", debounce(async function () {
    let email = this.value.trim();
    let regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!regex.test(email)) {
        showError("emailError", "กรุณากรอกอีเมลให้ถูกต้อง เช่น user@example.com");
    } else if (await checkExists("email", email)) {
        showError("emailError", "อีเมลนี้ถูกใช้แล้ว");
    } else {
        showError("emailError", "");
    }
}, 300));

document.getElementById("phone").addEventListener("blur", function () {
    let phone = this.value.trim();
    let regex = /^[0-9]{10}$/;
    showError("phoneError", !regex.test(phone) ? "เบอร์โทรศัพท์ต้องมี 10 หลัก" : "");
});

document.getElementById("terms").addEventListener("change", function () {
    showError("termsError", !this.checked ? "คุณต้องยอมรับเงื่อนไขการใช้งาน" : "");
});

document.getElementById("registerForm").addEventListener("submit", function (e) {
    e.preventDefault();

    ["username", "password", "confirmPassword", "email", "phone"].forEach(id => {
        document.getElementById(id).dispatchEvent(new Event("blur"));
    });
    document.getElementById("terms").dispatchEvent(new Event("change"));

    let errors = ["usernameError", "passwordError", "confirmPasswordError", "emailError", "phoneError", "termsError"];
    let hasError = errors.some(id => document.getElementById(id).innerText !== "");

    if (!hasError) {
        this.submit();
    }
});
