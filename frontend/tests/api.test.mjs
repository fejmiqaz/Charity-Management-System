import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
const source=(await readFile(new URL('../src/api/api.js',import.meta.url),'utf8')).replace('import.meta.env.VITE_API_BASE_URL','undefined');
let sequence=0;
async function client(){return import(`data:text/javascript;base64,${Buffer.from(source+'\n// '+sequence++).toString('base64')}`)}
test('GET requests include session credentials',async t=>{
  const a=await client();let options;
  t.mock.method(globalThis,'fetch',async (_url,o)=>{options=o;return new Response('{"totalMembers":4}')});
  assert.equal((await a.dashboardApi.get()).totalMembers,4);assert.equal(options.credentials,'include');
});
test('write requests acquire CSRF and serialize JSON',async t=>{
  const a=await client(),calls=[];
  t.mock.method(globalThis,'fetch',async(url,options)=>{calls.push({url,options});return url.endsWith('/csrf')?Response.json({headerName:'X-CSRF-TOKEN',token:'test-token'}):new Response(null,{status:204})});
  await a.yearsApi.create({yearValue:2026});assert.equal(calls.length,2);assert.equal(calls[1].options.headers['X-CSRF-TOKEN'],'test-token');assert.equal(calls[1].options.body,'{"yearValue":2026}');
});
test('empty successful responses do not fail JSON parsing',async t=>{
  const a=await client();t.mock.method(globalThis,'fetch',async()=>new Response(''));assert.equal(await a.api('/empty'),undefined);
});
test('validation errors preserve field feedback',async t=>{
  const a=await client();t.mock.method(globalThis,'fetch',async()=>Response.json({message:'Invalid data',fields:{email:'Email is required'}},{status:400}));
  await assert.rejects(a.api('/members'),e=>e.status===400&&e.fields.email==='Email is required');
});
test('network failures provide actionable feedback',async t=>{
  const a=await client();t.mock.method(globalThis,'fetch',async()=>{throw new TypeError('Failed to fetch')});await assert.rejects(a.api('/dashboard'),e=>e.status===0&&e.message.includes('Unable to reach the server'));
});
test('malformed server responses provide clear errors',async t=>{
  const a=await client();t.mock.method(globalThis,'fetch',async()=>new Response('<html>error</html>'));await assert.rejects(a.api('/dashboard'),/unexpected response/);
});
