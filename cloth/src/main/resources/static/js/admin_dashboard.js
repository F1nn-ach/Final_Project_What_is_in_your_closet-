
let currentYear;
let currentAstrologer = '';
let currentAstrologerId = null;
let matrixData = {};
let initialMatrixData = {};
let currentEditingCell = null;
let selectedColors = [];
let selectedLuckyTypes = [];
let isReadOnly = false;
let actualCurrentYear;

let days = [];
let luckyTypes = [];
let allColors = [];

function initializeData(data) {
  currentYear = data.currentYear;
  actualCurrentYear = data.currentYear;
  days = data.days;
  luckyTypes = data.luckyTypes;
  allColors = sortColorsByHue(data.allColors);
  updateReadOnlyMode();
  renderColorGrid();
}

// Render color grid in modal with sorted colors
function renderColorGrid() {
  const colorGrid = document.getElementById('color-grid');
  if (!colorGrid) return;

  let html = '';
  allColors.forEach(color => {
    html += '<div class="color-card-modal" data-id="' + color.id + '" data-name="' + color.name + '" data-hex="' + color.hex + '">';
    html += '<div class="color-preview-modal" style="background: ' + color.hex + ';"></div>';
    html += '<div class="color-name-modal">' + color.name + '</div>';
    html += '</div>';
  });

  colorGrid.innerHTML = html;
}

// Function to convert hex to HSL for sorting
function hexToHSL(hex) {
  // Remove # if present
  hex = hex.replace('#', '');

  // Convert hex to RGB
  const r = parseInt(hex.substring(0, 2), 16) / 255;
  const g = parseInt(hex.substring(2, 4), 16) / 255;
  const b = parseInt(hex.substring(4, 6), 16) / 255;

  const max = Math.max(r, g, b);
  const min = Math.min(r, g, b);
  let h, s, l = (max + min) / 2;

  if (max === min) {
    h = s = 0; // achromatic (gray)
  } else {
    const d = max - min;
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min);

    switch (max) {
      case r: h = ((g - b) / d + (g < b ? 6 : 0)) / 6; break;
      case g: h = ((b - r) / d + 2) / 6; break;
      case b: h = ((r - g) / d + 4) / 6; break;
    }
  }

  return {
    h: h * 360, // 0-360
    s: s * 100, // 0-100
    l: l * 100  // 0-100
  };
}

// Sort colors by hue to create a beautiful rainbow gradient
function sortColorsByHue(colors) {
  return colors.slice().sort((a, b) => {
    const hslA = hexToHSL(a.hex);
    const hslB = hexToHSL(b.hex);

    // First sort by hue (color spectrum)
    if (Math.abs(hslA.h - hslB.h) > 1) {
      return hslA.h - hslB.h;
    }

    // If hue is similar, sort by saturation (vibrant colors first)
    if (Math.abs(hslA.s - hslB.s) > 1) {
      return hslB.s - hslA.s;
    }

    // If saturation is similar, sort by lightness (darker first)
    return hslA.l - hslB.l;
  });
}

async function loadAstrologerData() {
  if (!currentAstrologer) return;

  matrixData = {};
  initialMatrixData = {};
  selectedLuckyTypes = [];

  try {
    const response = await fetch(
      `${getContextPath()}/api/admin/lucky-color/search?astrologerName=${encodeURIComponent(currentAstrologer)}&year=${currentYear}`
    );

    if (!response.ok) {
      console.error('Failed to load astrologer data');
      return;
    }

    const astrologerColors = await response.json();

    astrologerColors.forEach(lc => {
      const cellKey = lc.day + '-' + lc.typeId;
      matrixData[cellKey] = [...lc.colors];
      initialMatrixData[cellKey] = [...lc.colors];
    });

    const typesWithData = [...new Set(astrologerColors.map(lc => lc.typeId))];
    if (typesWithData.length > 0) {
      selectedLuckyTypes = typesWithData;
    }

    const hasSessionData = loadFromSessionStorage();
    if (hasSessionData) {
      showUnsavedDataNotification();
    }
  } catch (error) {
    console.error('Error loading astrologer data:', error);
  }
}

function showUnsavedDataNotification() {
  const existingNotification = document.getElementById('unsaved-data-notification');
  if (existingNotification) {
    existingNotification.remove();
  }

  const headerBar = document.querySelector('.header-bar');
  if (!headerBar) return;

  const notification = document.createElement('div');
  notification.id = 'unsaved-data-notification';
  notification.style.cssText = `
    background: linear-gradient(135deg, #fff3e0, #ffe0b2);
    border: 2px solid #ff9800;
    border-radius: 8px;
    padding: 12px 20px;
    margin-bottom: 15px;
    color: #e65100;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 10px;
    box-shadow: 0 2px 8px rgba(255, 152, 0, 0.2);
  `;
  notification.innerHTML = `
    <span style="font-size: 1.3rem;">💾</span>
    <span>พบข้อมูลที่ยังไม่ได้บันทึก - ข้อมูลถูกกู้คืนจากการแก้ไขครั้งก่อน</span>
  `;

  headerBar.insertBefore(notification, headerBar.firstChild);

  setTimeout(() => {
    if (notification && notification.parentNode) {
      notification.style.transition = 'opacity 0.5s ease';
      notification.style.opacity = '0';
      setTimeout(() => {
        if (notification && notification.parentNode) {
          notification.remove();
        }
      }, 500);
    }
  }, 10000);
}


