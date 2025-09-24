// 관리자 로그인 JavaScript
function adminLogin(event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    // 간단한 하드코딩된 인증 (실제 환경에서는 서버 인증 필요)
    const validCredentials = [
        { username: 'admin', password: 'admin123' },
        { username: 'manager', password: 'manager123' }
    ];

    const isValid = validCredentials.some(cred =>
        cred.username === username && cred.password === password
    );

    if (isValid) {
        // 로그인 성공
        localStorage.setItem('admin_logged_in', 'true');
        localStorage.setItem('admin_username', username);
        localStorage.setItem('admin_login_time', new Date().toISOString());

        alert('로그인 성공!');
        window.location.href = 'admin.html';
    } else {
        // 로그인 실패
        alert('사용자명 또는 비밀번호가 올바르지 않습니다.');

        // 입력 필드 초기화
        document.getElementById('username').value = '';
        document.getElementById('password').value = '';
        document.getElementById('username').focus();
    }
}

// 페이지 로드시 이미 로그인되어 있는지 확인
document.addEventListener('DOMContentLoaded', function() {
    const isLoggedIn = localStorage.getItem('admin_logged_in');
    const loginTime = localStorage.getItem('admin_login_time');

    // 로그인 세션 확인 (24시간 유효)
    if (isLoggedIn === 'true' && loginTime) {
        const loginDate = new Date(loginTime);
        const now = new Date();
        const hoursDiff = (now - loginDate) / (1000 * 60 * 60);

        if (hoursDiff < 24) {
            // 아직 유효한 세션이면 관리자 페이지로 리다이렉트
            window.location.href = 'admin.html';
        } else {
            // 세션 만료
            localStorage.removeItem('admin_logged_in');
            localStorage.removeItem('admin_username');
            localStorage.removeItem('admin_login_time');
        }
    }

    // 포커스를 사용자명 입력 필드로 설정
    document.getElementById('username').focus();
});

// 엔터 키 처리
document.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        const activeElement = document.activeElement;
        if (activeElement.id === 'username') {
            document.getElementById('password').focus();
        } else if (activeElement.id === 'password') {
            document.querySelector('.login-form').dispatchEvent(new Event('submit'));
        }
    }
});