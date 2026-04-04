/**
 * main.js - Script dùng chung (placeholder)
 * Các trang có thể thêm script riêng qua fragment footer-scripts.
 */
(function () {
    'use strict';

    function setupDraggableCarousels() {
        document.querySelectorAll('.carousel-track').forEach(track => {
            let isPointerDown = false;
            let isDragging = false;
            let startX = 0;
            let startScrollLeft = 0;
            let pointerId = null;

            const stopDragging = () => {
                if (!isPointerDown && !isDragging) {
                    return;
                }

                isPointerDown = false;
                track.classList.remove('dragging');

                if (pointerId !== null) {
                    try {
                        track.releasePointerCapture(pointerId);
                    } catch (error) {
                        // Ignore pointer capture release issues.
                    }
                }

                pointerId = null;

                window.setTimeout(() => {
                    isDragging = false;
                }, 0);
            };

            track.addEventListener('pointerdown', event => {
                if (event.button !== 0) {
                    return;
                }

                isPointerDown = true;
                isDragging = false;
                startX = event.clientX;
                startScrollLeft = track.scrollLeft;
                pointerId = event.pointerId;
                track.classList.add('dragging');
                track.setPointerCapture(pointerId);
            });

            track.addEventListener('pointermove', event => {
                if (!isPointerDown) {
                    return;
                }

                const deltaX = event.clientX - startX;
                if (Math.abs(deltaX) > 4) {
                    isDragging = true;
                }

                if (isDragging) {
                    event.preventDefault();
                    track.scrollLeft = startScrollLeft - deltaX;
                }
            });

            track.addEventListener('pointerup', stopDragging);
            track.addEventListener('pointercancel', stopDragging);
            track.addEventListener('lostpointercapture', stopDragging);

            track.addEventListener('click', event => {
                if (!isDragging) {
                    return;
                }

                event.preventDefault();
                event.stopPropagation();
            }, true);
        });
    }

    document.addEventListener('DOMContentLoaded', setupDraggableCarousels);
    // Placeholder: có thể thêm menu mobile, scroll header, ...
})();