function updateReadOnlyMode() {
  isReadOnly = (currentYear !== actualCurrentYear);
  updateUIForReadOnlyMode();
}

function updateUIForReadOnlyMode() {
  const submitBtn = document.getElementById('submit-all-btn');
  const deleteBtn = document.getElementById('delete-astrologer-btn');
  const clearBtn = document.getElementById('clear-form-btn');
  const astrologerSelect = document.getElementById('astrologer-select');
  const newAstrologerInput = document.getElementById('new-astrologer-input');
  const headerBar = document.querySelector('.header-bar');

  if (isReadOnly) {
    if (submitBtn) submitBtn.style.display = 'none';
    if (deleteBtn) deleteBtn.style.display = 'none';
    if (clearBtn) clearBtn.style.display = 'none';

    const newOption = astrologerSelect.querySelector('option[value="__new__"]');
    if (newOption) newOption.disabled = true;

    if (newAstrologerInput) newAstrologerInput.disabled = true;

    let readOnlyBanner = document.getElementById('read-only-banner');
    if (!readOnlyBanner) {
      readOnlyBanner = document.createElement('div');
      readOnlyBanner.id = 'read-only-banner';
      readOnlyBanner.className = 'read-only-banner';
      readOnlyBanner.innerHTML = '📖 โหมดดูข้อมูล - ปีที่เลือกไม่ใช่ปีปัจจุบัน (ไม่สามารถแก้ไขได้)';
      headerBar.insertBefore(readOnlyBanner, headerBar.firstChild);
    }
  } else {
    if (submitBtn) submitBtn.style.display = 'block';

    const newOption = astrologerSelect.querySelector('option[value="__new__"]');
    if (newOption) newOption.disabled = false;

    if (newAstrologerInput) newAstrologerInput.disabled = false;

    const readOnlyBanner = document.getElementById('read-only-banner');
    if (readOnlyBanner) {
      readOnlyBanner.remove();
    }
  }
}

function initYearSelector() {
  document.getElementById('year-select').addEventListener('change', async function () {
    clearSessionStorage();

    currentYear = parseInt(this.value);
    updateReadOnlyMode();

    if (currentAstrologer) {
      matrixData = {};
      initialMatrixData = {};

      await loadAstrologerData();

      if (selectedLuckyTypes.length > 0) {
        buildMatrixPreservingData(matrixData);
      } else {
        showTypeSelection();
      }
    }
  });
}

function initAstrologerSelector() {
  document.getElementById('astrologer-select').addEventListener('change', async function () {
    clearSessionStorage();

    const value = this.value;
    const selectedOption = this.options[this.selectedIndex];
    const newInput = document.getElementById('new-astrologer-input');
    const typeSelectionContainer = document.getElementById('type-selection-container');
    const deleteBtn = document.getElementById('delete-astrologer-btn');
    const clearBtn = document.getElementById('clear-form-btn');

    if (value === '__new__' && isReadOnly) {
      alert('ไม่สามารถเพิ่มแม่หมอใหม่ได้ เนื่องจากปีที่เลือกไม่ใช่ปีปัจจุบัน');
      this.value = '';
      return;
    }

    if (value === '__new__') {
      matrixData = {};
      initialMatrixData = {};
      selectedLuckyTypes = [];

      const newContainer = document.getElementById('new-astrologer-container');
      newContainer.classList.add('show');
      newInput.focus();

      currentAstrologer = '';
      currentAstrologerId = null;
      typeSelectionContainer.classList.remove('show');
      deleteBtn.style.display = 'none';
      clearBtn.style.display = 'none';

      const submitBtn = document.getElementById('submit-all-btn');
      submitBtn.style.display = 'none';

      document.getElementById('table-container').innerHTML = `
        <div class="empty-state">
          <h3>✏️ กรอกชื่อแม่หมอใหม่</h3>
          <p>กรุณากรอกชื่อแม่หมอในช่องด้านบน แล้วกดปุ่ม "เพิ่มแม่หมอ" เพื่อสร้างแม่หมอและเริ่มกรอกข้อมูลสีมงคล</p>
        </div>
      `;
    } else if (value) {
      const newContainer = document.getElementById('new-astrologer-container');
      newContainer.classList.remove('show');
      newInput.classList.remove('show');
      newInput.value = '';
      currentAstrologer = value;
      currentAstrologerId = selectedOption.getAttribute('data-id');
      deleteBtn.style.display = 'block';

      matrixData = {};
      initialMatrixData = {};

      await loadAstrologerData();

      showTypeSelection();
      updateSaveButtonState();
    } else {
      matrixData = {};
      initialMatrixData = {};
      selectedLuckyTypes = [];

      const newContainer = document.getElementById('new-astrologer-container');
      newContainer.classList.remove('show');
      newInput.classList.remove('show');
      newInput.value = '';

      currentAstrologer = '';
      currentAstrologerId = null;
      typeSelectionContainer.classList.remove('show');
      deleteBtn.style.display = 'none';
      clearBtn.style.display = 'none';
      document.getElementById('table-container').innerHTML = `
        <div class="empty-state">
          <h3>📋 เลือกแม่หมอเพื่อเริ่มต้น</h3>
          <p>กรุณาเลือกแม่หมอจากด้านบนเพื่อแสดงตารางเมทริกซ์</p>
        </div>
      `;
      updateSaveButtonState();
    }
  });
}

