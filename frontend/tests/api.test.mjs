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

test('account access preserves password and role endpoint contracts',async t=>{
  const a=await client(),calls=[];
  t.mock.method(globalThis,'fetch',async(url,options)=>{
    calls.push({url,options});
    if(url.endsWith('/csrf'))return Response.json({headerName:'X-CSRF-TOKEN',token:'test-token'});
    if(url.endsWith('/accounts'))return Response.json([{id:2,name:'Member',email:'member@example.org',role:'MEMBER'}]);
    return new Response(null,{status:204});
  });
  assert.equal((await a.accountsApi.list())[0].id,2);
  const password={currentPassword:'old-password',password:'new-password',confirmPassword:'new-password'};
  await a.profileApi.password(password);
  await a.accountsApi.role(2,'TREASURER');
  const passwordCall=calls.find(c=>c.url.endsWith('/profile/password'));
  assert.equal(passwordCall.options.method,'POST');
  assert.deepEqual(JSON.parse(passwordCall.options.body),password);
  const roleCall=calls.find(c=>c.url.endsWith('/accounts/2/role'));
  assert.equal(roleCall.options.method,'PUT');
  assert.deepEqual(JSON.parse(roleCall.options.body),{role:'TREASURER'});
  for(const call of [passwordCall,roleCall]){
    assert.equal(call.options.credentials,'include');
    assert.equal(call.options.headers['X-CSRF-TOKEN'],'test-token');
  }
});

test('Head management and mark-all-read requests preserve credentials and CSRF',async t=>{
  const a=await client(), calls=[];
  t.mock.method(globalThis,'fetch',async(url,options)=>{
    calls.push({url,options});
    if(url.endsWith('/csrf'))return Response.json({headerName:'X-CSRF-TOKEN',token:'test-token'});
    return Response.json({});
  });
  await a.managementApi.activity({page:2,size:20,action:'DELETE',actorId:7});
  await a.managementApi.security();
  await a.managementApi.revokeSession('opaque-reference');
  await a.notificationsApi.readAll();
  assert.match(calls[0].url,/\/management\/activity\?page=2&size=20&action=DELETE&actorId=7$/);
  assert.ok(calls.some(call=>call.url.endsWith('/management/security')));
  for(const endpoint of ['/management/security/sessions/opaque-reference/revoke','/notifications/read-all']){
    const call=calls.find(call=>call.url.endsWith(endpoint));
    assert.equal(call.options.method,'POST');
    assert.equal(call.options.credentials,'include');
    assert.equal(call.options.headers['X-CSRF-TOKEN'],'test-token');
  }
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
