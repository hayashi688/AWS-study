import test from 'node:test';
import assert from 'node:assert/strict';
import { shuffle, freshProgress, restoreProgress, markCorrect } from '../src/main/resources/static/js/progress.mjs';
const ids = Array.from({length:200}, (_,i)=>`AWS-${String(i+1).padStart(3,'0')}`);
test('Fisher-Yates returns a permutation without modifying source', () => {
  const shuffled=shuffle(ids,()=>0);
  assert.equal(new Set(shuffled).size,200);
  assert.deepEqual([...shuffled].sort(),ids);
  assert.notDeepEqual(shuffled,ids);
  assert.equal(ids[0],'AWS-001');
});
test('correct answer/reload preserves HP-clear position and does not double count',()=>{
  const state=freshProgress(ids,'v2');markCorrect(state);markCorrect(state);
  assert.equal(state.clearedIds.length,1);
  assert.deepEqual(restoreProgress(JSON.parse(JSON.stringify(state)),ids,'v2'),state);
  state.currentIndex++;
  assert.deepEqual(restoreProgress(state,ids,'v2'),state);
});
test('migration preserves 100-question clears and adds all new IDs exactly once',()=>{
  const state={datasetVersion:'v1',order:ids.slice(0,100),currentIndex:37,clearedIds:ids.slice(0,37),completed:false};
  const migrated=restoreProgress(state,ids,'v2');
  assert.deepEqual(migrated.clearedIds,state.clearedIds);
  assert.equal(migrated.currentIndex,37);
  assert.equal(new Set(migrated.order).size,200);
  assert.equal(migrated.completed,false);
});
test('corrupt order, duplicate IDs, removed IDs, and invalid positions recover safely',()=>{
  for(const malformed of [null,{},[],{order:[ids[0],ids[0]],currentIndex:-1,clearedIds:[ids[0],ids[0],'missing']},
    {order:ids,currentIndex:999,completed:true,clearedIds:[ids[0]]}]){
    const state=restoreProgress(malformed,ids,'v2');
    assert.equal(state.order.length,200);assert.equal(new Set(state.order).size,200);
    assert.ok(state.currentIndex>=0 && state.currentIndex<200);
    assert.equal(state.completed,false);assert.ok(!state.clearedIds.includes('missing'));
  }
});
test('completed run restores and replay starts with zero clears',()=>{
  const state={datasetVersion:'v2',order:ids,currentIndex:199,clearedIds:[...ids],completed:true};
  assert.deepEqual(restoreProgress(state,ids,'v2'),state);
  const replay=freshProgress(ids,'v2');assert.equal(replay.clearedIds.length,0);assert.equal(replay.completed,false);
});
