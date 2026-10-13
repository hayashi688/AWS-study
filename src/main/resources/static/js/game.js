const form = document.getElementById('answer-form');
const attack = document.getElementById('attack');
const input = document.getElementById('answer');
let cleared = false;
let pending = false;
form.addEventListener('submit', async (event) => {
  event.preventDefault();
  if (cleared || pending) return;
  const answer = input.value.trim();
  const error = document.getElementById('error');
  error.textContent = '';
  if (!answer) { error.textContent = '回答を入力してください。'; input.focus(); return; }
  pending = true;
  attack.disabled = true;
  attack.textContent = '判定中…';
  try {
    const response = await fetch(form.dataset.answerUrl, {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ questionId: form.dataset.questionId, answer })
    });
    if (!response.ok) throw new Error('回答を送信できませんでした。もう一度試してください。');
    const data = await response.json();
    document.getElementById('hp-number').textContent = data.enemyHp;
    document.getElementById('hp-bar').value = data.enemyHp;
    document.getElementById('enemy-statement').textContent = data.message;
    const result = document.getElementById('battle-result');
    result.textContent = data.result === 'CORRECT' ? `⚔ CRITICAL! S3スライムに${data.damage}ダメージ！` : 'MISS...';
    result.className = data.clear ? 'critical' : 'miss';
    cleared = data.clear;
    document.getElementById('clear').hidden = !cleared;
    input.disabled = cleared;
    if (cleared) document.querySelector('.slime').classList.add('defeated');
  } catch (failure) {
    error.textContent = failure.message;
  } finally {
    pending = false;
    attack.disabled = cleared;
    attack.textContent = cleared ? '討伐完了' : '⚔ 攻撃する';
  }
});
