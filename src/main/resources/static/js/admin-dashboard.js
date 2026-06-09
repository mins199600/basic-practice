document.addEventListener('DOMContentLoaded', function () {
    initMainMenu();
    initSidebarMenu();
    initMemberEditButtons();
    initMemberDeleteButtons();
    initPostEditButtons();
    initPopupPreviewButton();
    initModalCloseButtons();
    initModalBackgroundClose();
});

/**
 * 상단 메뉴 클릭 시 화면 전환
 */
function initMainMenu() {
    const mainMenuButtons = document.querySelectorAll('.main-menu-button');

    mainMenuButtons.forEach(function (button) {
        button.addEventListener('click', function () {
            const targetTab = button.dataset.tab;

            removeActiveClass(mainMenuButtons);
            button.classList.add('active');

            showContentSection(targetTab);
        });
    });
}

/**
 * 사이드바 메뉴 클릭 시 화면 전환
 */
function initSidebarMenu() {
    const sidebarButtons = document.querySelectorAll('.sidebar-menu-button');

    sidebarButtons.forEach(function (button) {
        button.addEventListener('click', function () {
            const targetTab = button.dataset.tab;

            removeActiveClass(sidebarButtons);
            button.classList.add('active');

            showContentSection(targetTab);
            syncMainMenu(targetTab);
        });
    });
}

/**
 * 특정 섹션만 보이게 처리
 */
function showContentSection(sectionId) {
    const sections = document.querySelectorAll('.content-section');

    sections.forEach(function (section) {
        section.classList.remove('active');
    });

    const targetSection = document.getElementById(sectionId);

    if (targetSection) {
        targetSection.classList.add('active');
    }
}

/**
 * 상단 메뉴 active 동기화
 */
function syncMainMenu(targetTab) {
    const mainMenuButtons = document.querySelectorAll('.main-menu-button');

    mainMenuButtons.forEach(function (button) {
        button.classList.remove('active');

        if (button.dataset.tab === targetTab) {
            button.classList.add('active');
        }
    });
}

/**
 * active 클래스 제거
 */
function removeActiveClass(elements) {
    elements.forEach(function (element) {
        element.classList.remove('active');
    });
}

/**
 * 회원 수정 버튼 클릭
 */
function initMemberEditButtons() {
    const editButtons = document.querySelectorAll('.btn-edit-member');

    editButtons.forEach(function (button) {
        button.addEventListener('click', function () {
            const name = button.dataset.name;
            const email = button.dataset.email;
            const phone = button.dataset.phone;

            document.getElementById('memberEditName').value = name;
            document.getElementById('memberEditEmail').value = email;
            document.getElementById('memberEditPhone').value = phone;

            openModal('memberEditModal');
        });
    });
}

/**
 * 회원 탈퇴 버튼 클릭
 */
function initMemberDeleteButtons() {
    const deleteButtons = document.querySelectorAll('.btn-delete-member');

    deleteButtons.forEach(function (button) {
        button.addEventListener('click', function () {
            const name = button.dataset.name;

            document.getElementById('deleteMemberName').textContent = name;

            openModal('memberDeleteModal');
        });
    });
}

/**
 * 게시글 수정 버튼 클릭
 */
function initPostEditButtons() {
    const editButtons = document.querySelectorAll('.btn-edit-post');

    editButtons.forEach(function (button) {
        button.addEventListener('click', function () {
            const title = button.dataset.title;
            const content = button.dataset.content;

            document.getElementById('postEditTitle').value = title;
            document.getElementById('postEditContent').value = content;

            openModal('postEditModal');
        });
    });
}

/**
 * 팝업 미리보기 버튼 클릭
 */
function initPopupPreviewButton() {
    const previewButton = document.querySelector('.btn-preview-popup');

    if (previewButton) {
        previewButton.addEventListener('click', function () {
            openModal('popupPreviewModal');
        });
    }
}

/**
 * 모달 열기
 */
function openModal(modalId) {
    const modal = document.getElementById(modalId);

    if (modal) {
        modal.classList.add('show');
    }
}

/**
 * 모달 닫기
 */
function closeModal(modal) {
    modal.classList.remove('show');
}

/**
 * X 버튼, 취소 버튼, 닫기 버튼 클릭 시 모달 닫기
 */
function initModalCloseButtons() {
    const closeButtons = document.querySelectorAll(
        '.btn-modal-close, .btn-cancel'
    );

    closeButtons.forEach(function (button) {
        button.addEventListener('click', function () {
            const modal = button.closest('.modal-background');

            if (modal) {
                closeModal(modal);
            }
        });
    });
}

/**
 * 모달 바깥 영역 클릭 시 닫기
 */
function initModalBackgroundClose() {
    const modalBackgrounds = document.querySelectorAll('.modal-background');

    modalBackgrounds.forEach(function (modal) {
        modal.addEventListener('click', function (event) {
            if (event.target === modal) {
                closeModal(modal);
            }
        });
    });
}
