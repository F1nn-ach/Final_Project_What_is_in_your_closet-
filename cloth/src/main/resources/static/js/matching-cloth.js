// Matching Cloth Page JavaScript

class MatchingClothApp {
    constructor() {
        this.selectedClothes = {
            top: null,
            bottom: null,
            jacket: null
        };

        // Matching results
        this.matchingResults = {
            tops: [],
            bottoms: [],
            jackets: []
        };
        this.currentIndex = {
            tops: 0,
            bottoms: 0,
            jackets: 0
        };

        this.init();
    }

    init() {
        this.setupEventListeners();
        this.updateFilterCounts();
        this.disableJacketItems();
    }

    setupEventListeners() {
        // Astrologer selection
        document.getElementById('astrologer-select')?.addEventListener('change', (e) => this.onAstrologerChange(e));

        // Day selection
        document.getElementById('day-select')?.addEventListener('change', (e) => this.onDayChange(e));

        // Lucky type checkboxes
        document.querySelectorAll('.lucky-type-checkbox').forEach(cb => {
            cb.addEventListener('change', (e) => this.onLuckyTypeChange(e));
        });

        // Cloth type filter buttons
        document.querySelectorAll('.cloth-type-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.onClothTypeFilter(e));
        });

        // Color theory selection
        document.getElementById('color-theory-select')?.addEventListener('change', (e) => this.onColorTheoryChange(e));

        // Include jacket toggle
        document.getElementById('include-jacket-toggle')?.addEventListener('change', (e) => this.onJacketToggle(e));

        // Clothing selection
        this.setupClothingSelection();

        // Navigation buttons
        this.setupNavigationButtons();