function initNewAstrologerInput() {
  const newInput = document.getElementById('new-astrologer-input');

  newInput.addEventListener('input', function () {
    const name = this.value.trim();

    const createBtn = document.getElementById('btn-create-astrologer');
    if (createBtn) {
      createBtn.disabled = !name;
    }

    const inlineBtn = document.getElementById('btn-add-astrologer-inline');
    if (inlineBtn) {
      inlineBtn.disabled = !name;
    }
  });

  newInput.addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
      const name = this.value.trim();
      if (name) {
        createNewAstrologer();
      }
    }
  });
}


async function createNewAstrologer() {
  const newInput = document.getElementById('new-astrologer-input');
  const astrologerName = newInput.value.trim();

  if (!astrologerName) {
    alert('กรุณากรอกชื่อแม่หมอ');
    return;
  }

  showConfirmModal(
    'ยืนยันจะเพิ่มแม่หมอ "' + astrologerName + '"?',
    async function () {
      try {
        const response = await fetch(getContextPath() + '/api/admin/astrologer/add', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: 'astrologerName=' + encodeURIComponent(astrologerName)
        });

        if (!response.ok) {
          throw new Error('Failed to create astrologer');
        }

        const result = await response.json();

        if (result.success) {
          currentAstrologer = astrologerName;
          currentAstrologerId = result.astrologerId;

          const astrologerSelect = document.getElementById('astrologer-select');
          const newOption = document.createElement('option');
          newOption.value = astrologerName;
          newOption.textContent = astrologerName;
          newOption.setAttribute('data-id', currentAstrologerId);

          const addNewOption = astrologerSelect.querySelector('option[value="__new__"]');
          astrologerSelect.insertBefore(newOption, addNewOption);

          astrologerSelect.value = astrologerName;

          const newContainer = document.getElementById('new-astrologer-container');
          newContainer.classList.remove('show');
          newInput.value = '';

          document.getElementById('table-container').innerHTML = `
            <div class="empty-state">
              <h3>✅ เพิ่มแม่หมอสำเร็จ</h3>
              <p>กำลังสร้างตารางสีมงคล...</p>
            </div>
          `;

          setTimeout(() => {
            showTypeSelection();
            document.getElementById('submit-all-btn').style.display = 'block';
            document.getElementById('delete-astrologer-btn').style.display = 'block';
            updateSaveButtonState();
          }, 500);

        } else {
          alert('ไม่สามารถเพิ่มแม่หมอได้: ' + (result.message || 'เกิดข้อผิดพลาด'));
        }

      } catch (error) {
        console.error('Error creating astrologer:', error);
        alert('เกิดข้อผิดพลาดในการเพิ่มแม่หมอ กรุณาลองใหม่อีกครั้ง');
      }
    },
    'ยืนยันการเพิ่มแม่หมอ'
  );
}


function showTypeSelection() {
  const container = document.getElementById('type-selection-container');
  const checkboxesDiv = document.getElementById('type-checkboxes');

  let html = '';
  luckyTypes.forEach(type => {
    const isKalakinee = type.name === 'กาลกิณี' || type.name === 'กาลากินี' || type.name.includes('กาล');
    const itemClass = 'type-checkbox-item' + (isKalakinee ? ' kalakinee' : '');

    html += '<div class="' + itemClass + '" onclick="toggleTypeCheckbox(' + type.id + ')">';
    html += '<input type="checkbox" id="type-' + type.id + '" value="' + type.id + '" onchange="handleTypeChange()">';
    html += '<label class="type-checkbox-label" for="type-' + type.id + '">' + type.name + '</label>';
    html += '</div>';
  });

  checkboxesDiv.innerHTML = html;
  container.classList.add('show');

  if (selectedLuckyTypes.length === 0) {
    selectedLuckyTypes = luckyTypes.map(t => t.id);
  }

  luckyTypes.forEach(type => {
    const checkbox = document.getElementById('type-' + type.id);
    checkbox.checked = selectedLuckyTypes.includes(type.id);
  });
  updateCheckboxStyles();

  if (Object.keys(matrixData).length > 0) {
    buildMatrixPreservingData(matrixData);
  } else {
    buildMatrix();
  }
}


