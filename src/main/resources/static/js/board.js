// 로그인한 사용자 정보 가져오기
fetch('/api/user-info')
    .then(response => response.json())
    .then(data => {
        console.log('사용자 정보:', data);

        const nickname = data.nickName || '사용자';
        const email = data.email || '이메일 정보 없음';

        document.getElementById('nickname').textContent = nickname;
        document.getElementById('email').textContent = email;
    })
    .catch(error => {
        console.error('사용자 정보 불러오기 실패:', error);

        document.getElementById('nickname').textContent = '사용자';
        document.getElementById('email').textContent = '이메일 정보를 불러오지 못했습니다.';
    });

// 로그아웃 함수
function logout() {
    if (confirm('정말 로그아웃 하시겠습니까?')) {
        window.location.href = '/logout';
    }
}

// 필터 드롭다운
const filterToggle   = document.getElementById('filterToggle');
const filterDropdown = document.getElementById('filterDropdown');

filterToggle.addEventListener('click', function(e) {
    e.stopPropagation();
    filterDropdown.classList.toggle('open');
});

document.addEventListener('click', function() {
    filterDropdown.classList.remove('open');
});

filterDropdown.addEventListener('click', function(e) {
    e.stopPropagation();
});

// 적용 버튼
document.getElementById('filterApply').addEventListener('click', function() {
    const selectedCategory = document.querySelector('input[name="filterCategory"]:checked');
    const selectedSort     = document.querySelector('input[name="filterSort"]:checked');

    document.getElementById('hiddenCategory').value = selectedCategory ? selectedCategory.value : '';
    document.getElementById('hiddenSort').value     = selectedSort ? selectedSort.value : 'latest';

    document.getElementById('searchForm').submit();
});