        // Error popup close
        document.getElementById('error-popup-close')?.addEventListener('click', () => this.hideErrorPopup());
        document.querySelector('.popup-overlay')?.addEventListener('click', (e) => {
            if (e.target === e.currentTarget) {
                this.hideErrorPopup();
            }
        });
    }

    setupNavigationButtons() {
        // Tops navigation
        document.getElementById('tops-nav-prev')?.addEventListener('click', () => this.navigateResults('tops', 'prev'));
        document.getElementById('tops-nav-next')?.addEventListener('click', () => this.navigateResults('tops', 'next'));

        // Bottoms navigation
        document.getElementById('bottoms-nav-prev')?.addEventListener('click', () => this.navigateResults('bottoms', 'prev'));
        document.getElementById('bottoms-nav-next')?.addEventListener('click', () => this.navigateResults('bottoms', 'next'));

        // Jackets navigation
        document.getElementById('jackets-nav-prev')?.addEventListener('click', () => this.navigateResults('jackets', 'prev'));
        document.getElementById('jackets-nav-next')?.addEventListener('click', () => this.navigateResults('jackets', 'next'));
    }

    disableJacketItems() {
        const clothingItems = document.querySelectorAll('.clothing-item');
        clothingItems.forEach(item => {
            if (this.isJacketType(item.dataset.typeName)) {
                item.classList.add('disabled');
                item.title = 'เปิดสวิตช์ "รวมเสื้อคลุมด้วย" เพื่อเลือกเสื้อคลุม';
            }
        });
    }

    enableJacketItems() {
        const clothingItems = document.querySelectorAll('.clothing-item');
        clothingItems.forEach(item => {
            if (this.isJacketType(item.dataset.typeName)) {
                item.classList.remove('disabled');
                item.title = '';
            }
        });
    }

    setupClothingSelection() {
        const clothingItems = document.querySelectorAll('.clothing-item');
        clothingItems.forEach(item => {
            item.addEventListener('click', (e) => this.onClothingSelect(e));
        });
    }

    // Event Handlers
    onAstrologerChange(event) {
        const selectedValue = event.target.value;
        const dayGroup = document.getElementById('day-group');
        const luckyTypesGroup = document.getElementById('lucky-types-group');

        if (selectedValue) {
            dayGroup.style.display = 'block';
            luckyTypesGroup.style.display = 'block';
            dayGroup.classList.add('fade-in');
            luckyTypesGroup.classList.add('fade-in');

            // Filter lucky types based on selected astrologer
            this.filterLuckyTypesByAstrologer(parseInt(selectedValue));
        } else {
            dayGroup.style.display = 'none';
            luckyTypesGroup.style.display = 'none';
            // Reset all checkboxes to visible
            this.showAllLuckyTypes();
        }

        // Resend API if clothes are already selected
        if (this.selectedClothes.top || this.selectedClothes.bottom || this.selectedClothes.jacket) {
            this.fetchMatchingClothes();
        }
    }

    filterLuckyTypesByAstrologer(astrologerId) {
        // Get unique lucky type IDs for this astrologer from allLuckyColorsData
        const astrologerLuckyTypes = new Set();

        if (typeof allLuckyColorsData !== 'undefined') {
            allLuckyColorsData.forEach(lc => {
                if (lc.astrologerId === astrologerId) {
                    astrologerLuckyTypes.add(lc.luckyColorTypeId);
                }
            });
        }

        // Show/hide checkboxes based on whether astrologer has that type
        document.querySelectorAll('.lucky-type-checkbox').forEach(checkbox => {
            const luckyTypeId = parseInt(checkbox.value);
            const checkboxItem = checkbox.closest('.checkbox-item');

            if (astrologerLuckyTypes.has(luckyTypeId)) {
                // Show this lucky type
                checkboxItem.style.display = 'flex';
                checkbox.disabled = false;
            } else {
                // Hide this lucky type
                checkboxItem.style.display = 'none';
                checkbox.disabled = true;
                checkbox.checked = false;
            }
        });
    }

    showAllLuckyTypes() {
        document.querySelectorAll('.lucky-type-checkbox').forEach(checkbox => {
            const checkboxItem = checkbox.closest('.checkbox-item');
            checkboxItem.style.display = 'flex';
            checkbox.disabled = false;
            checkbox.checked = false;
        });
    }

    onDayChange(event) {
        const selectedDay = event.target.value;

        // Resend API if clothes are already selected
        if (this.selectedClothes.top || this.selectedClothes.bottom || this.selectedClothes.jacket) {
            this.fetchMatchingClothes();
        }
    }

    onLuckyTypeChange(event) {
        const checkedTypes = Array.from(document.querySelectorAll('.lucky-type-checkbox:checked'))
            .map(cb => cb.value);

        // Resend API if clothes are already selected
        if (this.selectedClothes.top || this.selectedClothes.bottom || this.selectedClothes.jacket) {
            this.fetchMatchingClothes();
        }
    }

    onClothTypeFilter(event) {
        const button = event.target;
        const typeId = button.dataset.typeId;
        const type = button.dataset.type;

        // Update active button
        document.querySelectorAll('.cloth-type-btn').forEach(btn => btn.classList.remove('active'));
        button.classList.add('active');

        // Filter clothing items
        const clothingItems = document.querySelectorAll('.clothing-item');
        clothingItems.forEach(item => {
            if (type === 'all') {
                item.style.display = 'block';
            } else if (typeId && item.dataset.typeId === typeId) {
                item.style.display = 'block';
            } else if (typeId) {
                item.style.display = 'none';
            }
        });
    }

    onColorTheoryChange(event) {
        const selectedTheory = event.target.value;
        const selectedOption = event.target.selectedOptions[0];
        const description = selectedOption?.dataset.description;
        const descriptionDiv = document.getElementById('color-theory-description');
        const descriptionContent = descriptionDiv?.querySelector('.description-content');

        if (selectedTheory && description && descriptionContent) {
            descriptionContent.textContent = description;
            descriptionDiv.style.display = 'block';
            descriptionDiv.classList.add('fade-in');
        } else if (descriptionDiv) {
            descriptionDiv.style.display = 'none';
        }

        // Resend API if clothes are already selected
        if (this.selectedClothes.top || this.selectedClothes.bottom || this.selectedClothes.jacket) {
            this.fetchMatchingClothes();
        }
    }

    onJacketToggle(event) {
        const jacketResult = document.getElementById('jacket-result');
        const clothingItems = document.querySelectorAll('.clothing-item');

        if (event.target.checked) {
            jacketResult.style.display = 'block';
            jacketResult.classList.add('fade-in');
            // Enable jacket items when toggle is on
            this.enableJacketItems();
        } else {
            jacketResult.style.display = 'none';
            this.selectedClothes.jacket = null;

            // Clear matching jacket results
            this.clearMatchingResults('jackets');

            // Also clear the selected jacket slot manually
            const jacketSlot = document.getElementById('jacket-slot');
            if (jacketSlot) {
                jacketSlot.innerHTML = '<div class="slot-placeholder">เลือกเสื้อคลุมก่อน</div>';
                jacketSlot.classList.remove('filled');
            }

            // Disable jacket items when toggle is off
            this.disableJacketItems();
            // Remove selection from any selected jacket
            clothingItems.forEach(item => {
                if (this.isJacketType(item.dataset.typeName) && item.classList.contains('selected')) {
                    item.classList.remove('selected');
                }
            });
        }
    }

    onClothingSelect(event) {
        const clothingItem = event.currentTarget;
        const typeId = clothingItem.dataset.typeId;
        const typeName = clothingItem.dataset.typeName;

        // Prevent jacket selection if toggle is off
        const jacketToggle = document.getElementById('include-jacket-toggle');
        if (this.isJacketType(typeName) && !jacketToggle?.checked) {
            this.showErrorPopup('กรุณาเปิดสวิตช์ "รวมเสื้อคลุมด้วย" ก่อนเลือกเสื้อคลุม');
            return;
        }

        // Check if item is disabled
        if (clothingItem.classList.contains('disabled')) {
            return;
        }

        const clothId = clothingItem.dataset.clothId;

        // Toggle selection
        if (clothingItem.classList.contains('selected')) {
            clothingItem.classList.remove('selected');
            this.removeClothingFromSelection(clothId, typeName);
        } else {
            // Check mutual exclusivity
            // Can't select top if bottom is selected
            if (this.isTopType(typeName) && this.selectedClothes.bottom) {
                this.showErrorPopup('ไม่สามารถเลือกเสื้อและกางเกงพร้อมกันได้');
                return;
            }
            // Can't select bottom if top is selected
            if (this.isBottomType(typeName) && this.selectedClothes.top) {
                this.showErrorPopup('ไม่สามารถเลือกเสื้อและกางเกงพร้อมกันได้');
                return;
            }
            // Can't select top/bottom if jacket is already selected
            if ((this.isTopType(typeName) || this.isBottomType(typeName)) && this.selectedClothes.jacket) {
                this.showErrorPopup('ไม่สามารถเลือกเสื้อ/กางเกงเมื่อเลือกเสื้อคลุมแล้ว');
                return;
            }
            // Can't select jacket if top or bottom is already selected
            if (this.isJacketType(typeName) && (this.selectedClothes.top || this.selectedClothes.bottom)) {
                this.showErrorPopup('ไม่สามารถเลือกเสื้อคลุมเมื่อเลือกเสื้อ/กางเกงแล้ว');
                return;
            }

            // Remove previous selection of the same type
            this.clearPreviousSelection(typeName);

            clothingItem.classList.add('selected');
            this.addClothingToSelection(clothingItem, typeName);
        }

        this.updateMutualExclusivity();
    }

    clearPreviousSelection(typeName) {
        const allClothingItems = document.querySelectorAll('.clothing-item');

        allClothingItems.forEach(item => {
            const itemTypeName = item.dataset.typeName;

            // Clear selection for same type
            if (this.isJacketType(typeName) && this.isJacketType(itemTypeName)) {
                item.classList.remove('selected');
            } else if (this.isTopType(typeName) && this.isTopType(itemTypeName)) {
                item.classList.remove('selected');
            } else if (this.isBottomType(typeName) && this.isBottomType(itemTypeName)) {
                item.classList.remove('selected');
            }
        });
    }

    // Clothing Selection Management
    addClothingToSelection(clothingItem, typeName) {
        const clothingData = this.extractClothingData(clothingItem);

        if (this.isJacketType(typeName)) {
            this.selectedClothes.jacket = clothingData;
            this.updateResultSlot('jacket-slot', clothingData);
            // Automatically fetch matching tops and bottoms for jacket
            this.fetchMatchingClothes();
        } else if (this.isTopType(typeName)) {
            this.selectedClothes.top = clothingData;
            this.updateResultSlot('top-result-slot', clothingData);
            // Automatically fetch matching bottoms
            this.fetchMatchingClothes();
        } else if (this.isBottomType(typeName)) {
            this.selectedClothes.bottom = clothingData;
            this.updateResultSlot('bottom-result-slot', clothingData);
            // Automatically fetch matching tops
            this.fetchMatchingClothes();
        }
    }

    removeClothingFromSelection(clothId, typeName) {
        if (this.isJacketType(typeName)) {
            this.selectedClothes.jacket = null;
            this.updateResultSlot('jacket-slot', null);
            // Clear matching results for tops and bottoms when jacket was main
            this.clearMatchingResults('tops');
            this.clearMatchingResults('bottoms');
        } else if (this.isTopType(typeName)) {
            this.selectedClothes.top = null;
            this.updateResultSlot('top-result-slot', null);
            // Clear matching results
            this.clearMatchingResults('bottoms');
            this.clearMatchingResults('jackets');
        } else if (this.isBottomType(typeName)) {
            this.selectedClothes.bottom = null;
            this.updateResultSlot('bottom-result-slot', null);
            // Clear matching results
            this.clearMatchingResults('tops');
            this.clearMatchingResults('jackets');
        }
    }

    updateMutualExclusivity() {
        const clothingItems = document.querySelectorAll('.clothing-item');
        const hasTopSelected = !!this.selectedClothes.top;
        const hasBottomSelected = !!this.selectedClothes.bottom;
        const hasJacketSelected = !!this.selectedClothes.jacket;
        const jacketToggle = document.getElementById('include-jacket-toggle');
        const isJacketToggleOn = jacketToggle?.checked || false;

        clothingItems.forEach(item => {
            const typeName = item.dataset.typeName;
            const isTop = this.isTopType(typeName);
            const isBottom = this.isBottomType(typeName);
            const isJacket = this.isJacketType(typeName);

            // Handle jacket items separately
            if (isJacket) {
                // If toggle is off, keep jackets disabled
                if (!isJacketToggleOn) {
                    item.classList.add('disabled');
                    item.title = 'เปิดสวิตช์ "รวมเสื้อคลุมด้วย" เพื่อเลือกเสื้อคลุม';
                }
                // If top or bottom is selected, disable jackets
                else if (hasTopSelected || hasBottomSelected) {
                    item.classList.add('disabled');
                    item.title = 'ไม่สามารถเลือกเสื้อคลุมเมื่อเลือกเสื้อ/กางเกงแล้ว';
                }
                // Otherwise enable jackets
                else {
                    item.classList.remove('disabled');
                    item.title = '';
                }
                return;
            }

            // If jacket is selected, disable all tops and bottoms
            if (hasJacketSelected && (isTop || isBottom)) {
                item.classList.add('disabled');
                return;
            }

            // Disable tops if bottom is selected, and vice versa
            if ((hasTopSelected && isBottom) || (hasBottomSelected && isTop)) {
                item.classList.add('disabled');
            } else {
                item.classList.remove('disabled');
            }
        });
    }


    extractClothingData(clothingItem) {
        const img = clothingItem.querySelector('img');
        const nameElement = clothingItem.querySelector('.clothing-info h4');
        const idElement = clothingItem.querySelector('.clothing-info p');

        return {
            id: clothingItem.dataset.clothId,
            name: nameElement?.textContent || '',
            displayInfo: idElement?.textContent || '',
            imageSrc: img?.src || '',
            imageAlt: img?.alt || '',
            clothId: parseInt(clothingItem.dataset.clothId),
            clothImage: `${imageBaseUrl}${clothingItem.dataset.username}/${clothingItem.dataset.clothImage}`,
            clothTypeId: parseInt(clothingItem.dataset.typeId),
            clothTypeName: clothingItem.dataset.typeName,
            subHex: clothingItem.dataset.subHex,
            subGroup: clothingItem.dataset.subGroup,
            mainHex: clothingItem.dataset.mainHex,
            mainGroup: clothingItem.dataset.mainGroup
        };
    }

    updateResultSlot(slotId, clothingData) {
        const slot = document.getElementById(slotId);
        if (!slot) return;

        if (clothingData) {
            slot.innerHTML = `
                <div class="result-clothing-item">
                    <button class="remove-btn" onclick="matchingApp.removeFromSlot('${slotId}')" title="ลบ">×</button>
                    <div class="result-clothing-image">
                        <img src="${clothingData.imageSrc}" alt="${clothingData.imageAlt}">
                    </div>
                    <div class="result-clothing-name">${clothingData.name}</div>
                </div>
            `;
            slot.classList.add('filled');
        } else {
            const placeholderText = slotId.includes('jacket') ? 'เลือกเสื้อคลุมก่อน' :
                (slotId.includes('top') ? 'เลือกเสื้อก่อน' : 'เลือกกางเกงก่อน');
            slot.innerHTML = `<div class="slot-placeholder">${placeholderText}</div>`;
            slot.classList.remove('filled');
        }
    }

    removeFromSlot(slotId) {
        // Determine which type of clothing to remove
        if (slotId === 'jacket-slot') {
            // Find and unselect jacket in grid
            const clothingItems = document.querySelectorAll('.clothing-item');
            clothingItems.forEach(item => {
                if (this.isJacketType(item.dataset.typeName) && item.classList.contains('selected')) {
                    item.classList.remove('selected');
                }
            });
            this.selectedClothes.jacket = null;
            this.updateResultSlot('jacket-slot', null);
            this.clearMatchingResults('jackets');
        } else if (slotId === 'top-result-slot') {
            // Find and unselect top in grid
            const clothingItems = document.querySelectorAll('.clothing-item');
            clothingItems.forEach(item => {
                if (this.isTopType(item.dataset.typeName) && item.classList.contains('selected')) {
                    item.classList.remove('selected');
                }
            });
            this.selectedClothes.top = null;
            this.updateResultSlot('top-result-slot', null);
            this.clearMatchingResults('bottoms');
            this.clearMatchingResults('jackets');
            this.updateMutualExclusivity();
        } else if (slotId === 'bottom-result-slot') {
            // Find and unselect bottom in grid
            const clothingItems = document.querySelectorAll('.clothing-item');
            clothingItems.forEach(item => {
                if (this.isBottomType(item.dataset.typeName) && item.classList.contains('selected')) {
                    item.classList.remove('selected');
                }
            });
            this.selectedClothes.bottom = null;
            this.updateResultSlot('bottom-result-slot', null);
            this.clearMatchingResults('tops');
            this.clearMatchingResults('jackets');
            this.updateMutualExclusivity();
        }
    }

    // Error Popup
    showErrorPopup(message) {
        const popup = document.getElementById('error-popup');
        const messageElement = document.getElementById('error-popup-message');
        if (popup && messageElement) {
            messageElement.textContent = message;
            popup.classList.add('visible');
        }
    }

    hideErrorPopup() {
        const popup = document.getElementById('error-popup');
        if (popup) {
            popup.classList.remove('visible');
        }
    }

    // Type Checking Helpers
    isTopType(typeIdentifier) {
        // Check by type name or type ID
        return typeIdentifier === 'เสื้อ' || typeIdentifier === '1';
    }

    isBottomType(typeIdentifier) {
        // Check by type name or type ID
        return typeIdentifier === 'กางเกง' || typeIdentifier === 'กระโปรง' ||
            typeIdentifier === '2' || typeIdentifier === '3';
    }

    isJacketType(typeIdentifier) {
        // Check by type name or type ID
        return typeIdentifier === 'เสื้อคลุม' || typeIdentifier === '4';
    }

    // UI Helpers
    updateFilterCounts() {
        const clothingItems = document.querySelectorAll('.clothing-item');
        const clothTypeButtons = document.querySelectorAll('.cloth-type-btn');

        clothTypeButtons.forEach(button => {
            const typeId = button.dataset.typeId;
            const type = button.dataset.type;
            let count = 0;

            if (type === 'all') {
                count = clothingItems.length;
            } else if (typeId) {
                count = Array.from(clothingItems).filter(item =>
                    item.dataset.typeId === typeId
                ).length;
            }

            const originalText = button.textContent.replace(/ \(\d+\)$/, '');
            button.textContent = `${originalText} (${count})`;
        });
    }

    // Map Thai color theory names to English
    getEnglishTheoryName(thaiName) {
        const theoryMap = {
            'สีตรงข้าม': 'complementary',
            'สีใกล้เคียง': 'analogous',
            'สีสามเสา': 'triadic'
        };
        return theoryMap[thaiName] || thaiName.toLowerCase();
    }

    // Matching API Functions
    async fetchMatchingClothes() {
        const mainCloth = this.selectedClothes.top || this.selectedClothes.bottom || this.selectedClothes.jacket;
        if (!mainCloth) return;

        const colorTheorySelect = document.getElementById('color-theory-select');
        const astrologerSelect = document.getElementById('astrologer-select');
        const daySelect = document.getElementById('day-select');
        const jacketToggle = document.getElementById('include-jacket-toggle');

        const selectedTheoryOption = colorTheorySelect?.selectedOptions[0];
        const thaiTheoryName = selectedTheoryOption?.dataset?.theoryName || '';

        // Convert Thai name to English (use empty string if no theory selected)
        const theoryName = thaiTheoryName ? this.getEnglishTheoryName(thaiTheoryName) : '';

        // Build main_item object
        const main_item = {
            clothId: mainCloth.clothId,
            clothImage: mainCloth.clothImage,
            clothTypeId: mainCloth.clothTypeId,
            clothTypeName: mainCloth.clothTypeName,
            subHex: mainCloth.subHex,
            subGroup: mainCloth.subGroup,
            mainHex: mainCloth.mainHex,
            mainGroup: mainCloth.mainGroup
        };

        // Collect all clothing items for candidates
        const allClothingItems = document.querySelectorAll('.clothing-item');
        const tops_candidates = [];
        const pants_candidates = [];
        const outer_candidates = [];

        const isTopSelected = !!this.selectedClothes.top;
        const isBottomSelected = !!this.selectedClothes.bottom;
        const isJacketSelected = !!this.selectedClothes.jacket;
        const hasOuter = jacketToggle?.checked || false;

        allClothingItems.forEach(item => {
            const typeName = item.dataset.typeName;
            const clothData = {
                clothId: parseInt(item.dataset.clothId),
                clothImage: `${imageBaseUrl}${item.dataset.username}/${item.dataset.clothImage}`,
                clothTypeId: parseInt(item.dataset.typeId),
                clothTypeName: typeName,
                subHex: item.dataset.subHex,
                subGroup: item.dataset.subGroup,
                mainHex: item.dataset.mainHex,
                mainGroup: item.dataset.mainGroup
            };

            if (typeName === 'เสื้อ') {
                // Add tops if bottom is selected OR jacket is selected
                if (isBottomSelected || isJacketSelected) {
                    tops_candidates.push(clothData);
                }
            } else if (typeName === 'กางเกง' || typeName === 'กระโปรง') {
                // Add pants/skirts if top is selected OR jacket is selected
                if (isTopSelected || isJacketSelected) {
                    pants_candidates.push(clothData);
                }
            } else if (typeName === 'เสื้อคลุม' && hasOuter) {
                // Only add outer if toggle is on AND (top OR bottom is selected, NOT jacket as main)
                if ((isTopSelected || isBottomSelected) && !isJacketSelected) {
                    outer_candidates.push(clothData);
                }
            }
        });

        // Fetch lucky colors
        const lucky_colors = await this.fetchLuckyColors(astrologerSelect, daySelect);

        const requestData = {
            main_item: main_item,
            theory: theoryName,
            lucky_colors: lucky_colors,
            has_outer: hasOuter
        };

        // Only include candidates arrays if they should be sent
        if (isBottomSelected) {
            requestData.tops_candidates = tops_candidates;
            requestData.pants_candidates = [];
        } else if (isTopSelected) {
            requestData.pants_candidates = pants_candidates;
            requestData.tops_candidates = [];
        } else if (isJacketSelected) {
            // If jacket is selected as main, search for both tops and bottoms
            requestData.tops_candidates = tops_candidates;
            requestData.pants_candidates = pants_candidates;
        }

        if (hasOuter && !isJacketSelected) {
            requestData.outer_candidates = outer_candidates;
        } else {
            requestData.outer_candidates = [];
        }

        try {
            const response = await fetch(`${contextPath}/api/matching/match`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestData)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Failed to fetch matching clothes');
            }

            const data = await response.json();
            this.handleMatchingResults(data);

        } catch (error) {
            this.showErrorPopup('เกิดข้อผิดพลาดในการค้นหาเสื้อผ้าที่จับคู่: ' + error.message);
        }
    }

    handleMatchingResults(data) {
        // Store results
        this.matchingResults.tops = data.tops_options || [];
        this.matchingResults.bottoms = data.pants_options || [];
        this.matchingResults.jackets = data.outer_options || [];

        // Reset indices
        this.currentIndex.tops = 0;
        this.currentIndex.bottoms = 0;
        this.currentIndex.jackets = 0;

        // Display first result
        if (this.selectedClothes.top) {
            // If top is selected, show matching bottoms
            this.displayMatchingResult('bottoms');
        } else if (this.selectedClothes.bottom) {
            // If bottom is selected, show matching tops
            this.displayMatchingResult('tops');
        } else if (this.selectedClothes.jacket) {
            // If jacket is selected as main, show matching tops and bottoms
            this.displayMatchingResult('tops');
            this.displayMatchingResult('bottoms');
        }

        // Display jackets if toggle is on AND jacket is not the main item
        if (document.getElementById('include-jacket-toggle')?.checked && !this.selectedClothes.jacket) {
            this.displayMatchingResult('jackets');
        }
    }

    displayMatchingResult(type) {
        const results = this.matchingResults[type];
        const index = this.currentIndex[type];

        let slotId;
        if (type === 'tops') {
            slotId = 'top-result-slot';
        } else if (type === 'bottoms') {
            slotId = 'bottom-result-slot';
        } else if (type === 'jackets') {
            slotId = 'jacket-slot';
        }

        const slot = document.getElementById(slotId);
        if (!slot) return;

        // Clear slot first
        slot.innerHTML = '';
        slot.classList.remove('filled');

        if (!results || results.length === 0) {
            // No results found
            slot.innerHTML = '<div class="no-results">ไม่มีผลลัพธ์ที่จับคู่ได้</div>';
            this.updateNavigationButtons(type, 0, 0);
            return;
        }

        // Display current result
        const currentResult = results[index];
        // Handle both response formats
        let imageSrc;
        if (currentResult.clothImage && currentResult.clothImage.startsWith('http')) {
            // Full URL already provided
            imageSrc = currentResult.clothImage;
        } else if (currentResult.username && currentResult.clothImage) {
            // Build URL from parts
            imageSrc = `${imageBaseUrl}${currentResult.username}/${currentResult.clothImage}`;
        } else {
            // Fallback
            imageSrc = currentResult.clothImage || '';
        }

        slot.innerHTML = `
            <div class="result-clothing-item">
                <div class="result-clothing-image">
                    <img src="${imageSrc}" alt="${currentResult.clothTypeName || currentResult.typeName || ''}">
                </div>
                <div class="result-clothing-name">${currentResult.clothTypeName || currentResult.typeName || ''}</div>
            </div>
        `;
        slot.classList.add('filled');

        // Update navigation buttons
        this.updateNavigationButtons(type, index + 1, results.length);
    }

    updateNavigationButtons(type, current, total) {
        const prevBtn = document.getElementById(`${type}-nav-prev`);
        const nextBtn = document.getElementById(`${type}-nav-next`);
        const counter = document.getElementById(`${type}-counter`);

        if (total === 0 || !prevBtn || !nextBtn || !counter) {
            if (prevBtn) prevBtn.style.display = 'none';
            if (nextBtn) nextBtn.style.display = 'none';
            if (counter) counter.style.display = 'none';
            return;
        }

        // Show buttons and counter
        prevBtn.style.display = 'block';
        nextBtn.style.display = 'block';
        counter.style.display = 'block';

        // Update counter text
        counter.textContent = `${current} / ${total}`;

        // Enable/disable buttons
        prevBtn.disabled = current <= 1;
        nextBtn.disabled = current >= total;
    }

    navigateResults(type, direction) {
        const results = this.matchingResults[type];
        if (!results || results.length === 0) return;

        if (direction === 'next') {
            if (this.currentIndex[type] < results.length - 1) {
                this.currentIndex[type]++;
            }
        } else if (direction === 'prev') {
            if (this.currentIndex[type] > 0) {
                this.currentIndex[type]--;
            }
        }

        this.displayMatchingResult(type);
    }

    clearMatchingResults(type) {
        this.matchingResults[type] = [];
        this.currentIndex[type] = 0;

        let slotId;
        if (type === 'tops') {
            slotId = 'top-result-slot';
        } else if (type === 'bottoms') {
            slotId = 'bottom-result-slot';
        } else if (type === 'jackets') {
            slotId = 'jacket-slot';
        }

        const slot = document.getElementById(slotId);
        if (slot) {
            const placeholderText = type === 'jackets' ? 'เลือกเสื้อคลุมก่อน' :
                (type === 'tops' ? 'เลือกเสื้อก่อน' : 'เลือกกางเกงก่อน');
            slot.innerHTML = `<div class="slot-placeholder">${placeholderText}</div>`;
            slot.classList.remove('filled');
        }

        // Hide navigation buttons and counter
        const prevBtn = document.getElementById(`${type}-nav-prev`);
        const nextBtn = document.getElementById(`${type}-nav-next`);
        const counter = document.getElementById(`${type}-counter`);

        if (prevBtn) prevBtn.style.display = 'none';
        if (nextBtn) nextBtn.style.display = 'none';
        if (counter) counter.style.display = 'none';
    }

    async fetchLuckyColors(astrologerSelect, daySelect) {
        const astrologerId = astrologerSelect?.value ? parseInt(astrologerSelect.value) : null;
        const dayOfWeek = daySelect?.value || null;

        // If no astrologer or day selected, return empty
        if (!astrologerId || !dayOfWeek) {
            return { good: [], bad: [] };
        }

        // Collect lucky color type IDs
        const luckyColorTypeIds = Array.from(document.querySelectorAll('.lucky-type-checkbox:checked'))
            .map(cb => parseInt(cb.value));

        try {
            const response = await fetch(`${contextPath}/api/matching/lucky-colors`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    astrologerId: astrologerId,
                    dayOfWeek: dayOfWeek,
                    luckyColorTypeIds: luckyColorTypeIds.length > 0 ? luckyColorTypeIds : null
                })
            });

            if (response.ok) {
                const data = await response.json();
                return data;
            }
        } catch (error) {
            // Silent error handling
        }

        return { good: [], bad: [] };
    }
}

// Initialize app when DOM is ready
let matchingApp;
document.addEventListener('DOMContentLoaded', () => {
    matchingApp = new MatchingClothApp();
});