
// exam.js — chạy thi dùng chung cho 3 luồng.
// Luật: điểm do SERVER tính (POST /log), frontend chỉ hiển thị.
// Luồng 1: bấm Bắt đầu 1 lần -> tự đếm giờ, tự chuyển bước; hết giờ ở
//   bước nào -> dừng ngay, gọi /finish failed=true, làm lại từ đầu.
// Luồng 2/3: thầy bấm + (đúng), - (chọn lỗi), Qua bài. Đồng hồ chỉ hiển thị.
(function () {
  let plan = null, idx = 0, timer = null, remaining = 0, sessionId = null;
  let score = 100, muted = false, audioCtx = null, flow = 1, presetId = 1;
  let transmission = 'MANUAL';
  // Tình huống nguy hiểm (S07-D): chỉ Luồng 2/3, chỉ khi preset bật (mặc định TẮT).
  let hazardIdx = -1, hazardDone = false, hazardTimer = null, hazardLeft = 0;
  function flowBase(f) { return '/api/SH/A0/' + ['SHA0_0400', 'SHA0_0500', 'SHA0_0600'][f - 1]; }

  function el(id) { return document.getElementById(id); }
  // Âm thanh thi (S10): file mp3 giọng nữ thu sẵn cho 14 mục + tình huống
  // nguy hiểm (static/audio/). Loa thi thật đọc "Bài thi tiếp theo, bài thi
  // <tên>" khi chuyển bài (bài 1 không có "tiếp theo"), nên lesson-02…11 có
  // tiền tố đó trong file, còn chữ hiện trên màn hình vẫn là tên bài.
  // Không khớp (bước chi tiết, câu tùy chỉnh) -> giọng máy speechSynthesis
  // như cũ. File lỗi/mất -> cũng về giọng máy.
  const AUDIO_MAP = {
    'Bài thi xuất phát': 'lesson-01.mp3',
    'Bài thi dừng xe nhường đường cho người đi bộ': 'lesson-02.mp3',
    'Bài thi dừng xe và khởi hành ngang dốc': 'lesson-03.mp3',
    'Bài thi qua vệt bánh xe và đường hẹp vuông góc': 'lesson-04.mp3',
    'Bài thi qua ngã tư có đèn tín hiệu': 'lesson-05.mp3',
    'Bài thi đường vòng quanh co': 'lesson-06.mp3',
    'Bài thi ghép xe dọc vào nơi đỗ': 'lesson-07.mp3',
    'Bài thi tạm dừng ở chỗ có đường sắt chạy qua': 'lesson-08.mp3',
    'Bài thi thay đổi số trên đường bằng': 'lesson-09.mp3',
    'Bài thi ghép xe ngang vào nơi đỗ': 'lesson-10.mp3',
    'Bài thi kết thúc': 'lesson-11.mp3',
    'Tình huống nguy hiểm': 'dangerous.mp3'
  };
  let currentAudio = null;
  // File mp3 theo số bài (khớp LESSON_NO sau V5: 14 mục). Ngã tư (2)/(3)/(4)
  // (bài 7/9/13) dùng chung file lesson-05.mp3 vì loa đọc cùng 1 câu.
  const LESSON_AUDIO = { 1: 'lesson-01.mp3', 2: 'lesson-02.mp3', 3: 'lesson-03.mp3', 4: 'lesson-04.mp3', 5: 'lesson-05.mp3', 6: 'lesson-06.mp3', 7: 'lesson-05.mp3', 8: 'lesson-07.mp3', 9: 'lesson-05.mp3', 10: 'lesson-08.mp3', 11: 'lesson-09.mp3', 12: 'lesson-10.mp3', 13: 'lesson-05.mp3', 14: 'lesson-11.mp3' };
  function stopAudio() {
    if (currentAudio) { try { currentAudio.pause(); } catch (e) {} currentAudio = null; }
    if (window.speechSynthesis) { try { window.speechSynthesis.cancel(); } catch (e) {} }
  }
  function playFile(file, fallback) {
    try {
      const a = new Audio('/audio/' + file);
      currentAudio = a;
      a.onerror = function () { currentAudio = null; if (fallback) fallback(); };
      const p = a.play();
      if (p && p.catch) p.catch(function () { currentAudio = null; if (fallback) fallback(); });
    } catch (e) { currentAudio = null; if (fallback) fallback(); }
  }
  function speakTts(text) {
    if (!('speechSynthesis' in window)) return;
    try {
      const u = new SpeechSynthesisUtterance(text);
      u.lang = 'vi-VN';
      window.speechSynthesis.speak(u);
    } catch (e) { /* chỉ hiện chữ */ }
  }
  // Đọc loa cho bước/bài hiện tại (S10b).
  // - Câu chuẩn trong AUDIO_MAP -> phát mp3 (Luồng 3, hoặc bước trùng tên bài).
  // - Bước chi tiết: vừa sang bài mới -> phát mp3 của bài đó (như thi thật,
  //   loa chỉ đọc tên bài khi vào bài); các bước sau trong cùng bài -> giọng
  //   máy đọc tên bước như cũ. Trước S10b, Luồng 1/2 toàn là bước chi tiết
  //   nên không bao giờ nghe được mp3 mới.
  function announceItem() {
    const it = plan.items[idx];
    if (AUDIO_MAP[it.announce]) { speak(it.announce); return; }
    const prev = idx > 0 ? plan.items[idx - 1] : null;
    if ((!prev || prev.lessonNo !== it.lessonNo) && LESSON_AUDIO[it.lessonNo] && !muted) {
      const a = el('announce'); if (a) a.textContent = it.announce || '';
      stopAudio();
      playFile(LESSON_AUDIO[it.lessonNo], function () { speakTts(it.announce); });
      return;
    }
    speak(it.announce);
  }
  function speak(text) {
    const a = el('announce'); if (a) a.textContent = text || '';
    if (muted || !text) return;
    const file = AUDIO_MAP[text];
    stopAudio();
    if (file) { playFile(file, function () { speakTts(text); }); return; }
    speakTts(text);
  }
  function beepOsc() {
    try {
      audioCtx = audioCtx || new (window.AudioContext || window.webkitAudioContext)();
      const o = audioCtx.createOscillator(), g = audioCtx.createGain();
      o.frequency.value = 880; o.connect(g); g.connect(audioCtx.destination);
      o.start(); g.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + 0.15);
      o.stop(audioCtx.currentTime + 0.15);
    } catch (e) { /* không có audio */ }
  }
  function beep() {
    if (muted) return;
    playFile('beep.mp3', beepOsc);
  }
  function show() {
    if (!plan) return;
    const it = plan.items[idx];
    if (!it) { finish(false, null, null); return; }
    el('lesson-name').textContent = 'Bài ' + it.lessonNo + (it.stepNo ? ' — bước ' + it.stepNo : '') + ': ' + it.name;
    el('score').textContent = score;
    el('progress-bar').style.width = Math.round(idx / plan.items.length * 100) + '%';
    el('progress-text').textContent = (idx + 1) + ' / ' + plan.items.length;
    remaining = it.durationSeconds;
    renderTimer();
    announceItem();
    beep();
    clearInterval(timer);
    // Tình huống nguy hiểm: tới đúng bước/bài đã chọn thì dừng đồng hồ,
    // hiện đèn đỏ + đọc loa, chờ thầy bấm nút nguy hiểm.
    if (flow !== 1 && plan.dangerousEnabled && idx === hazardIdx && !hazardDone) {
      triggerHazard();
      return;
    }
    timer = setInterval(function () {
      remaining--;
      renderTimer();
      if (remaining <= 0) {
        clearInterval(timer);
        if (flow === 1) {
          // Luồng 1: hết giờ = sai -> dừng ngay, làm lại từ đầu.
          finish(true, it.name, 'Hết giờ ở ' + it.name + ' — làm lại từ đầu');
        }
        // Luồng 2/3: đồng hồ chỉ hiển thị, thầy tự quyết định bấm + / -.
      }
    }, 1000);
  }
  function renderTimer() {
    const t = el('timer');
    t.textContent = Math.max(0, remaining);
    t.classList.toggle('low', remaining <= 5);
  }
  function triggerHazard() {
    const ov = el('hazard-overlay');
    if (!ov) { hazardDone = true; show(); return; }
    hazardLeft = plan.dangerousTimeoutSeconds || 5;
    el('hazard-count').textContent = hazardLeft;
    ov.style.display = 'block';
    speak('Tình huống nguy hiểm');
    beep();
    clearInterval(hazardTimer);
    hazardTimer = setInterval(function () {
      hazardLeft--;
      el('hazard-count').textContent = Math.max(0, hazardLeft);
      if (hazardLeft <= 0) {
        clearInterval(hazardTimer);
        hazardDone = true;
        ov.style.display = 'none';
        // Hết giờ không bấm nút: trừ 10 điểm theo lỗi DANGEROUS đã seed.
        if (plan.dangerousErrorRuleId) { applyError(plan.dangerousErrorRuleId); }
        else { show(); }
      }
    }, 1000);
  }
  window.pressHazard = function () {
    clearInterval(hazardTimer);
    hazardDone = true;
    const ov = el('hazard-overlay');
    if (ov) ov.style.display = 'none';
    show(); // Bấm kịp: không trừ điểm, chạy lại đồng hồ của bài/bước hiện tại.
  };
  async function finish(failed, failedStep, failedReason) {
    clearInterval(timer);
    clearInterval(hazardTimer);
    if (!sessionId) return;
    const r = await api(flowBase(flow) + '/finish', {
      method: 'POST', body: JSON.stringify({ sessionId: sessionId, failed: failed, failedStep: failedStep, failedReason: failedReason })
    });
    const d = r.data || {};
    el('exam-box').style.display = 'none';
    const res = el('result-box'); res.style.display = 'block';
    el('result-text').textContent = d.resultText || (d.passed ? 'ĐẠT' : 'TRƯỢT');
    el('result-text').className = d.passed ? 'result-pass' : 'result-fail';
    el('result-score').textContent = 'Điểm: ' + d.score + ' / 100';
    el('result-reason').textContent = d.failedReason || '';
    el('result-link').href = '/SH/SHA0_0700?sessionId=' + sessionId;
    // S10: đỗ -> loa chúc mừng như sân thi thật ("Chúc mừng bạn đã thi đạt...").
    if (d.passed && !muted) playFile('result-pass.mp3');
  }
  async function logCurrent(errorRuleId) {
    const it = plan.items[idx];
    const r = await api(flowBase(flow) + '/log', {
      method: 'POST',
      body: JSON.stringify({ sessionId: sessionId, lessonNo: it.lessonNo, stepNo: it.stepNo, errorRuleId: errorRuleId || null })
    });
    if (r.code !== 200) { toast(r.message || 'Lỗi ghi điểm'); return null; }
    score = r.data.score;
    el('score').textContent = score;
    return r.data;
  }
  function next() { idx++; if (idx >= plan.items.length) { finish(false, null, null); } else { show(); } }

  window.startExam = async function (f, preset) {
    flow = f; presetId = preset;
    const studentName = (el('student-name') || {}).value || '';
    transmission = (el('transmission') || {}).value || 'MANUAL';
    const base = flowBase(f);
    const p = await api(base + '/plan?presetId=' + presetId + '&transmission=' + transmission);
    if (p.code !== 200) { toast(p.message || 'Không tải được kế hoạch thi'); return; }
    plan = p.data;
    const s = await api(base + '/start', { method: 'POST', body: JSON.stringify({ presetId: presetId, studentName: studentName, transmission: transmission }) });
    if (s.code !== 200) { toast(s.message || 'Không tạo được phiên thi'); return; }
    sessionId = s.data.sessionId; score = 100; idx = 0;
    // Tình huống nguy hiểm: chọn ngẫu nhiên 1 điểm giữa bài thi (Luồng 2/3, khi preset bật).
    hazardDone = false; hazardIdx = -1;
    if (flow !== 1 && plan.dangerousEnabled && plan.items.length >= 5) {
      hazardIdx = 2 + Math.floor(Math.random() * (plan.items.length - 4));
    }
    el('setup-box').style.display = 'none';
    el('exam-box').style.display = 'block';
    el('flow1-only').style.display = flow === 1 ? 'block' : 'none';
    el('flow23-only').style.display = flow === 1 ? 'none' : 'block';
    show();
  };
  // Luồng 1: học viên hoàn thành bước trước khi hết giờ -> qua bước tiếp.
  window.completeStep = async function () {
    if (flow !== 1) return;
    await logCurrent(null);
    next();
  };
  // Luồng 2/3: bấm + là đúng, qua bước/bài tiếp.
  window.markCorrect = async function () { await logCurrent(null); next(); };
  window.nextItem = function () { next(); };
  // Luồng 2/3: bấm - -> mở danh sách lỗi của bài hiện tại để chọn.
  window.openErrors = function () {
    const it = plan.items[idx];
    const list = plan.errors.filter(function (e) { return e.LESSON_NO === 0 || e.LESSON_NO === it.lessonNo; });
    const box = el('error-list');
    box.innerHTML = list.map(function (e) {
      return '<div class="error-item' + (e.IS_LIET ? ' liet' : '') + '" onclick="applyError(' + e.ID + ')">'
        + e.NAME + ' — ' + (e.IS_LIET ? 'LIỆT (rớt ngay)' : ('trừ ' + (e.DEDUCT_POINTS || 0) + ' điểm')) + '</div>';
    }).join('') || '<p>Không có lỗi nào cho bài này.</p>';
    el('error-modal').classList.add('open');
  };
  window.applyError = async function (errorRuleId) {
    el('error-modal').classList.remove('open');
    const d = await logCurrent(errorRuleId);
    if (!d) return;
    if (d.isLiet) { finish(true, plan.items[idx].name, 'RỚT do lỗi liệt'); return; }
    next();
  };
  window.closeErrors = function () { el('error-modal').classList.remove('open'); };
  window.toggleMute = function () {
    muted = !muted;
    const b = el('mute-btn'); if (b) b.textContent = muted ? 'Bật loa' : 'Tắt loa';
    if (muted) stopAudio();
  };
  window.restartExam = function () { window.location.reload(); };
})();
