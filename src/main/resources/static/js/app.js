
// app.js — dùng chung: session, menu động từ DB, fetch helper, toast.
async function api(path, opts) {
  const o = opts || {};
  const r = await fetch(path, Object.assign({ headers: { 'Content-Type': 'application/json' } }, o));
  if (r.status === 401) {
    // S08: KHÔNG chuyển về /login khi đã ở trang login ("/" hoặc "/login").
    // Trước đây loadSession()/loadMenu() trên trang login nhận 401 rồi tự
    // gán location.href = '/login' -> trang tải lại -> lại gọi session ->
    // lại 401 -> lặp vô hạn, không nhập hay bấm gì được.
    const onLoginPage = window.location.pathname === '/login' || window.location.pathname === '/';
    if (!o.noRedirect && !onLoginPage) { window.location.href = '/login'; }
    throw new Error('UNAUTHORIZED');
  }
  return r.json();
}
// S09: toast KHONG dung alert() nua — alert() la modal chan ca trang
// (dong ho thi cung dung lai, thay phai bam OK moi tiep tuc duoc).
// Toast div tu hien 4 giay roi tu an, khong chan thao tac.
function toast(msg) {
  if (!document.body) { alert(msg); return; }
  let t = document.getElementById('toast');
  if (!t) {
    t = document.createElement('div');
    t.id = 'toast';
    t.style.cssText = 'position:fixed;left:50%;bottom:24px;transform:translateX(-50%);background:#1f2937;color:#fff;padding:10px 16px;border-radius:8px;z-index:9999;max-width:90vw;box-shadow:0 4px 16px rgba(0,0,0,.25);font-size:14px';
    document.body.appendChild(t);
  }
  t.textContent = msg;
  t.style.display = 'block';
  clearTimeout(t._h);
  t._h = setTimeout(function () { t.style.display = 'none'; }, 4000);
}
async function loadSession() {
  try {
    const r = await api('/api/ZZ/A0/ZZA0_0001/session', { noRedirect: true });
    if (r.code === 200 && r.data) {
      const el = document.getElementById('current-user');
      if (el) el.textContent = r.data.FULL_NAME + ' (' + r.data.ROLE_CODE + ')';
      window.SH_USER = r.data;
      return r.data;
    }
  } catch (e) { /* chưa đăng nhập */ }
  return null;
}
async function loadMenu() {
  const nav = document.getElementById('main-nav');
  if (!nav) return;
  try {
    const r = await api('/api/MS/A0/MSA0_0200/menu', { noRedirect: true });
    if (r.code === 200) {
      nav.innerHTML = r.data.map(function (m) {
        return '<a href="' + m.PAGE_URL + '">' + m.FUNCTION_NAME + '</a>';
      }).join('');
    }
  } catch (e) { /* menu mặc định trong HTML */ }
}
async function logout() {
  await api('/api/ZZ/A0/ZZA0_0001/logout', { method: 'POST', body: '{}' });
  window.location.href = '/login';
}
document.addEventListener('DOMContentLoaded', function () {
  loadSession();
  loadMenu();
});