function toggleTypeCheckbox(typeId) {
  const checkbox = document.getElementById('type-' + typeId);
  checkbox.checked = !checkbox.checked;
  handleTypeChange();
}

function handleTypeChange() {
  const previousData = JSON.parse(JSON.stringify(matrixData));

  selectedLuckyTypes = [];
  luckyTypes.forEach(type => {
    const checkbox = document.getElementById('type-' + type.id);
    if (checkbox && checkbox.checked) {
      selectedLuckyTypes.push(type.id);
    }
  });

  Object.keys(previousData).forEach(key => {
    const typeId = parseInt(key.split('-')[1]);
    if (!selectedLuckyTypes.includes(typeId)) {
      delete previousData[key];
    }
  });

  updateCheckboxStyles();

  saveToSessionStorage();

  if (selectedLuckyTypes.length > 0) {
    buildMatrixPreservingData(previousData);
  } else {
    document.getElementById('table-container').innerHTML = `
      <div class="empty-state">
        <h3>⚠️ กรุณาเลือกด้านโชคลาภ</h3>
        <p>เลือกอย่างน้อย 1 ด้านโชคลาภเพื่อแสดงตาราง</p>
      </div>
    `;
  }
}


function updateCheckboxStyles() {
  luckyTypes.forEach(type => {
    const checkbox = document.getElementById('type-' + type.id);
    const item = checkbox.closest('.type-checkbox-item');
    if (checkbox.checked) {
      item.classList.add('checked');
    } else {
      item.classList.remove('checked');
    }
  });
}

function selectAllTypes() {
  luckyTypes.forEach(type => {
    document.getElementById('type-' + type.id).checked = true;
  });
  handleTypeChange();
}


function buildMatrixPreservingData(previousData) {
  matrixData = previousData || matrixData;

  const selectedTypes = luckyTypes.filter(t => selectedLuckyTypes.includes(t.id));

  if (selectedTypes.length === 0) {
    document.getElementById('table-container').innerHTML = `
      <div class="empty-state">
        <h3>📊 เลือกด้านโชคลาภ</h3>
        <p>กรุณาเลือกด้านโชคลาภที่ต้องการเพิ่ม</p>
      </div>
    `;
    return;
  }

  // Sort: กาลกิณี at the end, others by original order
  const sortedTypes = [...selectedTypes].sort((a, b) => {
    const aIsKalakinee = a.name === 'กาลกิณี' || a.name === 'กาลากินี' || a.name.includes('กาล');
    const bIsKalakinee = b.name === 'กาลกิณี' || b.name === 'กาลากินี' || b.name.includes('กาล');

    if (aIsKalakinee && !bIsKalakinee) return 1;
    if (!aIsKalakinee && bIsKalakinee) return -1;
    return 0;
  });

  let html = '<table class="matrix-table">';
  html += '<thead><tr>';
  html += '<th>วัน \\ ด้านโชคลาภ</th>';

  sortedTypes.forEach(type => {
    html += '<th>' + type.name + '</th>';
  });

  html += '</tr></thead><tbody>';

  days.forEach(day => {
    html += '<tr>';
    html += '<td>' + day + '</td>';

    sortedTypes.forEach(type => {
      const cellKey = day + '-' + type.id;
      const isKalakinee = type.name === 'กาลกิณี' || type.name === 'กาลากินี' || type.name.includes('กาล');
      const cellClass = 'matrix-cell empty' + (isKalakinee ? ' kalakinee-cell' : '') + (isReadOnly ? ' read-only' : '');

      html += '<td>';
      const onclickAttr = isReadOnly ? '' : 'onclick="openCellColorPicker(this)"';
      const cursorStyle = isReadOnly ? 'cursor: default;' : '';
      html += '<div class="' + cellClass + '" data-day="' + day + '" data-type="' + type.id + '" data-typename="' + type.name + '" ' + onclickAttr + ' style="' + cursorStyle + '">';
      html += '<div class="cell-colors" id="cell-' + cellKey + '"></div>';
      html += '</div>';
      html += '</td>';
    });

    html += '</tr>';
  });

  html += '</tbody></table>';

  document.getElementById('table-container').innerHTML = html;

  Object.keys(matrixData).forEach(key => {
    const cellDiv = document.querySelector('[data-day="' + key.split('-')[0] + '"][data-type="' + key.split('-')[1] + '"]');
    if (cellDiv && matrixData[key] && matrixData[key].length > 0) {
      updateCellDisplay(cellDiv, matrixData[key]);
    }
  });

  updateSaveButtonState();
}

