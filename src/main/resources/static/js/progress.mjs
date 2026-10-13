export const STORAGE_KEY = 'aws-rpg-progress';
export function shuffle(ids, random = Math.random) {
  const order = [...ids];
  for (let i = order.length - 1; i > 0; i--) {
    const j = Math.floor(random() * (i + 1));
    [order[i], order[j]] = [order[j], order[i]];
  }
  return order;
}
export function freshProgress(ids, version) {
  return { datasetVersion: version, order: shuffle(ids), currentIndex: 0, clearedIds: [], completed: false };
}
export function restoreProgress(saved, ids, version) {
  if (!saved || typeof saved !== 'object') return freshProgress(ids, version);
  const available = new Set(ids);
  const cleared = Array.isArray(saved.clearedIds)
    ? [...new Set(saved.clearedIds.filter(id => available.has(id)))] : [];
  const orderValid = Array.isArray(saved.order) && saved.order.length === ids.length
    && new Set(saved.order).size === ids.length && saved.order.every(id => available.has(id));
  const i = saved.currentIndex;
  const positionValid = Number.isInteger(i) && i >= 0 && i < ids.length;
  const prefix = orderValid && positionValid ? saved.order.slice(0, i) : [];
  const coherent = positionValid && orderValid && typeof saved.completed === 'boolean'
    && cleared.length === saved.clearedIds?.length
    && prefix.every(id => cleared.includes(id))
    && cleared.every(id => prefix.includes(id) || id === saved.order[i])
    && saved.completed === (cleared.length === ids.length);
  if (saved.datasetVersion === version && coherent) {
    return { datasetVersion: version, order: [...saved.order], currentIndex: i,
      clearedIds: cleared, completed: saved.completed };
  }
  // 旧版・破損した順序は再構築するが、有効な正解済みIDは保持する。
  const remaining = shuffle(ids.filter(id => !cleared.includes(id)));
  return { datasetVersion: version, order: [...cleared, ...remaining],
    currentIndex: Math.min(cleared.length, ids.length - 1), clearedIds: cleared,
    completed: remaining.length === 0 };
}
export function markCorrect(state) {
  const id = state.order[state.currentIndex];
  if (!state.clearedIds.includes(id)) state.clearedIds.push(id);
  state.completed = state.clearedIds.length === state.order.length;
}
