let contextPath = '';
let imageBaseUrl = '';

document.addEventListener('DOMContentLoaded', function () {
    setTimeout(function() {
        initializeNotices();

        const clothItems = document.querySelectorAll('.cloth-item');
        const filterSection = document.querySelector('.filter-section');

        if (clothItems.length > 0 && filterSection) {
            initializeStaggeredAnimations();
            initializeHybridColorFiltering();
            initializeClothTypeFiltering();
            initializeDeleteFunctionality();
            initializeScrollButtons();
            initializeParallaxEffects();
            initializeImageModal();
        }
    }, 100);
});

function setContextPath(path) {
    contextPath = path;
}

function setImageBaseUrl(url) {
    imageBaseUrl = url;
}

function initializeStaggeredAnimations() {
    const clothItems = document.querySelectorAll('.cloth-item');
    const colorCircles = document.querySelectorAll('.color-circle');
    const clothTypeButtons = document.querySelectorAll('.cloth-type-btn');

    clothItems.forEach((item, index) => {
        item.style.setProperty('--item-index', index);
        item.style.animationDelay = `${0.6 + (index * 0.1)}s`;
    });

    colorCircles.forEach((circle, index) => {
        circle.style.animationDelay = `${0.4 + (index * 0.05)}s`;
        circle.classList.add('animate-in');
    });

    clothTypeButtons.forEach((button, index) => {
        button.style.animationDelay = `${0.5 + (index * 0.1)}s`;
        button.classList.add('animate-in');
    });
}

function initializeParallaxEffects() {
    let ticking = false;

    function updateParallax() {
        const scrolled = window.pageYOffset;
        const parallaxElements = document.querySelectorAll('.category-section');

        parallaxElements.forEach((element, index) => {
            const speed = 0.1 + (index * 0.05);
            const yPos = -(scrolled * speed);
            element.style.transform = `translateY(${yPos}px)`;
        });

        ticking = false;
    }

    function requestTick() {
        if (!ticking) {
            requestAnimationFrame(updateParallax);
            ticking = true;
        }
    }

    window.addEventListener('scroll', requestTick, { passive: true });
}

let globalFilterState = {
    selectedColorId: null,
    activeTypeIds: {
        upper: [],
        lower: []
    }
};

// Debounce utility function
let debounceTimer = null;
function debounce(func, delay = 300) {
    return function(...args) {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => func.apply(this, args), delay);
    };
}

function initializeHybridColorFiltering() {
    const colorCircles = document.querySelectorAll('.color-circle');
    const clothItems = document.querySelectorAll('.cloth-item');

    if (colorCircles.length === 0) {
        return;
    }

    const usedColorIds = new Set();
    clothItems.forEach(item => {
        const dominantColorId = item.getAttribute('data-dominant-color-id');
        if (dominantColorId && dominantColorId !== '') {
            usedColorIds.add(dominantColorId);
        }
    });

    colorCircles.forEach((circle) => {
        const colorId = circle.getAttribute('data-color-id');

        if (!usedColorIds.has(colorId)) {
            circle.style.display = 'none';
            return;
        }

        circle.addEventListener('click', async function(e) {
            e.preventDefault();
            e.stopPropagation();
            await handleHybridColorClick(this);
        });

        circle.addEventListener('mouseenter', function() {
            this.style.opacity = '0.8';
        });

        circle.addEventListener('mouseleave', function() {
            this.style.opacity = '1';
        });
    });
}

async function handleHybridColorClick(element) {
    const colorId = element.getAttribute('data-color-id');

    if (globalFilterState.selectedColorId === colorId) {
        globalFilterState.selectedColorId = null;
        element.classList.remove('selected');
    } else {
        document.querySelectorAll('.color-circle.selected').forEach(circle => {
            circle.classList.remove('selected');
        });
        globalFilterState.selectedColorId = colorId;
        element.classList.add('selected');
    }

    // Call fetch directly - debounce not needed for single click
    await fetchFilteredClothes();
}

