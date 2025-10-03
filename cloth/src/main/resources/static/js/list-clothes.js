document.addEventListener('DOMContentLoaded', function () {
    setTimeout(function() {
        // Always initialize notices
        initializeNotices();

        // Check if we have any clothes
        const clothItems = document.querySelectorAll('.cloth-item');
        const filterSection = document.querySelector('.filter-section');

        // Only initialize these functions if we have clothes
        if (clothItems.length > 0 && filterSection) {
            initializeStaggeredAnimations();
            initializeColorFiltering();
            initializeClothTypeFiltering();
            initializeDeleteFunctionality();
            initializeScrollButtons();
            initializeParallaxEffects();
            initializeImageModal();
        }
    }, 100);
});

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

// Global filter state
let globalFilterState = {
    selectedMainColorId: null,
    selectedSubColorId: null,
    activeTypeIds: {
        upper: [],
        lower: []
    }
};

function initializeColorFiltering() {
    const mainColorCircles = document.querySelectorAll('.main-color');
    const subColorCircles = document.querySelectorAll('.sub-color');
    const clothItems = document.querySelectorAll('.cloth-item');

    if (mainColorCircles.length === 0) {
        return;
    }

    mainColorCircles.forEach((circle) => {
        circle.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            handleMainColorClick(this);
        });

        circle.addEventListener('mouseenter', function() {
            this.style.opacity = '0.8';
        });

        circle.addEventListener('mouseleave', function() {
            this.style.opacity = '1';
        });
    });

    subColorCircles.forEach((circle) => {
        circle.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            handleSubColorClick(this);
        });

        circle.addEventListener('mouseenter', function() {
            if (this.style.display !== 'none') {
                this.style.opacity = '0.8';
            }
        });

        circle.addEventListener('mouseleave', function() {
            this.style.opacity = '1';
        });
    });

    function handleMainColorClick(element) {
        const mainColorId = element.getAttribute('data-main-color-id');

        if (globalFilterState.selectedMainColorId === mainColorId) {
            globalFilterState.selectedMainColorId = null;
            element.classList.remove('selected');
            hideSubColors();
            applyAllFilters();
        } else {
            clearMainColorSelection();
            globalFilterState.selectedMainColorId = mainColorId;
            globalFilterState.selectedSubColorId = null;
            element.classList.add('selected');
            showSubColors(mainColorId);
            applyAllFilters();
        }
    }

    function handleSubColorClick(element) {
        const subColorId = element.getAttribute('data-sub-color-id');

        if (globalFilterState.selectedSubColorId === subColorId) {
            globalFilterState.selectedSubColorId = null;
            element.classList.remove('selected');
            applyAllFilters();
        } else {
            clearSubColorSelection();
            globalFilterState.selectedSubColorId = subColorId;
            element.classList.add('selected');
            applyAllFilters();
        }
    }

    function clearMainColorSelection() {
        mainColorCircles.forEach(circle => {
            circle.classList.remove('selected');
        });
        clearSubColorSelection();
        hideSubColors();
    }

    function clearSubColorSelection() {
        subColorCircles.forEach(circle => {
            circle.classList.remove('selected');
        });
    }

    function showSubColors(mainColorId) {
        hideSubColors();
        subColorCircles.forEach(circle => {
            const circleMainColorId = circle.getAttribute('data-main-color-id');
            if (circleMainColorId === mainColorId) {
                circle.style.display = 'block';
                circle.style.visibility = 'visible';
                circle.style.pointerEvents = 'auto';
            }
        });
    }

    function hideSubColors() {
        subColorCircles.forEach(circle => {
            circle.style.display = 'none';
            circle.style.visibility = 'hidden';
            circle.style.pointerEvents = 'none';
            circle.classList.remove('selected');
        });
    }

    // Remove old individual filter functions - they're replaced by applyAllFilters
}

// Unified filtering function that combines color and type filters
function applyAllFilters() {
    const clothItems = document.querySelectorAll('.cloth-item');
    let totalVisibleCount = 0;

    // Filter items in each category separately
    ['upper', 'lower'].forEach(categoryGroup => {
        const categorySection = document.querySelector(`[data-category-group="${categoryGroup}"]`);
        if (!categorySection) return;

        const clothesRow = categorySection.querySelector('.clothes-row');
        if (!clothesRow) return;

        const itemsInCategory = clothesRow.querySelectorAll('.cloth-item');
        let visibleInCategory = 0;

        // Track which type IDs have items matching the color filter
        const availableTypeIds = new Set();

        itemsInCategory.forEach(item => {
            let shouldShow = true;

            // Apply color filter
            if (globalFilterState.selectedMainColorId) {
                const itemMainColorId = item.getAttribute('data-main-color-id');

                if (globalFilterState.selectedSubColorId) {
                    const itemSubColorId = item.getAttribute('data-sub-color-id');
                    shouldShow = itemSubColorId === globalFilterState.selectedSubColorId;
                } else {
                    shouldShow = itemMainColorId === globalFilterState.selectedMainColorId;
                }
            }

            // Track available types for color-filtered items
            if (shouldShow) {
                const clothTypeId = item.getAttribute('data-cloth-type-id');
                availableTypeIds.add(clothTypeId);
            }

            // Apply type filter (only if color filter passes or no color filter is set)
            if (shouldShow && globalFilterState.activeTypeIds[categoryGroup].length > 0) {
                const clothTypeId = item.getAttribute('data-cloth-type-id');
                shouldShow = globalFilterState.activeTypeIds[categoryGroup].includes(clothTypeId);
            }

            if (shouldShow) {
                item.style.display = 'block';
                visibleInCategory++;
                totalVisibleCount++;
            } else {
                item.style.display = 'none';
            }
        });

        // Hide/show cloth type buttons based on available types
        updateClothTypeButtons(categorySection, availableTypeIds);
    });

    toggleEmptyState(totalVisibleCount === 0);
}

