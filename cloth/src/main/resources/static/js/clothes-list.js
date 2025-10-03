document.addEventListener('DOMContentLoaded', () => {
    const mainColorCircles = document.querySelectorAll('.main-color-circle');
    const subColorGroups = document.querySelectorAll('.sub-color-group');
    const subColorCircles = document.querySelectorAll('.sub-color-circle');
    const clothCards = document.querySelectorAll('.cloth-card');
    const filterTagsContainer = document.querySelector('.filter-tags');

    let activeColorFilters = [];

    mainColorCircles.forEach(circle => {
        circle.addEventListener('click', () => {
            const mainColorId = circle.dataset.mainColorId;

            // Toggle active state for main colors
            mainColorCircles.forEach(c => c.classList.remove('active'));
            circle.classList.add('active');

            // Show the corresponding sub-color group
            subColorGroups.forEach(group => {
                if (group.id === `sub-colors-${mainColorId}`) {
                    group.style.display = 'flex';
                } else {
                    group.style.display = 'none';
                }
            });
        });
    });

    subColorCircles.forEach(circle => {
        circle.addEventListener('click', () => {
            const subColorId = circle.dataset.subColorId;
            circle.classList.toggle('active');

            if (activeColorFilters.includes(subColorId)) {
                activeColorFilters = activeColorFilters.filter(id => id !== subColorId);
            } else {
                activeColorFilters.push(subColorId);
            }

            updateFilterTags();
            filterClothes();
        });
    });

    function updateFilterTags() {
        filterTagsContainer.innerHTML = '';
        activeColorFilters.forEach(filterId => {
            const subColorCircle = document.querySelector(`.sub-color-circle[data-sub-color-id='${filterId}']`);
            if (subColorCircle) {
                const tagName = subColorCircle.getAttribute('title');
                const tag = document.createElement('div');
                tag.className = 'filter-tag';
                tag.textContent = tagName;
                const removeBtn = document.createElement('span');
                removeBtn.className = 'remove-tag';
                removeBtn.textContent = 'x';
                removeBtn.addEventListener('click', () => {
                    // Find the circle and simulate a click to toggle it off
                    subColorCircle.click(); 
                });
                tag.appendChild(removeBtn);
                filterTagsContainer.appendChild(tag);
            }
        });
    }

    function filterClothes() {
        clothCards.forEach(card => {
            const clothColorIds = card.dataset.colorIds.split(',');

            if (activeColorFilters.length === 0) {
                card.style.display = 'block';
                return;
            }

            // Check if the cloth has ALL the active filter colors
            const hasAllColors = activeColorFilters.every(filterId => clothColorIds.includes(filterId));

            if (hasAllColors) {
                card.style.display = 'block';
            } else {
                card.style.display = 'none';
            }
        });
    }
});
