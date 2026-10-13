const form = document.getElementById('answer-form');
const attack = document.getElementById('attack');
const input = document.getElementById('answer');
const next = document.getElementById('next-stage');
const error = document.getElementById('error');
let cleared = false;
let pending = false;
let questions = [];
let index = 0;
const text = (id, value) => { document.getElementById(id).textContent = value; };
// 各分類の学習本文をまとめ、問題に関係なく同じ書籍を閲覧できるようにする。
const serviceNames = {
  S3: 'Amazon S3', EC2: 'Amazon EC2', VPC: 'Amazon VPC',
  Network: 'ネットワーク・ELB', Lambda: 'AWS Lambda', Database: 'データベース・RDS',
  IAM: 'AWS IAM・セキュリティ', Monitor: '監視・CloudWatch',
  Scaling: 'Auto Scaling・運用', Containers: 'Amazon ECS・コンテナ'
};
function showBookIndex(focus = false) {
  document.getElementById('book-index').hidden = false;
  document.getElementById('book-detail').hidden = true;
  document.querySelector('#answer-help details').open = false;
  if (focus) document.getElementById('book-index-title').focus();
}
function openService(category) {
  const entries = questions.filter(q => q.category === category);
  const first = entries[0];
  text('category', category);
  text('guide-title', serviceNames[category] || first.adventureBook.title);
  text('guide-body', [...new Set(entries.map(q => q.adventureBook.body))].join('\n\n'));
  document.getElementById('official-doc').href = first.adventureBook.officialDocUrl;
  const current = questions[index];
  document.getElementById('answer-help').hidden = current.category !== category;
  text('prompt', current.question);
  text('sample-answer', current.sampleAnswer);
  document.querySelector('#answer-help details').open = false;
  document.getElementById('book-index').hidden = true;
  document.getElementById('book-detail').hidden = false;
  document.getElementById('guide-title').focus();
}
function buildServiceList() {
  const list = document.getElementById('service-list');
  list.replaceChildren();
  for (const category of new Set(questions.map(q => q.category))) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'service-card';
    button.dataset.category = category;
    button.textContent = serviceNames[category] || category;
    button.addEventListener('click', () => openService(category));
    list.append(button);
  }
}
document.getElementById('book-back').addEventListener('click', () => showBookIndex(true));
function showStage() {
  const q = questions[index];
  cleared = false;
  form.dataset.questionId = q.id;
  text('progress', `${index + 1} / ${questions.length}`);
  text('area', q.area);
  text('enemy-name', q.enemy.name);
  const face = document.querySelector('.slime');
  face.textContent = q.enemy.face;
  face.setAttribute('aria-label', q.enemy.name);
  face.classList.remove('defeated');
  text('hp-number', q.enemy.maxHp);
  text('max-hp', q.enemy.maxHp);
  const hp = document.getElementById('hp-bar');
  hp.max = q.enemy.maxHp;
  hp.value = q.enemy.maxHp;
  text('enemy-statement', q.enemy.statement);
  showBookIndex();
  text('battle-result', '');
  text('error', '');
  document.getElementById('clear').hidden = true;
  next.disabled = true;
  input.value = '';
  input.disabled = false;
  attack.disabled = false;
  attack.textContent = '⚔ 攻撃する';
}
// 同じAPIから全問題を取得し、正解時にだけ次へ進む。
attack.disabled = true;
fetch(form.dataset.questionsUrl).then(response => {
  if (!response.ok) throw new Error('問題を読み込めませんでした。ページを再読み込みしてください。');
  return response.json();
}).then(data => {
  if (!Array.isArray(data) || !data.length) throw new Error('問題データがありません。');
  questions = data;
  buildServiceList();
  showStage();
}).catch(failure => { error.textContent = failure.message; });
form.addEventListener('submit', async event => {
  event.preventDefault();
  if (cleared || pending || !questions.length) return;
  const answer = input.value.trim();
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
    text('hp-number', data.enemyHp);
    document.getElementById('hp-bar').value = data.enemyHp;
    text('enemy-statement', data.message);
    const result = document.getElementById('battle-result');
    result.textContent = data.clear ? `⚔ CRITICAL! ${questions[index].enemy.name}に${data.damage}ダメージ！` : 'MISS... 判定できない場合は回答例を確認してください。';
    result.className = data.clear ? 'critical' : 'miss';
    cleared = data.clear;
    input.disabled = cleared;
    if (cleared) {
      document.querySelector('.slime').classList.add('defeated');
      if (index === questions.length - 1) {
        document.querySelector('.columns').hidden = true;
        document.querySelector('.answer-panel').hidden = true;
        const ending = document.getElementById('game-clear');
        ending.hidden = false;
        ending.focus();
      } else {
        text('clear-message', `${questions[index].enemy.name}を倒した！`);
        document.getElementById('clear').hidden = false;
        next.disabled = false;
      }
    }
  } catch (failure) { error.textContent = failure.message; }
  finally {
    pending = false;
    attack.disabled = cleared;
    attack.textContent = cleared ? '討伐完了' : '⚔ 攻撃する';
  }
});
next.addEventListener('click', () => {
  if (!cleared || pending || index >= questions.length - 1) return;
  index++;
  showStage();
  input.focus();
});
document.getElementById('restart').addEventListener('click', () => {
  index = 0;
  document.getElementById('game-clear').hidden = true;
  document.querySelector('.columns').hidden = false;
  document.querySelector('.answer-panel').hidden = false;
  showStage();
  input.focus();
});
