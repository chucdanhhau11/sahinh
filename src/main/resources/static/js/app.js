
// app.js — dùng chung: session, menu động từ DB, fetch helper, toast.
async function api(path, opts) {
  const r = await fetch(path, Object.assign({ headers: { 'Content-Type': 'application/json' } }, opts || {}));
  if (r.status === 401) { window.location.href = '/login'; throw new Error('UNAUTHORIZED'); }
  return r.json();
}
function toast(msg) { alert(msg); }
async function loadSession() {
  try {
    const r = await api('/api/ZZ/A0/ZZA0_0001/session');
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
    const r = await api('/api/MS/A0/MSA0_0200/menu');
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
