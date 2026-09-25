import test from 'node:test';
import assert from 'node:assert/strict';
import {catalogs,translate,normalizeLanguage,formatDate,formatNumber} from '../src/i18n/index.js';
import {translateError} from '../src/i18n/errors.js';
import {notificationTitle,notificationMessage} from '../src/i18n/notifications.js';
import {auditTranslations} from '../scripts/check-i18n.mjs';

test('all static interface copy is localized, with explicit option values',()=>{
 const {keys,issues}=auditTranslations();assert.ok(keys.length>400);assert.deepEqual(issues,[]);
});
test('all language catalogs contain the same keys and interpolation variables',()=>{
 const placeholders=text=>[...new Set(text.match(/\{\w+\}/g)||[])].sort();
 const keys=Object.keys(catalogs.sq).sort();
 for(const language of ['sq','fr','de']){
  assert.deepEqual(Object.keys(catalogs[language]).sort(),keys);
  for(const key of keys){assert.ok(catalogs[language][key].trim(),`${language}: ${key}`);assert.deepEqual(placeholders(catalogs[language][key]),placeholders(key),`${language}: ${key}`)}
 }
});
test('the formerly English-only dashboard copy has all three translations',()=>{
 for(const language of ['sq','fr','de'])for(const key of ['EVERY CONTRIBUTION COUNTS','A clearer view.','A greater impact.','Growing together','People in your community','Ideas put into action','Contributions recorded','Your impact over time','Follow progress and coordinate people'])assert.notEqual(translate(language,key),key,`${language}: ${key}`);
});
test('dynamic phrases preserve user data and allow different word order',()=>{
 assert.equal(translate('de','Delete {name}?',{name:'Community garden'}),'Community garden löschen?');
 assert.equal(translate('fr','Page {page} of {total}',{page:2,total:5}),'Page 2 sur 5');
 assert.equal(translate('sq','Delete {name}?',{name:'{name} <script>'}),'Të fshihet {name} <script>?');
 assert.equal(translate('fr','Unknown user-provided title'),'Unknown user-provided title');
 assert.equal(translate('de',null),null);
});
test('backend enums translate only their display labels',()=>{
 assert.equal(translate('sq','PROJECT_MANAGER'),'Menaxher projekti');
 assert.equal(translate('de','ON_HOLD'),'Pausiert');
 assert.equal(translate('fr','APPROVED'),'Approuvé');
 assert.equal(translate('en','FINISHED'),'Finished');
 assert.equal(normalizeLanguage('invalid'),'en');
});
test('server validation messages and network errors are localized',()=>{
 for(const language of ['sq','fr','de'])for(const text of ['Name is required','Surname must be between 2 and 50 characters','Password must contain between 8 and 128 characters','Email must contain at most 254 characters','must not be blank','Request failed (403)','Unable to reach the server. Check your connection and try again.','Payment date must be today or earlier.'])assert.notEqual(translateError(language,text),text,`${language}: ${text}`);
 assert.match(translateError('de','Surname must be between 2 and 50 characters'),/Nachname.*2.*50/);
 assert.equal(translateError('de','New server diagnostic'),'New server diagnostic');
});
test('notification templates translate without changing names or descriptions',()=>{
 const text='You have been assigned to "Prepare venue" for Community gathering.\nEvent: 10 Oct 2026, 12:00 (Europe/Skopje)\nTask details: Keep this description.';
 for(const language of ['sq','fr','de']){
  const result=notificationMessage(language,text);
  assert.ok(!result.includes('You have been assigned'));
  assert.ok(!result.includes('Task details:'));
  assert.ok(result.includes('Prepare venue'));assert.ok(result.includes('Keep this description.'));assert.ok(result.includes('Europe/Skopje'));
  assert.notEqual(notificationTitle(language,'Task assigned: Prepare venue'),'Task assigned: Prepare venue');
  assert.notEqual(notificationMessage(language,'Community gathering is tomorrow.\nScheduled for Date to be confirmed.'),'Community gathering is tomorrow.\nScheduled for Date to be confirmed.');
  assert.notEqual(notificationMessage(language,'Community gathering is in 7 days.\nScheduled for Date to be confirmed.'),'Community gathering is in 7 days.\nScheduled for Date to be confirmed.');
 }
 assert.equal(notificationMessage('de','User-written notification'),'User-written notification');
});
test('dates and amounts use the selected locale without shifting calendar dates',()=>{
 assert.match(formatNumber('de',1250.5,{minimumFractionDigits:2}),/1\.250,50/);
 assert.match(formatNumber('fr',1250.5,{minimumFractionDigits:2}),/250,50/);
 assert.equal(formatDate('de','2026-09-01',{dateStyle:'medium'}),'01.09.2026');
 assert.match(formatDate('fr','2026-09',{month:'long',year:'numeric'}),/septembre 2026/);
 assert.equal(formatDate('sq',null),'—');assert.equal(formatDate('de','invalid'),'invalid');
});