// Function to update cloth type button visibility based on available types
function updateClothTypeButtons(categorySection, availableTypeIds) {
    const clothTypeButtons = categorySection.querySelectorAll('.cloth-type-btn');

    clothTypeButtons.forEach(button => {
        const typeId = button.getAttribute('data-type-id');

        // If color filter is active, only show buttons for types that have items with that color
        if (globalFilterState.selectedMainColorId || globalFilterState.selectedSubColorId) {
            if (availableTypeIds.has(typeId)) {
                button.style.display = 'inline-block';
            } else {
                button.style.display = 'none';
                // If the hidden button was active, deactivate it
                if (button.classList.contains('active')) {
                    button.classList.remove('active');
                    const categoryGroup = categorySection.getAttribute('data-category-group');
                    const index = globalFilterState.activeTypeIds[categoryGroup].indexOf(typeId);
                    if (index > -1) {
                        globalFilterState.activeTypeIds[categoryGroup].splice(index, 1);
                    }
                }
            }
        } else {
            // No color filter active, show all buttons
            button.style.display = 'inline-block';
        }
    });
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

// Function to reset all filters
function resetAllFilters() {
    // Reset global state
    globalFilterState.selectedMainColorId = null;
    globalFilterState.selectedSubColorId = null;
    globalFilterState.activeTypeIds.upper = [];
    globalFilterState.activeTypeIds.lower = [];

    // Clear color selections
    document.querySelectorAll('.main-color.selected').forEach(circle => {
        circle.classList.remove('selected');
    });
    document.querySelectorAll('.sub-color.selected').forEach(circle => {
        circle.classList.remove('selected');
    });
    document.querySelectorAll('.sub-color').forEach(circle => {
        circle.style.display = 'none';
    });

    // Clear type selections
    document.querySelectorAll('.cloth-type-btn.active').forEach(button => {
        button.classList.remove('active');
    });

    // Show all clothes
    applyAllFilters();
}

function initializeClothTypeFiltering() {
    const clothTypeButtons = document.querySelectorAll('.cloth-type-btn');

    clothTypeButtons.forEach(button => {
        button.addEventListener('click', function () {
            const typeId = this.getAttribute('data-type-id');
            const categoryGroup = this.closest('.category-section').getAttribute('data-category-group');

            this.classList.toggle('active');

            // Update global filter state
            const isActive = this.classList.contains('active');
            if (isActive) {
                // Add to active types
                if (!globalFilterState.activeTypeIds[categoryGroup].includes(typeId)) {
                    globalFilterState.activeTypeIds[categoryGroup].push(typeId);
                }
            } else {
                // Remove from active types
                const index = globalFilterState.activeTypeIds[categoryGroup].indexOf(typeId);
                if (index > -1) {
                    globalFilterState.activeTypeIds[categoryGroup].splice(index, 1);
                }
            }

            // Apply all filters
            applyAllFilters();
        });
    });
}

function initializeDeleteFunctionality() {
    const deleteButtons = document.querySelectorAll('.delete-btn');
    const deleteForm = document.getElementById('deleteForm');
    const deleteClothIdInput = document.getElementById('deleteClothId');
    const confirmationPopup = document.getElementById('confirmationPopup');
    const cancelButton = document.getElementById('cancelDelete');
    const confirmButton = document.getElementById('confirmDelete');

    let currentClothId = null;

    // Delete button click handlers
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.stopPropagation();
            e.preventDefault();

            currentClothId = this.getAttribute('data-cloth-id');
            showConfirmationPopup();
        });
    });

    // Cancel button handler
    if (cancelButton) {
        cancelButton.addEventListener('click', function() {
            hideConfirmationPopup();
            currentClothId = null;
        });
    }

    // Confirm button handler
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

    // Close popup when clicking outside
    if (confirmationPopup) {
        confirmationPopup.addEventListener('click', function(e) {
            if (e.target === confirmationPopup) {
                hideConfirmationPopup();
                currentClothId = null;
            }
        });
    }

    // ESC key to close popup
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && confirmationPopup && confirmationPopup.classList.contains('show')) {
            hideConfirmationPopup();
            currentClothId = null;
        }
    });

    function showConfirmationPopup() {
        if (confirmationPopup) {
            confirmationPopup.style.display = 'flex';
            // Force reflow for animation
            confirmationPopup.offsetHeight;
            confirmationPopup.classList.add('show');

            // Focus on cancel button for accessibility
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

function initializeScrollButtons() {
    const clothesRows = document.querySelectorAll('.clothes-row');

    if (clothesRows.length === 0) {
        return;
    }

    clothesRows.forEach((clothesRow, index) => {
        const categorySection = clothesRow.closest('.category-section');
        const clothItems = clothesRow.querySelectorAll('.cloth-item');

        // Only add scroll buttons if there are items and potential overflow
        if (clothItems.length === 0) {
            return;
        }

        // Create scroll buttons container
        const scrollButtonsContainer = document.createElement('div');
        scrollButtonsContainer.className = 'scroll-buttons-container';

        // Create left scroll button
        const leftButton = document.createElement('button');
        leftButton.className = 'scroll-btn scroll-btn-left';
        leftButton.innerHTML = `
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="15,18 9,12 15,6"></polyline>
            </svg>
        `;

        // Create right scroll button
        const rightButton = document.createElement('button');
        rightButton.className = 'scroll-btn scroll-btn-right';
        rightButton.innerHTML = `
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9,18 15,12 9,6"></polyline>
            </svg>
        `;

        scrollButtonsContainer.appendChild(leftButton);
        scrollButtonsContainer.appendChild(rightButton);

        // Insert scroll buttons before clothes row
        clothesRow.parentNode.insertBefore(scrollButtonsContainer, clothesRow);

        // Add scroll functionality
        leftButton.addEventListener('click', () => {
            clothesRow.scrollBy({ left: -200, behavior: 'smooth' });
        });

        rightButton.addEventListener('click', () => {
            clothesRow.scrollBy({ left: 200, behavior: 'smooth' });
        });

        // Update button visibility based on scroll position
        function updateScrollButtons() {
            const scrollLeft = clothesRow.scrollLeft;
            const maxScroll = clothesRow.scrollWidth - clothesRow.clientWidth;

            leftButton.style.opacity = scrollLeft > 0 ? '1' : '0.3';
            rightButton.style.opacity = scrollLeft < maxScroll ? '1' : '0.3';

            leftButton.disabled = scrollLeft <= 0;
            rightButton.disabled = scrollLeft >= maxScroll;

            // Update scroll indicators
            updateScrollIndicators();
        }

        // Add scroll progress indicator
        function updateScrollIndicators() {
            const scrollLeft = clothesRow.scrollLeft;
            const maxScroll = clothesRow.scrollWidth - clothesRow.clientWidth;

            if (maxScroll > 0) {
                const scrollPercentage = (scrollLeft / maxScroll) * 100;

                // Update progress bar if it exists
                const progressBar = categorySection.querySelector('.scroll-progress');
                if (progressBar) {
                    progressBar.style.width = `${scrollPercentage}%`;
                }

                // Update fade indicators
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

        // Create scroll progress bar (only if there are items to scroll)
        const itemCount = clothItems.length;

        if (itemCount === 0) {
            // Show empty state for this category
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

            // Create items count indicator
            const itemsCount = document.createElement('div');
            itemsCount.className = 'items-count';
            itemsCount.textContent = `${itemCount} ชิ้น`;

            // Create fade indicators
            const fadeLeft = document.createElement('div');
            fadeLeft.className = 'fade-left';
            const fadeRight = document.createElement('div');
            fadeRight.className = 'fade-right';

            // Insert elements
            clothesRow.parentNode.insertBefore(progressContainer, clothesRow);
            categorySection.appendChild(itemsCount);
            categorySection.appendChild(fadeLeft);
            categorySection.appendChild(fadeRight);

            // Show progress bar only if content overflows
            const hasOverflow = clothesRow.scrollWidth > clothesRow.clientWidth;
            if (!hasOverflow) {
                progressContainer.style.display = 'none';
            }
        }

        clothesRow.addEventListener('scroll', updateScrollButtons);

        // Add touch support for mobile
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

        // Enhance scroll buttons with keyboard support
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

        // Initialize scroll functionality
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

    // Add click event to all cloth images
    clothImages.forEach(img => {
        img.addEventListener('click', function(e) {
            e.stopPropagation();
            modalImage.src = this.src;
            imageModal.classList.add('show');
        });
    });

    // Close modal when clicking on it
    imageModal.addEventListener('click', function() {
        this.classList.remove('show');
    });

    // Close modal with ESC key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && imageModal.classList.contains('show')) {
            imageModal.classList.remove('show');
        }
    });
}