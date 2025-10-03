document.addEventListener('DOMContentLoaded', function () {
    // Initialize notices first
    initializeNotices();

    const navLinks = document.querySelectorAll('a[href^="#"]');
    navLinks.forEach(link => {
        link.addEventListener('click', function (e) {
            e.preventDefault();
            const targetId = this.getAttribute('href').substring(1);
            const targetSection = document.getElementById(targetId);
            if (targetSection) {
                targetSection.scrollIntoView({
                    behavior: 'smooth'
                });
            }
        });
    });

    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver(function (entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);

    // Observe feature cards and step cards
    const animateElements = document.querySelectorAll('.feature-card, .step-card');
    animateElements.forEach(element => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(20px)';
        element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(element);
    });
});

function initializeNotices() {
    const notices = document.querySelectorAll('.notice');

    notices.forEach(notice => {
        // Show the notice with animation
        setTimeout(() => {
            notice.classList.add('show');
        }, 100);

        // Add click to dismiss
        notice.addEventListener('click', function() {
            dismissNotice(this);
        });

        // Add dismiss button
        const dismissBtn = document.createElement('button');
        dismissBtn.innerHTML = '×';
        dismissBtn.className = 'notice-dismiss';
        dismissBtn.style.cssText = `
            position: absolute;
            top: 8px;
            right: 12px;
            background: none;
            border: none;
            font-size: 18px;
            line-height: 1;
            cursor: pointer;
            color: currentColor;
            opacity: 0.6;
            padding: 4px;
            width: 24px;
            height: 24px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            transition: all 0.2s ease;
        `;

        dismissBtn.addEventListener('mouseenter', function() {
            this.style.opacity = '1';
            this.style.backgroundColor = 'rgba(0, 0, 0, 0.1)';
        });

        dismissBtn.addEventListener('mouseleave', function() {
            this.style.opacity = '0.6';
            this.style.backgroundColor = 'transparent';
        });

        dismissBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            const notice = this.closest('.notice');
            dismissNotice(notice);
        });

        notice.appendChild(dismissBtn);
    });

    function dismissNotice(notice) {
        notice.classList.remove('show');
        notice.classList.add('hide');
        setTimeout(() => {
            if (notice.parentNode) {
                notice.parentNode.removeChild(notice);
            }
        }, 300);
    }
}