function buildMatrix(preserveInitialData = false) {
  matrixData = {};
  if (!preserveInitialData) {
    initialMatrixData = {};
  }

  const selectedTypes = luckyTypes.filter(t => selectedLuckyTypes.includes(t.id));

  if (selectedTypes.length === 0) {
    document.getElementById('table-container').innerHTML = `
      <div class="empty-state">
        <h3>⚠️ กรุณาเลือกด้านโชคลาภ</h3>
        <p>เลือกอย่างน้อย 1 ด้านโชคลาภเพื่อแสดงตาราง</p>
      </div>
    `;
    return;
  }

  // Sort: กาลกิณี at the end, others by original order
  const sortedTypes = [...selectedTypes].sort((a, b) => {
    const aIsKalakinee = a.name === 'กาลกิณี' || a.name === 'กาลากินี' || a.name.includes('กาล');
    const bIsKalakinee = b.name === 'กาลกิณี' || b.name === 'กาลากินี' || b.name.includes('กาล');

    if (aIsKalakinee && !bIsKalakinee) return 1;
    if (!aIsKalakinee && bIsKalakinee) return -1;
    return 0;
  });

  let html = '<table class="matrix-table">';
  html += '<thead><tr>';
  html += '<th>วัน \\ ด้านโชคลาภ</th>';

  sortedTypes.forEach(type => {
    html += '<th>' + type.name + '</th>';
  });

  html += '</tr></thead><tbody>';

  days.forEach(day => {
    html += '<tr>';
    html += '<td>' + day + '</td>';

    sortedTypes.forEach(type => {
      const cellKey = day + '-' + type.id;
      const isKalakinee = type.name === 'กาลกิณี' || type.name === 'กาลากินี' || type.name.includes('กาล');
      const cellClass = 'matrix-cell empty' + (isKalakinee ? ' kalakinee-cell' : '') + (isReadOnly ? ' read-only' : '');

      html += '<td>';
      const onclickAttr = isReadOnly ? '' : 'onclick="openCellColorPicker(this)"';
      const cursorStyle = isReadOnly ? 'cursor: default;' : '';
      html += '<div class="' + cellClass + '" data-day="' + day + '" data-type="' + type.id + '" data-typename="' + type.name + '" ' + onclickAttr + ' style="' + cursorStyle + '">';
      html += '<div class="cell-colors" id="cell-' + cellKey + '"></div>';
      html += '</div>';
      html += '</td>';
    });

    html += '</tr>';
  });

  html += '</tbody></table>';

  document.getElementById('table-container').innerHTML = html;
}

// Get กาลกิณี colors for a specific day
function getKalakineeColorsForDay(day) {
  // Try both possible spellings
  const kalakineeType = luckyTypes.find(t =>
    t.name === 'กาลกิณี' ||
    t.name === 'กาลากินี' ||
    t.name.includes('กาล')
  );

  if (!kalakineeType) {
    return [];
  }

  const cellKey = day + '-' + kalakineeType.id;
  const colors = matrixData[cellKey] || [];

  return colors;
}

// Check if current type is กาลกิณี
function isKalakineeType(typeId) {
  const type = luckyTypes.find(t => t.id === parseInt(typeId));
  const isKalakinee = type && (
    type.name === 'กาลกิณี' ||
    type.name === 'กาลากินี' ||
    type.name.includes('กาล')
  );

  return isKalakinee;
}

// Get all colors already used by other types on same day (excluding current type)
function getColorsUsedByOtherTypes(day, currentTypeId) {
  const usedColors = [];
  const selectedTypes = luckyTypes.filter(t => selectedLuckyTypes.includes(t.id));

  selectedTypes.forEach(type => {
    if (type.id !== parseInt(currentTypeId)) {
      const cellKey = day + '-' + type.id;
      const colors = matrixData[cellKey] || [];
      colors.forEach(colorId => {
        if (!usedColors.includes(colorId)) {
          usedColors.push(colorId);
        }
      });
    }
  });

  return usedColors;
}

// Get disabled colors for current cell
function getDisabledColorsForCell(day, typeId) {
  const isCurrentKalakinee = isKalakineeType(typeId);

  if (isCurrentKalakinee) {
    // If editing กาลกิณี, can't use colors already used by OTHER types on same day
    const colorsUsedByOthers = getColorsUsedByOtherTypes(day, typeId);
    return colorsUsedByOthers;
  } else {
    // If editing other type, can't use colors in กาลกิณี
    const kalakineeColors = getKalakineeColorsForDay(day);
    return kalakineeColors;
  }
}