async function fetchFilteredClothes() {
    try {
        showLoadingIndicator();

        const params = new URLSearchParams();

        // Add color filter
        if (globalFilterState.selectedColorId) {
            params.append('colorId', globalFilterState.selectedColorId);
        }

        // Add type filters for both upper and lower categories
        const allActiveTypeIds = [
            ...globalFilterState.activeTypeIds.upper,
            ...globalFilterState.activeTypeIds.lower
        ];

        if (allActiveTypeIds.length > 0) {
            allActiveTypeIds.forEach(typeId => {
                params.append('typeIds', typeId);
            });
        }

        const response = await fetch(`${contextPath}/api/cloth/filter?${params}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to filter clothes');
        }

        const data = await response.json();

        if (data.success) {
            updateClothesDisplay(data.clothes);
        } else {
            showErrorMessage(data.message || 'เกิดข้อผิดพลาดในการกรองข้อมูล');
        }

        hideLoadingIndicator();

    } catch (error) {
        console.error('Error filtering clothes:', error);
        hideLoadingIndicator();
        showErrorMessage('เกิดข้อผิดพลาดในการเชื่อมต่อกับเซิร์ฟเวอร์');
    }
}

function updateClothesDisplay(clothes) {
    const upperClothes = clothes.filter(c =>
        c.typeName === 'เสื้อ' || c.typeName === 'เสื้อคลุม'
    );
    const lowerClothes = clothes.filter(c =>
        c.typeName === 'กางเกง' || c.typeName === 'กระโปรง'
    );

    updateCategorySection('upper', upperClothes);
    updateCategorySection('lower', lowerClothes);

    if (clothes.length === 0) {
        toggleEmptyState(true);
    } else {
        toggleEmptyState(false);
    }

    updateTypeButtonsVisibility(upperClothes, lowerClothes);
}

function updateCategorySection(category, clothes) {
    const clothesRow = document.querySelector(
        `[data-category-group="${category}"] .clothes-row`
    );

    if (!clothesRow) return;

    clothesRow.innerHTML = '';

    clothes.forEach(cloth => {
        const clothItem = createClothItemElement(cloth);
        clothesRow.appendChild(clothItem);
    });
}

function createClothItemElement(cloth) {
    const div = document.createElement('div');
    div.className = 'cloth-item';
    div.setAttribute('data-cloth-id', cloth.clothId);
    div.setAttribute('data-clothing-type-id', cloth.typeId);
    div.setAttribute('data-dominant-color-id', cloth.dominantColorId || '');
    div.setAttribute('data-color-name', cloth.dominantColorName || '');

    div.innerHTML = `
        <div class="cloth-image">
            <button class="delete-btn" data-cloth-id="${cloth.clothId}">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash" viewBox="0 0 16 16">
                    <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                    <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H9.5a1 1 0 0 1 1 1v1H14a1 1 0 0 1 1 1v1zM2.5 3V2h11v1h-11z"/>
                </svg>
            </button>
            <img src="${cloth.clothImage}" alt="${cloth.typeName}" />
        </div>
    `;

    return div;
}

function updateTypeButtonsVisibility(upperClothes, lowerClothes) {
    const upperSection = document.querySelector('[data-category-group="upper"]');
    const lowerSection = document.querySelector('[data-category-group="lower"]');

    // Get available type IDs from server response
    const upperTypeIds = new Set(upperClothes.map(c => c.typeId.toString()));
    const lowerTypeIds = new Set(lowerClothes.map(c => c.typeId.toString()));

    if (upperSection) {
        const upperButtons = upperSection.querySelectorAll('.cloth-type-btn');
        upperButtons.forEach(btn => {
            const typeId = btn.getAttribute('data-type-id');

            // If no color filter is active, show all buttons
            if (!globalFilterState.selectedColorId) {
                btn.style.display = 'inline-block';
                btn.style.opacity = '1';
                btn.disabled = false;
            } else {
                // Color filter is active - only show types that have items with that color
                if (upperTypeIds.has(typeId)) {
                    btn.style.display = 'inline-block';
                    btn.style.opacity = '1';
                    btn.disabled = false;
                } else {
                    // Hide buttons for types that don't have items with the selected color
                    btn.style.display = 'none';
                }
            }
        });
    }

    if (lowerSection) {
        const lowerButtons = lowerSection.querySelectorAll('.cloth-type-btn');
        lowerButtons.forEach(btn => {
            const typeId = btn.getAttribute('data-type-id');

            // If no color filter is active, show all buttons
            if (!globalFilterState.selectedColorId) {
                btn.style.display = 'inline-block';
                btn.style.opacity = '1';
                btn.disabled = false;
            } else {
                // Color filter is active - only show types that have items with that color
                if (lowerTypeIds.has(typeId)) {
                    btn.style.display = 'inline-block';
                    btn.style.opacity = '1';
                    btn.disabled = false;
                } else {
                    // Hide buttons for types that don't have items with the selected color
                    btn.style.display = 'none';
                }
            }
        });
    }

    initializeDeleteFunctionality();
    initializeImageModal();
}

let loadingCount = 0; // Track multiple simultaneous loading operations

function showLoadingIndicator() {
    loadingCount++;

    let indicator = document.getElementById('loading-indicator');
    if (!indicator) {
        indicator = document.createElement('div');
        indicator.id = 'loading-indicator';
        indicator.className = 'loading-indicator';
        indicator.innerHTML = `
            <div class="loading-backdrop"></div>
            <div class="loading-content">
                <div class="loading-spinner"></div>
                <p class="loading-text">กำลังกรองข้อมูล...</p>
            </div>
        `;
        document.body.appendChild(indicator);

        // Add smooth fade-in after a small delay to avoid flicker on fast responses
        setTimeout(() => {
            if (indicator && loadingCount > 0) {
                indicator.classList.add('show');
            }
        }, 100);
    } else {
        indicator.classList.add('show');
    }
}

function hideLoadingIndicator() {
    loadingCount = Math.max(0, loadingCount - 1);

    if (loadingCount === 0) {
        const indicator = document.getElementById('loading-indicator');
        if (indicator) {
            indicator.classList.remove('show');
            setTimeout(() => {
                if (indicator.parentNode && loadingCount === 0) {
                    indicator.remove();
                }
            }, 300);
        }
    }
}

function showErrorMessage(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'notice notice-error';
    errorDiv.innerHTML = `
        <div class="notice-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" y1="9" x2="9" y2="15"/>
                <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
        </div>
        <div class="notice-content">
            <h4 class="notice-title">เกิดข้อผิดพลาด!</h4>
            <p class="notice-message">${message}</p>
        </div>
    `;

    const content = document.querySelector('.content');
    if (content) {
        content.insertBefore(errorDiv, content.firstChild);
        setTimeout(() => errorDiv.classList.add('show'), 100);
        setTimeout(() => {
            errorDiv.classList.remove('show');
            setTimeout(() => errorDiv.remove(), 300);
        }, 5000);
    }
}

function toggleEmptyState(show) {
    const emptyState = document.querySelector('.empty-state');
    const categorySections = document.querySelectorAll('.category-section');

    if (show) {
        if (emptyState) emptyState.style.display = 'block';
        categorySections.forEach(section => {
            section.style.display = 'none';
        });
    } else {
        if (emptyState) emptyState.style.display = 'none';
        categorySections.forEach(section => {
            section.style.display = 'block';
        });
    }
}

function initializeClothTypeFiltering() {
    const clothTypeButtons = document.querySelectorAll('.cloth-type-btn');

    // Create debounced version of fetchFilteredClothes
    const debouncedFetch = debounce(fetchFilteredClothes, 300);

    clothTypeButtons.forEach(button => {
        button.addEventListener('click', function () {
            const typeId = this.getAttribute('data-type-id');
            const categoryGroup = this.closest('.category-section').getAttribute('data-category-group');

            this.classList.toggle('active');

            const isActive = this.classList.contains('active');
            if (isActive) {
                if (!globalFilterState.activeTypeIds[categoryGroup].includes(typeId)) {
                    globalFilterState.activeTypeIds[categoryGroup].push(typeId);
                }
            } else {
                const index = globalFilterState.activeTypeIds[categoryGroup].indexOf(typeId);
                if (index > -1) {
                    globalFilterState.activeTypeIds[categoryGroup].splice(index, 1);
                }
            }

            // Use hybrid approach: fetch from server instead of client-side filtering
            debouncedFetch();
        });
    });
}

// This function has been removed as filtering is now done on the server-side
// Type filtering is handled by fetchFilteredClothes() which sends requests to the backend

function initializeDeleteFunctionality() {
    const deleteButtons = document.querySelectorAll('.delete-btn');
    const deleteForm = document.getElementById('deleteForm');
    const deleteClothIdInput = document.getElementById('deleteClothId');
    const confirmationPopup = document.getElementById('confirmationPopup');
    const cancelButton = document.getElementById('cancelDelete');
    const confirmButton = document.getElementById('confirmDelete');

    let currentClothId = null;

    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.stopPropagation();
            e.preventDefault();

            currentClothId = this.getAttribute('data-cloth-id');
            showConfirmationPopup();
        });
    });

    if (cancelButton) {
        cancelButton.addEventListener('click', function() {
            hideConfirmationPopup();
            currentClothId = null;
        });
    }

    if (confirmButton) {
        confirmButton.addEventListener('click', function() {
            if (currentClothId && deleteForm && deleteClothIdInput) {
                deleteClothIdInput.value = currentClothId;
                deleteForm.submit();
            } else {
                hideConfirmationPopup();
            }
        });
    }

    if (confirmationPopup) {
        confirmationPopup.addEventListener('click', function(e) {
            if (e.target === confirmationPopup) {
                hideConfirmationPopup();
                currentClothId = null;
            }
        });
    }

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && confirmationPopup && confirmationPopup.classList.contains('show')) {
            hideConfirmationPopup();
            currentClothId = null;
        }
    });

    function showConfirmationPopup() {
        if (confirmationPopup) {
            confirmationPopup.style.display = 'flex';
            confirmationPopup.offsetHeight;
            confirmationPopup.classList.add('show');

            if (cancelButton) {
                setTimeout(() => cancelButton.focus(), 100);
            }
        }
    }

    function hideConfirmationPopup() {
        if (confirmationPopup) {
            confirmationPopup.classList.remove('show');
            setTimeout(() => {
                confirmationPopup.style.display = 'none';
            }, 300);
        }
    }
}

function initializeNotices() {
    const notices = document.querySelectorAll('.notice');

    notices.forEach(notice => {
        setTimeout(() => {
            notice.classList.add('show');
        }, 100);

        notice.addEventListener('click', function() {
            dismissNotice(this);
        });

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

function initializeScrollButtons() {
    const clothesRows = document.querySelectorAll('.clothes-row');

    if (clothesRows.length === 0) {
        return;
    }

    clothesRows.forEach((clothesRow, index) => {
        const categorySection = clothesRow.closest('.category-section');
        const clothItems = clothesRow.querySelectorAll('.cloth-item');

        if (clothItems.length === 0) {
            return;
        }

        const scrollButtonsContainer = document.createElement('div');
        scrollButtonsContainer.className = 'scroll-buttons-container';

        const leftButton = document.createElement('button');
        leftButton.className = 'scroll-btn scroll-btn-left';
        leftButton.innerHTML = `
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="15,18 9,12 15,6"></polyline>
            </svg>
        `;

        const rightButton = document.createElement('button');
        rightButton.className = 'scroll-btn scroll-btn-right';
        rightButton.innerHTML = `
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9,18 15,12 9,6"></polyline>
            </svg>
        `;

        scrollButtonsContainer.appendChild(leftButton);
        scrollButtonsContainer.appendChild(rightButton);

        clothesRow.parentNode.insertBefore(scrollButtonsContainer, clothesRow);

        leftButton.addEventListener('click', () => {
            clothesRow.scrollBy({ left: -200, behavior: 'smooth' });
        });

        rightButton.addEventListener('click', () => {
            clothesRow.scrollBy({ left: 200, behavior: 'smooth' });
        });

        function updateScrollButtons() {
            const scrollLeft = clothesRow.scrollLeft;
            const maxScroll = clothesRow.scrollWidth - clothesRow.clientWidth;

            leftButton.style.opacity = scrollLeft > 0 ? '1' : '0.3';
            rightButton.style.opacity = scrollLeft < maxScroll ? '1' : '0.3';

            leftButton.disabled = scrollLeft <= 0;
            rightButton.disabled = scrollLeft >= maxScroll;

            updateScrollIndicators();
        }

        function updateScrollIndicators() {
            const scrollLeft = clothesRow.scrollLeft;
            const maxScroll = clothesRow.scrollWidth - clothesRow.clientWidth;

            if (maxScroll > 0) {
                const scrollPercentage = (scrollLeft / maxScroll) * 100;

                const progressBar = categorySection.querySelector('.scroll-progress');
                if (progressBar) {
                    progressBar.style.width = `${scrollPercentage}%`;
                }

                const fadeLeft = categorySection.querySelector('.fade-left');
                const fadeRight = categorySection.querySelector('.fade-right');

                if (fadeLeft) {
                    fadeLeft.style.opacity = scrollLeft > 10 ? '1' : '0';
                }
                if (fadeRight) {
                    fadeRight.style.opacity = scrollLeft < maxScroll - 10 ? '1' : '0';
                }
            }
        }

        const itemCount = clothItems.length;

        if (itemCount === 0) {
            const categoryGroup = categorySection.getAttribute('data-category-group');
            const categoryName = categoryGroup === 'upper' ? 'เสื้อผ้าส่วนบน' : 'เสื้อผ้าส่วนล่าง';
            const emptyIcon = categoryGroup === 'upper' ? '👕' : '👖';

            const emptyState = document.createElement('div');
            emptyState.className = 'empty-category';
            emptyState.innerHTML = `
                <div class="empty-category-icon">${emptyIcon}</div>
                <div class="empty-category-text">ยังไม่มี${categoryName}</div>
                <div class="empty-category-subtext">เพิ่มเสื้อผ้าใหม่เพื่อเริ่มต้นสร้างตู้เสื้อผ้าของคุณ</div>
            `;
            clothesRow.replaceWith(emptyState);
            return;
        }

        if (itemCount > 0) {
            const progressContainer = document.createElement('div');
            progressContainer.className = 'scroll-progress-container';
            progressContainer.innerHTML = `
                <div class="scroll-progress"></div>
            `;

            const itemsCount = document.createElement('div');
            itemsCount.className = 'items-count';
            itemsCount.textContent = `${itemCount} ชิ้น`;

            const fadeLeft = document.createElement('div');
            fadeLeft.className = 'fade-left';
            const fadeRight = document.createElement('div');
            fadeRight.className = 'fade-right';

            clothesRow.parentNode.insertBefore(progressContainer, clothesRow);
            categorySection.appendChild(itemsCount);
            categorySection.appendChild(fadeLeft);
            categorySection.appendChild(fadeRight);

            const hasOverflow = clothesRow.scrollWidth > clothesRow.clientWidth;
            if (!hasOverflow) {
                progressContainer.style.display = 'none';
            }
        }

        clothesRow.addEventListener('scroll', updateScrollButtons);

        let isDown = false;
        let startX;
        let scrollLeftStart;

        clothesRow.addEventListener('mousedown', (e) => {
            isDown = true;
            startX = e.pageX - clothesRow.offsetLeft;
            scrollLeftStart = clothesRow.scrollLeft;
        });

        clothesRow.addEventListener('mouseleave', () => {
            isDown = false;
        });

        clothesRow.addEventListener('mouseup', () => {
            isDown = false;
        });

        clothesRow.addEventListener('mousemove', (e) => {
            if (!isDown) return;
            e.preventDefault();
            const x = e.pageX - clothesRow.offsetLeft;
            const walk = (x - startX) * 2;
            clothesRow.scrollLeft = scrollLeftStart - walk;
        });

        leftButton.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                leftButton.click();
            }
        });

        rightButton.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                rightButton.click();
            }
        });

        setTimeout(() => {
            updateScrollButtons();
            updateScrollIndicators();
        }, 200);
    });
}

function initializeImageModal() {
    const imageModal = document.getElementById('imageModal');
    const modalImage = document.getElementById('modalImage');
    const clothImages = document.querySelectorAll('.cloth-image img');

    if (!imageModal || !modalImage) return;

    clothImages.forEach(img => {
        img.addEventListener('click', function(e) {
            e.stopPropagation();
            modalImage.src = this.src;
            imageModal.classList.add('show');
        });
    });

    imageModal.addEventListener('click', function() {
        this.classList.remove('show');
    });

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && imageModal.classList.contains('show')) {
            imageModal.classList.remove('show');
        }
    });
}
