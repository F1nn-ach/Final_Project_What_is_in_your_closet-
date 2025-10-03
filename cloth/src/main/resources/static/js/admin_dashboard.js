
let currentYear;
let currentAstrologer = '';
let currentAstrologerId = null;
let matrixData = {};
let initialMatrixData = {};
let currentEditingCell = null;
let selectedColors = [];
let selectedLuckyTypes = [];

let days = [];
let luckyTypes = [];
let allColors = [];
let allLuckyColors = [];

function initializeData(data) {
  currentYear = data.currentYear;
  days = data.days;
  luckyTypes = data.luckyTypes;
  allColors = data.allColors;
  allLuckyColors = data.allLuckyColors;
}

function loadAstrologerData() {
  if (!currentAstrologer) return;

  matrixData = {};
  initialMatrixData = {};
  selectedLuckyTypes = [];

  const astrologerColors = allLuckyColors.filter(lc =>
    lc.astrologerName === currentAstrologer && lc.year === currentYear
  );

  astrologerColors.forEach(lc => {
    const cellKey = lc.day + '-' + lc.typeId;
    matrixData[cellKey] = [...lc.colors];
    initialMatrixData[cellKey] = [...lc.colors];
  });

  const typesWithData = [...new Set(astrologerColors.map(lc => lc.typeId))];
  if (typesWithData.length > 0) {
    selectedLuckyTypes = typesWithData;
  }
}

// Year selector
function initYearSelector() {
  document.getElementById('year-select').addEventListener('change', function () {
    currentYear = parseInt(this.value);
    if (currentAstrologer) {
      // Clear current data first
      matrixData = {};
      initialMatrixData = {};

      // Reload data for new year
      loadAstrologerData();

      // Rebuild matrix with the loaded data
      if (selectedLuckyTypes.length > 0) {
        buildMatrixPreservingData(matrixData);
      } else {
        // Show type selection again if no types selected
        showTypeSelection();
      }
    }
  });
}