// Open color picker for a cell
function openCellColorPicker(cellDiv) {
  // Prevent editing if in read-only mode
  if (isReadOnly) {
    alert('ไม่สามารถแก้ไขข้อมูลได้ เนื่องจากปีที่เลือกไม่ใช่ปีปัจจุบัน\nคุณสามารถดูข้อมูล History เท่านั้น');
    return;
  }

  currentEditingCell = cellDiv;
  const day = cellDiv.dataset.day;
  const typeId = cellDiv.dataset.type;
  const typeName = cellDiv.dataset.typename;
  const cellKey = day + '-' + typeId;

  document.getElementById('modal-cell-info').textContent = day + ' - ' + typeName;

  // Load existing colors for this cell
  selectedColors = matrixData[cellKey] || [];

  // Get disabled colors (กาลกิณี colors on same day)
  const disabledColors = getDisabledColorsForCell(day, typeId);

  // Show/hide warning message
  const warningDiv = document.getElementById('modal-warning');
  const isCurrentKalakinee = isKalakineeType(typeId);

  if (disabledColors.length > 0) {
    // Update warning text based on context
    if (isCurrentKalakinee) {
      warningDiv.innerHTML = '⚠️ สีที่เป็นสีเทาไม่สามารถเลือกได้ เพราะถูกใช้ใน <strong>ด้านโชคลาภอื่น</strong> แล้ว (กาลกิณีต้องไม่ซ้ำกับด้านอื่น)';
    } else {
      warningDiv.innerHTML = '⚠️ สีที่เป็นสีเทาไม่สามารถเลือกได้ เพราะถูกใช้ใน <strong>กาลกิณี</strong> แล้ว';
    }

    warningDiv.style.display = 'block';
  } else {
    warningDiv.style.display = 'none';
  }

  // Update modal UI
  document.querySelectorAll('.color-card-modal').forEach(card => {
    const colorId = parseInt(card.dataset.id);
    const isSelected = selectedColors.includes(colorId);
    const isDisabled = disabledColors.includes(colorId);

    card.classList.remove('selected', 'disabled');

    if (isDisabled) {
      card.classList.add('disabled');
      card.style.opacity = '0.3';
      card.style.cursor = 'not-allowed';
      card.title = 'สีนี้ถูกใช้ใน กาลกิณี แล้ว';
    } else {
      card.style.opacity = '1';
      card.style.cursor = 'pointer';
      card.title = '';
      if (isSelected) {
        card.classList.add('selected');
      }
    }
  });

  updateSelectedCount();

  // Add click handlers
  const isCurrentKalakineeCopy = isCurrentKalakinee; // Capture for closure
  document.querySelectorAll('.color-card-modal').forEach(card => {
    card.onclick = function () {
      // Check if disabled
      if (this.classList.contains('disabled')) {
        if (isCurrentKalakineeCopy) {
          alert('ไม่สามารถเลือกสีนี้ได้ เนื่องจากถูกใช้ในด้านโชคลาภอื่นแล้ว\nกาลกิณีต้องไม่ซ้ำกับด้านอื่น');
        } else {
          alert('ไม่สามารถเลือกสีนี้ได้ เนื่องจากถูกใช้ใน กาลกิณี แล้ว');
        }
        return;
      }

      const colorId = parseInt(this.dataset.id);

      if (this.classList.contains('selected')) {
        this.classList.remove('selected');
        selectedColors = selectedColors.filter(id => id !== colorId);
      } else {
        this.classList.add('selected');
        selectedColors.push(colorId);
      }

      updateSelectedCount();
    };
  });

  document.getElementById('color-modal').classList.add('show');
}

function closeColorModal() {
  document.getElementById('color-modal').classList.remove('show');
  currentEditingCell = null;
  selectedColors = [];
}

function clearAllColors() {
  selectedColors = [];
  document.querySelectorAll('.color-card-modal').forEach(card => {
    card.classList.remove('selected');
  });
  updateSelectedCount();
}

function confirmColors() {
  if (!currentEditingCell) return;

  const day = currentEditingCell.dataset.day;
  const typeId = currentEditingCell.dataset.type;
  const cellKey = day + '-' + typeId;

  // Double check for color conflicts
  const disabledColors = getDisabledColorsForCell(day, typeId);
  const hasConflict = selectedColors.some(colorId => disabledColors.includes(colorId));

  if (hasConflict) {
    if (isKalakineeType(typeId)) {
      alert('ไม่สามารถบันทึกได้ เนื่องจากมีสีที่ซ้ำกับด้านโชคลาภอื่น\nกาลกิณีต้องไม่ซ้ำกับด้านอื่น');
    } else {
      alert('ไม่สามารถบันทึกได้ เนื่องจากมีสีที่ซ้ำกับ กาลกิณี');
    }
    return;
  }

  // Save to matrix data
  if (selectedColors.length > 0) {
    matrixData[cellKey] = [...selectedColors];
  } else {
    delete matrixData[cellKey];
  }

  updateCellDisplay(currentEditingCell, selectedColors);

  saveToSessionStorage();

  updateSaveButtonState();
  closeColorModal();
}

function updateCellDisplay(cellDiv, colorIds) {
  const cellKey = cellDiv.dataset.day + '-' + cellDiv.dataset.type;
  const colorsContainer = document.getElementById('cell-' + cellKey);
  const isKalakinee = isKalakineeType(cellDiv.dataset.type);

  if (!colorIds || colorIds.length === 0) {
    cellDiv.classList.remove('has-colors');
    cellDiv.classList.add('empty');
    // Preserve kalakinee-cell class
    if (!isKalakinee) {
      cellDiv.className = 'matrix-cell empty';
    } else {
      cellDiv.className = 'matrix-cell empty kalakinee-cell';
    }
    colorsContainer.innerHTML = '';
    return;
  }

  cellDiv.classList.add('has-colors');
  cellDiv.classList.remove('empty');

  let html = '';
  colorIds.forEach(colorId => {
    const color = allColors.find(c => c.id === colorId);
    if (color) {
      html += '<div class="color-bubble" style="background: ' + color.hex + ';" data-name="' + color.name + '"></div>';
    }
  });

  html += '<div class="colors-count">' + colorIds.length + ' สี</div>';

  colorsContainer.innerHTML = html;
}

