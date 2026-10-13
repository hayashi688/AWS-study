import { STORAGE_KEY, freshProgress, restoreProgress, markCorrect } from './progress.mjs';
const form = document.getElementById('answer-form');
const attack = document.getElementById('attack');
const input = document.getElementById('answer');
const next = document.getElementById('next-stage');
const error = document.getElementById('error');
const text = (id, value) => { document.getElementById(id).textContent = value; };
let dataset;
let version;
let state;
let byId;
let pending = false;
let selectedService;
const current = () => byId.get(state.order[state.currentIndex]);
function saveProgress() {
  try { localStorage.setItem(STORAGE_KEY, JSON.stringify(state)); }
  catch { text('storage-notice', 'このブラウザでは進捗を保存できません。再読み込みすると進捗が戻る場合があります。'); }
}
function updateProgress() {
  const count = new Set(state.clearedIds).size;
  text('progress', `クリア済み：${count} / ${state.order.length}問`);
  text('progress-rate', `${Math.round(count / state.order.length * 1000) / 10}%`);
}
function bookPage(id) {
  for (const page of ['book-index', 'book-sections', 'book-detail']) {
    document.getElementById(page).hidden = page !== id;
  }
}
function buildServiceList() {
  const list = document.getElementById('service-list');
  list.replaceChildren();
  for (const service of dataset.serviceCatalog) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'service-card';
    button.dataset.service = service.serviceName;
    const relevant = current().adventureBook.serviceName === service.serviceName;
    button.textContent = service.serviceName + (relevant ? ' ★ 今回の問題' : '');
    button.classList.toggle('current-service', relevant);
    button.addEventListener('click', () => openService(service));
    list.append(button);
  }
}
function showBookIndex(focus = false) {
  bookPage('book-index');
  document.querySelector('#answer-help details').open = false;
  if (focus) document.getElementById('book-index-title').focus();
}
function openService(service) {
  selectedService = service;
  text('service-title', service.serviceName);
  const list = document.getElementById('section-list');
  list.replaceChildren();
  for (const section of dataset.navigationSections) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'service-card';
    button.dataset.section = section.id;
    button.textContent = section.label;
    button.title = section.description;
    button.addEventListener('click', () => openSection(section));
    list.append(button);
  }
  bookPage('book-sections');
  document.getElementById('service-title').focus();
}
function openSection(section) {
  text('category', selectedService.serviceName);
  text('guide-title', section.label);
  const container = document.getElementById('guide-entries');
  container.replaceChildren();
  const entries = selectedService.sections.find(item => item.id === section.id)?.entries || [];
  if (!entries.length) {
    const empty = document.createElement('p');
    empty.textContent = 'この項目の資料はまだありません。他の項目を選んでください。';
    container.append(empty);
  }
  for (const entry of entries) {
    const article = document.createElement('article');
    article.className = 'book-entry';
    const title = document.createElement('h4'); title.textContent = entry.title;
    const body = document.createElement('p'); body.textContent = entry.body;
    const link = document.createElement('a');
    link.textContent = 'AWS公式ドキュメントを開く ↗';
    link.href = entry.officialDocUrl; link.target = '_blank'; link.rel = 'noopener noreferrer';
    article.append(title, body, link); container.append(article);
  }
  const q = current();
  document.getElementById('answer-help').hidden = q.adventureBook.serviceName !== selectedService.serviceName;
  text('prompt', q.question);
  text('sample-answer', q.sampleAnswer);
  document.querySelector('#answer-help details').open = false;
  bookPage('book-detail');
  document.getElementById('guide-title').focus();
}
document.getElementById('sections-back').addEventListener('click', () => showBookIndex(true));
document.getElementById('book-back').addEventListener('click', () => openService(selectedService));
function showStage() {
  updateProgress();
  document.querySelector('.columns').hidden = state.completed;
  document.querySelector('.answer-panel').hidden = state.completed;
  document.getElementById('game-clear').hidden = !state.completed;
  if (state.completed) { document.getElementById('game-clear').focus(); return; }
  const q = current();
  const cleared = state.clearedIds.includes(q.id);
  form.dataset.questionId = q.id;
  text('area', q.area);
  text('enemy-name', q.enemy.name);
  const face = document.querySelector('.slime');
  face.textContent = q.enemy.face;
  face.setAttribute('aria-label', q.enemy.name);
  face.classList.toggle('defeated', cleared);
  text('hp-number', cleared ? 0 : q.enemy.maxHp);
  text('max-hp', q.enemy.maxHp);
  const hp = document.getElementById('hp-bar'); hp.max = q.enemy.maxHp; hp.value = cleared ? 0 : q.enemy.maxHp;
  text('enemy-statement', cleared ? 'ぐっ……正解だ……！' : q.enemy.statement);
  text('battle-result', cleared ? `⚔ CRITICAL! ${q.enemy.name}に${q.enemy.maxHp}ダメージ！` : '');
  document.getElementById('battle-result').className = cleared ? 'critical' : '';
  text('clear-message', `${q.enemy.name}を倒した！`);
  document.getElementById('clear').hidden = !cleared;
  next.disabled = !cleared;
  input.value = ''; input.disabled = cleared;
  attack.disabled = cleared; attack.textContent = cleared ? '討伐完了' : '⚔ 攻撃する';
  text('error', '');
  buildServiceList(); showBookIndex();
}
attack.disabled = true;
fetch(form.dataset.questionsUrl).then(response => {
  if (!response.ok) throw new Error('問題を読み込めませんでした。ページを再読み込みしてください。');
  return response.json();
}).then(payload => {
  dataset = payload.data; version = payload.datasetVersion;
  if (!dataset.questions?.length || !Array.isArray(dataset.serviceCatalog) || !Array.isArray(dataset.navigationSections))
    throw new Error('問題データが不正です。');
  if (dataset.quizSettings.order !== 'shuffle-without-replacement' || dataset.quizSettings.scope !== 'all'
      || dataset.quizSettings.questionsPerRun !== dataset.questions.length)
    throw new Error('この出題設定には対応していません。');
  byId = new Map(dataset.questions.map(q => [q.id, q]));
  let saved;
  try { const raw = localStorage.getItem(STORAGE_KEY); saved = raw ? JSON.parse(raw) : null; }
  catch { text('storage-notice', '保存済みの進捗を読み込めなかったため、新しい冒険を開始します。'); }
  state = restoreProgress(saved, [...byId.keys()], version);
  if (saved && JSON.stringify(state) !== JSON.stringify(saved))
    text('storage-notice', '問題データの更新または保存内容の不整合に合わせて進捗を復旧しました。有効なクリア済みIDは保持しています。');
  saveProgress(); showStage();
}).catch(failure => { error.textContent = failure.message; });
form.addEventListener('submit', async event => {
  event.preventDefault();
  if (!state || pending || state.completed || state.clearedIds.includes(current().id)) return;
  const answer = input.value.trim(); text('error', '');
  if (!answer) { error.textContent = '回答を入力してください。'; input.focus(); return; }
  pending = true; attack.disabled = true; attack.textContent = '判定中…';
  try {
    const response = await fetch(form.dataset.answerUrl, {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ questionId: current().id, answer })
    });
    if (!response.ok) throw new Error('回答を送信できませんでした。もう一度試してください。');
    const data = await response.json();
    if (data.clear) {
      markCorrect(state); saveProgress(); showStage();
    } else {
      text('enemy-statement', data.message);
      text('battle-result', 'MISS... 冒険者の書で調べて、もう一度答えよう。');
      document.getElementById('battle-result').className = 'miss';
    }
  } catch (failure) { error.textContent = failure.message; }
  finally {
    pending = false;
    const cleared = state.clearedIds.includes(current().id);
    attack.disabled = cleared; attack.textContent = cleared ? '討伐完了' : '⚔ 攻撃する';
  }
});
next.addEventListener('click', () => {
  if (!state || pending || state.completed || !state.clearedIds.includes(current().id)) return;
  state.currentIndex++; saveProgress(); showStage(); input.focus();
});
document.getElementById('restart').addEventListener('click', () => {
  state = freshProgress([...byId.keys()], version); saveProgress(); showStage(); input.focus();
});