function initAstrologerSelector() {
  document.getElementById('astrologer-select').addEventListener('change', function () {
    const value = this.value;
    const selectedOption = this.options[this.selectedIndex];
    const newInput = document.getElementById('new-astrologer-input');
    const typeSelectionContainer = document.getElementById('type-selection-container');
    const deleteBtn = document.getElementById('delete-astrologer-btn');
    const clearBtn = document.getElementById('clear-form-btn');

    if (value === '__new__') {
      // Clear all data when creating new astrologer
      matrixData = {};
      initialMatrixData = {};
      selectedLuckyTypes = [];

      newInput.classList.add('show');
      newInput.focus();
      currentAstrologer = '';
      currentAstrologerId = null;
      typeSelectionContainer.classList.remove('show');
      deleteBtn.style.display = 'none';
      clearBtn.style.display = 'none';
      document.getElementById('table-container').innerHTML = `
        <div class="empty-state">
          <h3>✏️ กรอกชื่อแม่หมอใหม่</h3>
          <p>กรุณากรอกชื่อแม่หมอในช่องด้านบน แล้วกดปุ่มใดก็ได้เพื่อสร้างตาราง</p>
        </div>
      `;
      updateSaveButtonState();
    } else if (value) {
      newInput.classList.remove('show');
      newInput.value = '';
      currentAstrologer = value;
      currentAstrologerId = selectedOption.getAttribute('data-id');
      deleteBtn.style.display = 'block';
      // Don't set clearBtn visibility here - let updateSaveButtonState() handle it

      // Clear old data first, then load new data
      matrixData = {};
      initialMatrixData = {};

      // Load existing data for this astrologer
      loadAstrologerData();

      showTypeSelection();
      updateSaveButtonState();
    } else {
      // Clear all data when deselecting
      matrixData = {};
      initialMatrixData = {};
      selectedLuckyTypes = [];

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

// New astrologer input
function initNewAstrologerInput() {
  document.getElementById('new-astrologer-input').addEventListener('blur', function () {
    const name = this.value.trim();
    if (name) {
      currentAstrologer = name;
      showTypeSelection();
      document.getElementById('submit-all-btn').disabled = false;
    }
  });

  document.getElementById('new-astrologer-input').addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
      this.blur();
    }
  });
}

// Show type selection checkboxes
function showTypeSelection() {
  const container = document.getElementById('type-selection-container');
  const checkboxesDiv = document.getElementById('type-checkboxes');

  // Generate checkboxes
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

  // If selectedLuckyTypes already set by loadAstrologerData, use those
  // Otherwise select all by default
  if (selectedLuckyTypes.length === 0) {
    selectedLuckyTypes = luckyTypes.map(t => t.id);
  }

  luckyTypes.forEach(type => {
    const checkbox = document.getElementById('type-' + type.id);
    checkbox.checked = selectedLuckyTypes.includes(type.id);
  });
  updateCheckboxStyles();

  // Build matrix preserving loaded data
  if (Object.keys(matrixData).length > 0) {
    buildMatrixPreservingData(matrixData);
  } else {
    buildMatrix();
  }
}

// Toggle checkbox
function toggleTypeCheckbox(typeId) {
  const checkbox = document.getElementById('type-' + typeId);
  checkbox.checked = !checkbox.checked;
  handleTypeChange();
}

// Handle type selection change
function handleTypeChange() {
  // Save current data before rebuilding
  const previousData = JSON.parse(JSON.stringify(matrixData));

  selectedLuckyTypes = [];
  luckyTypes.forEach(type => {
    const checkbox = document.getElementById('type-' + type.id);
    if (checkbox && checkbox.checked) {
      selectedLuckyTypes.push(type.id);
    }
  });

  // Remove data for unselected types
  Object.keys(previousData).forEach(key => {
    const typeId = parseInt(key.split('-')[1]);
    if (!selectedLuckyTypes.includes(typeId)) {
      delete previousData[key];
    }
  });

  updateCheckboxStyles();

  // Rebuild matrix
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

// Update checkbox visual styles
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

// Select all types
function selectAllTypes() {
  luckyTypes.forEach(type => {
    document.getElementById('type-' + type.id).checked = true;
  });
  handleTypeChange();
}

// Build matrix table preserving existing data
function buildMatrixPreservingData(previousData) {
  // Keep existing data
  matrixData = previousData || matrixData;

  // Filter only selected types
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

  let html = '<table class="matrix-table">';
  html += '<thead><tr>';
  html += '<th>วัน \\ ด้านโชคลาภ</th>';

  selectedTypes.forEach(type => {
    html += '<th>' + type.name + '</th>';
  });

  html += '</tr></thead><tbody>';

  days.forEach(day => {
    html += '<tr>';
    html += '<td>' + day + '</td>';

    selectedTypes.forEach(type => {
      const cellKey = day + '-' + type.id;
      const isKalakinee = type.name === 'กาลกิณี' || type.name === 'กาลากินี' || type.name.includes('กาล');
      const cellClass = 'matrix-cell empty' + (isKalakinee ? ' kalakinee-cell' : '');

      html += '<td>';
      html += '<div class="' + cellClass + '" data-day="' + day + '" data-type="' + type.id + '" data-typename="' + type.name + '" onclick="openCellColorPicker(this)">';
      html += '<div class="cell-colors" id="cell-' + cellKey + '"></div>';
      html += '</div>';
      html += '</td>';
    });

    html += '</tr>';
  });

  html += '</tbody></table>';

  document.getElementById('table-container').innerHTML = html;

  // Restore colors to cells
  Object.keys(matrixData).forEach(key => {
    const cellDiv = document.querySelector('[data-day="' + key.split('-')[0] + '"][data-type="' + key.split('-')[1] + '"]');
    if (cellDiv && matrixData[key] && matrixData[key].length > 0) {
      updateCellDisplay(cellDiv, matrixData[key]);
    }
  });

  updateSaveButtonState();
}

// Build matrix table (fresh start)
function buildMatrix(preserveInitialData = false) {
  matrixData = {}; // Reset data
  if (!preserveInitialData) {
    initialMatrixData = {}; // Reset initial state
  }

  // Filter only selected types
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

  let html = '<table class="matrix-table">';
  html += '<thead><tr>';
  html += '<th>วัน \\ ด้านโชคลาภ</th>';

  selectedTypes.forEach(type => {
    html += '<th>' + type.name + '</th>';
  });

  html += '</tr></thead><tbody>';

  days.forEach(day => {
    html += '<tr>';
    html += '<td>' + day + '</td>';

    selectedTypes.forEach(type => {
      const cellKey = day + '-' + type.id;
      const isKalakinee = type.name === 'กาลกิณี' || type.name === 'กาลากินี' || type.name.includes('กาล');
      const cellClass = 'matrix-cell empty' + (isKalakinee ? ' kalakinee-cell' : '');

      html += '<td>';
      html += '<div class="' + cellClass + '" data-day="' + day + '" data-type="' + type.id + '" data-typename="' + type.name + '" onclick="openCellColorPicker(this)">';
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

  // Update cell display
  updateCellDisplay(currentEditingCell, selectedColors);

  // If this is กาลกิณี, need to update other cells on same day to disable these colors
  if (isKalakineeType(typeId)) {
    // Force re-render of affected cells if they're open
    // This ensures colors are properly disabled in other types
  }

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

  // Check if using new astrologer name
  const newInput = document.getElementById('new-astrologer-input');
  let astrologerName = newInput.classList.contains('show') ? newInput.value.trim() : currentAstrologer;

  if (!astrologerName) {
    alert('กรุณากรอกชื่อแม่หมอ');
    return;
  }

  // If new astrologer, prefix with __new__:
  if (newInput.classList.contains('show')) {
    astrologerName = '__new__:' + astrologerName;
  }

  // Count entries
  let entryCount = 0;
  Object.keys(matrixData).forEach(key => {
    const colorIds = matrixData[key];
    if (colorIds && colorIds.length > 0) {
      entryCount++;
    }
  });

  // Check if this is a deletion (cleared all data)
  const isClearing = entryCount === 0 && Object.keys(initialMatrixData).length > 0;

  if (entryCount === 0 && !isClearing) {
    alert('กรุณาเพิ่มสีมงคลอย่างน้อย 1 รายการ');
    return;
  }

  // Confirm message based on action
  let confirmMessage;
  let confirmTitle;
  if (isClearing) {
    confirmMessage = 'ยืนยันการลบข้อมูลสีมงคลทั้งหมดของแม่หมอนี้?';
    confirmTitle = 'ยืนยันการลบข้อมูล';
  } else {
    confirmMessage = 'ยืนยันการบันทึก ' + entryCount + ' รายการ?';
    confirmTitle = 'ยืนยันการบันทึก';
  }

  // Show confirmation modal
  showConfirmModal(confirmMessage, function () {
    // Prepare matrix data string format: "day-typeId:colorId1,colorId2,colorId3;day2-typeId2:..."
    const matrixDataStr = Object.keys(matrixData).map(key => {
      const colorIds = matrixData[key];
      if (colorIds && colorIds.length > 0) {
        return key + ':' + colorIds.join(',');
      }
      return '';
    }).filter(s => s !== '').join(';');

    // Create form and submit
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = getContextPath() + '/save-matrix-data';

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

    const matrixDataInput = document.createElement('input');
    matrixDataInput.type = 'hidden';
    matrixDataInput.name = 'matrixData';
    matrixDataInput.value = matrixDataStr;
    form.appendChild(matrixDataInput);

    // Send selected types - this is crucial for deletion to work
    const selectedTypesInput = document.createElement('input');
    selectedTypesInput.type = 'hidden';
    selectedTypesInput.name = 'selectedTypes';
    selectedTypesInput.value = selectedLuckyTypes.join(',');
    form.appendChild(selectedTypesInput);

    document.body.appendChild(form);
    form.submit();
  }, confirmTitle);
}

// Delete astrologer
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
    form.action = getContextPath() + '/delete/astrologer/' + pendingDeleteAstrologerId;

    document.body.appendChild(form);
    form.submit();
  }
}

// Delete current selected astrologer
function deleteCurrentAstrologer() {
  if (currentAstrologerId && currentAstrologer) {
    deleteAstrologer(currentAstrologerId, currentAstrologer);
  }
}

// Clear form
function clearForm() {
  // Show confirmation before clearing
  showConfirmModal(
    'คุณต้องการล้างข้อมูลทั้งหมดในฟอร์มนี้หรือไม่?',
    function () {
      matrixData = {};
      // Don't reset initialMatrixData so hasChanges() detects the clearing
      buildMatrix(true); // Pass true to preserve initialMatrixData
      updateSaveButtonState();
    },
    'ยืนยันการล้างฟอร์ม'
  );
}

// Check if data has changed
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

// Update save button state
function updateSaveButtonState() {
  const saveBtn = document.getElementById('submit-all-btn');
  const clearBtn = document.getElementById('clear-form-btn');
  const hasData = Object.keys(matrixData).some(k => matrixData[k] && matrixData[k].length > 0);
  const changed = hasChanges();

  // Check if user cleared form (no data but had initial data)
  const isClearing = !hasData && Object.keys(initialMatrixData).length > 0;

  // Update clear button visibility - only show if there's data to clear
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