function updateSelectedCount() {
  document.getElementById('selected-count').textContent = selectedColors.length;
}

// Submit all data
function submitAll() {
  if (!currentAstrologer) {
    alert('กรุณาเลือกหรือกรอกชื่อแม่หมอ');
    return;
  }

  // Use currentAstrologer directly (no longer need to check newInput)
  let astrologerName = currentAstrologer;

  if (!astrologerName) {
    alert('กรุณากรอกชื่อแม่หมอ');
    return;
  }

  // Count entries
  let entryCount = 0;
  Object.keys(matrixData).forEach(key => {
    const colorIds = matrixData[key];
    if (colorIds && colorIds.length > 0) {
      entryCount++;
    }
  });

  // Determine action type - check if astrologer was recently created (no initialMatrixData)
  const isNewAstrologer = Object.keys(initialMatrixData).length === 0 && currentAstrologerId;
  const isClearing = entryCount === 0 && Object.keys(initialMatrixData).length > 0;

  if (entryCount === 0 && !isClearing) {
    alert('กรุณาเพิ่มสีมงคลอย่างน้อย 1 รายการ');
    return;
  }

  // Confirm message based on action
  let confirmMessage;
  let confirmTitle;

  if (isClearing) {
    // ลบข้อมูลทั้งหมด
    confirmMessage = 'ยืนยันจะลบข้อมูลสีมงคลทั้งหมดของแม่หมอนี้?';
    confirmTitle = 'ยืนยันการลบข้อมูล';
  } else if (isNewAstrologer) {
    // เพิ่มแม่หมอใหม่
    confirmMessage = 'ยืนยันจะเพิ่มข้อมูลสีมงคล ' + entryCount + ' รายการ?';
    confirmTitle = 'ยืนยันการเพิ่มข้อมูล';
  } else {
    // แก้ไขข้อมูลแม่หมอที่มีอยู่
    confirmMessage = 'ยืนยันจะแก้ไขข้อมูลสีมงคล ' + entryCount + ' รายการ?';
    confirmTitle = 'ยืนยันการแก้ไขข้อมูล';
  }

  // Show confirmation modal
  showConfirmModal(confirmMessage, function () {
    // Prepare lucky colors data string format: "day-typeId:colorId1,colorId2,colorId3;day2-typeId2:..."
    const luckyColorsDataStr = Object.keys(matrixData).map(key => {
      const colorIds = matrixData[key];
      if (colorIds && colorIds.length > 0) {
        return key + ':' + colorIds.join(',');
      }
      return '';
    }).filter(s => s !== '').join(';');

    // Determine action type
    const isNewlyCreatedAstrologer = Object.keys(initialMatrixData).length === 0 && currentAstrologerId;

    // Create form and submit
    const form = document.createElement('form');
    form.method = 'POST';

    // Use different endpoint based on whether it's new or edit
    if (isNewlyCreatedAstrologer) {
      form.action = getContextPath() + '/admin/lucky-color/save';
    } else {
      form.action = getContextPath() + '/admin/lucky-color/edit';
    }

    const astrologerInput = document.createElement('input');
    astrologerInput.type = 'hidden';
    astrologerInput.name = 'astrologerName';
    astrologerInput.value = astrologerName;
    form.appendChild(astrologerInput);

    const yearInput = document.createElement('input');
    yearInput.type = 'hidden';
    yearInput.name = 'year';
    yearInput.value = currentYear;
    form.appendChild(yearInput);

    const luckyColorsDataInput = document.createElement('input');
    luckyColorsDataInput.type = 'hidden';
    luckyColorsDataInput.name = 'luckyColorsData';
    luckyColorsDataInput.value = luckyColorsDataStr;
    form.appendChild(luckyColorsDataInput);

    // Send selected types - this is crucial for deletion to work
    const selectedTypesInput = document.createElement('input');
    selectedTypesInput.type = 'hidden';
    selectedTypesInput.name = 'selectedTypes';
    selectedTypesInput.value = selectedLuckyTypes.join(',');
    form.appendChild(selectedTypesInput);

    clearSessionStorage();

    document.body.appendChild(form);
    form.submit();
  }, confirmTitle);
}

let pendingDeleteAstrologerId = null;

function deleteAstrologer(astrologerId, astrologerName) {
  pendingDeleteAstrologerId = astrologerId;
  document.getElementById('delete-astrologer-name').textContent = astrologerName;
  document.getElementById('delete-modal').classList.add('show');
}

function closeDeleteModal() {
  document.getElementById('delete-modal').classList.remove('show');
  pendingDeleteAstrologerId = null;
}

function confirmDeleteAstrologer() {
  if (pendingDeleteAstrologerId) {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = getContextPath() + '/admin/astrologer/delete/' + pendingDeleteAstrologerId;

    document.body.appendChild(form);
    form.submit();
  }
}

function deleteCurrentAstrologer() {
  if (currentAstrologerId && currentAstrologer) {
    deleteAstrologer(currentAstrologerId, currentAstrologer);
  }
}

function clearForm() {
  showConfirmModal(
    'คุณต้องการล้างข้อมูลทั้งหมดในฟอร์มนี้หรือไม่?',
    function () {
      matrixData = {};
      buildMatrix(true);

      clearSessionStorage();

      updateSaveButtonState();
    },
    'ยืนยันการล้างฟอร์ม'
  );
}


function hasChanges() {
  const currentKeys = Object.keys(matrixData).filter(k => matrixData[k] && matrixData[k].length > 0);
  const initialKeys = Object.keys(initialMatrixData).filter(k => initialMatrixData[k] && initialMatrixData[k].length > 0);

  if (currentKeys.length !== initialKeys.length) return true;

  for (let key of currentKeys) {
    const current = (matrixData[key] || []).sort().join(',');
    const initial = (initialMatrixData[key] || []).sort().join(',');
    if (current !== initial) return true;
  }

  return false;
}

function updateSaveButtonState() {
  const saveBtn = document.getElementById('submit-all-btn');
  const clearBtn = document.getElementById('clear-form-btn');
  const hasData = Object.keys(matrixData).some(k => matrixData[k] && matrixData[k].length > 0);
  const changed = hasChanges();

  const isClearing = !hasData && Object.keys(initialMatrixData).length > 0;

  if (clearBtn) {
    if (hasData) {
      clearBtn.style.display = 'block';
    } else {
      clearBtn.style.display = 'none';
    }
  }

  if (!currentAstrologer && !document.getElementById('new-astrologer-input').value.trim()) {
    saveBtn.disabled = true;
  } else if (!hasData && !isClearing) {
    saveBtn.disabled = true;
  } else if (!changed) {
    saveBtn.disabled = true;
  } else {
    saveBtn.disabled = false;
  }
}

let contextPath = '';
function getContextPath() {
  return contextPath;
}

function setContextPath(path) {
  contextPath = path;
}

function getSessionStorageKey() {
  return `adminDashboard_${currentAstrologer}_${currentYear}`;
}

function saveToSessionStorage() {
  if (!currentAstrologer) return;

  const sessionData = {
    matrixData: matrixData,
    selectedLuckyTypes: selectedLuckyTypes,
    timestamp: new Date().getTime()
  };

  try {
    sessionStorage.setItem(getSessionStorageKey(), JSON.stringify(sessionData));
  } catch (e) {
    console.error('Failed to save to session storage:', e);
  }
}

function loadFromSessionStorage() {
  if (!currentAstrologer) return false;

  try {
    const savedData = sessionStorage.getItem(getSessionStorageKey());
    if (savedData) {
      const sessionData = JSON.parse(savedData);

      const now = new Date().getTime();
      const maxAge = 24 * 60 * 60 * 1000;

      if (now - sessionData.timestamp < maxAge) {
        matrixData = sessionData.matrixData || {};
        selectedLuckyTypes = sessionData.selectedLuckyTypes || [];
        return true;
      } else {
        clearSessionStorage();
      }
    }
  } catch (e) {
    console.error('Failed to load from session storage:', e);
  }

  return false;
}

function clearSessionStorage() {
  try {
    sessionStorage.removeItem(getSessionStorageKey());
  } catch (e) {
    console.error('Failed to clear session storage:', e);
  }
}

function clearAllSessionStorage() {
  try {
    Object.keys(sessionStorage).forEach(key => {
      if (key.startsWith('adminDashboard_')) {
        sessionStorage.removeItem(key);
      }
    });
  } catch (e) {
    console.error('Failed to clear all session storage:', e);
  }
}

let confirmModalCallback = null;

function showConfirmModal(message, onConfirm, title = 'ยืนยันการดำเนินการ') {
  const modal = document.getElementById('confirm-modal');
  const titleEl = document.getElementById('confirm-modal-title');
  const messageEl = document.getElementById('confirm-modal-message');

  if (!modal) {
    return;
  }

  if (titleEl) titleEl.textContent = title;
  if (messageEl) messageEl.textContent = message;
  confirmModalCallback = onConfirm;
  modal.classList.add('show');
}

function closeConfirmModal() {
  document.getElementById('confirm-modal').classList.remove('show');
  confirmModalCallback = null;
}

function confirmModalAction() {
  if (confirmModalCallback) {
    confirmModalCallback();
  }
  closeConfirmModal();
}

function initEventListeners() {
  initYearSelector();
  initAstrologerSelector();
  initNewAstrologerInput();
}
