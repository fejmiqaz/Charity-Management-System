import legacy from './translations.js';
import {homeTranslations} from './home.js';
import additions from './additional.js';
export const locales = {en: 'en-GB', sq: 'sq-AL', fr: 'fr-FR', de: 'de-DE'};
export const normalizeLanguage = language => Object.hasOwn(locales, language) ? language : 'en';
export const catalogs = Object.fromEntries(['sq', 'fr', 'de'].map(language => [language, {...homeTranslations[language], ...legacy[language], ...additions[language]}]));
const aliases = {HEAD:'Head', SUBHEAD:'Subhead', TREASURER:'Treasurer', PROJECT_MANAGER:'Project Manager', EVENT_MANAGER:'Event Manager', VOLUNTEER:'Volunteer', MEMBER:'Member', APPROVED:'Approved', ON_HOLD:'On Hold', PLANNED:'Planned', ONGOING:'Ongoing', FINISHED:'Finished', CANCELLED:'Cancelled', STANDARD:'Standard', REVENUE:'Revenue', NORMAL:'Normal', TASK_BASED:'With tasks', UPCOMING:'Upcoming', PAST:'Past', UNSCHEDULED:'Unscheduled'};
export function translate(language, text, values = {}) {
  if (typeof text !== 'string') return text;
  const key = text.trim();
  const canonical = aliases[key] || key;
  const translated = catalogs[normalizeLanguage(language)]?.[canonical] ?? canonical;
  return text.slice(0, text.indexOf(key)) + translated.replace(/\{(\w+)\}/g, (match, name) => Object.hasOwn(values, name) ? String(values[name]) : match) + text.slice(text.indexOf(key) + key.length);
}
export function formatNumber(language, value, options = {}) {
  const locale = locales[normalizeLanguage(language)];
  const fallback = language === 'sq' && !Intl.NumberFormat.supportedLocalesOf([locale]).length;
  return new Intl.NumberFormat(fallback ? 'de-DE' : locale, {...(fallback ? {useGrouping:'min2'} : {}), ...options}).format(Number(value));
}
export function formatDate(language, value, options = {}) {
  if (!value) return '—';
  // Calendar dates and local server datetimes must not shift with the viewer's timezone.
  const local = /^\d{4}-\d{2}(-\d{2})?(T[\d:.]+)?$/.test(value);
  const normalized = /^\d{4}-\d{2}$/.test(value) ? `${value}-01` : value;
  const date = new Date(local ? `${normalized.includes('T') ? normalized : normalized + 'T00:00:00'}Z` : normalized);
  if (Number.isNaN(date.getTime())) return value;
  const locale = locales[normalizeLanguage(language)];
  const fallback = language === 'sq' && !Intl.DateTimeFormat.supportedLocalesOf([locale]).length;
  const formatter = new Intl.DateTimeFormat(fallback ? 'en-GB' : locale, {...(local ? {timeZone:'UTC'} : {}), ...options});
  if (!fallback) return formatter.format(date);
  // Some embedded browsers omit Albanian ICU data. Keep text Albanian there too.
  const months = {January:'janar',February:'shkurt',March:'mars',April:'prill',May:'maj',June:'qershor',July:'korrik',August:'gusht',September:'shtator',October:'tetor',November:'nëntor',December:'dhjetor',Jan:'jan',Feb:'shk',Mar:'mar',Apr:'pri',Jun:'qer',Jul:'kor',Aug:'gsh',Sep:'sht',Sept:'sht',Oct:'tet',Nov:'nën',Dec:'dhj'};
  const weekdays = {Monday:'e hënë',Tuesday:'e martë',Wednesday:'e mërkurë',Thursday:'e enjte',Friday:'e premte',Saturday:'e shtunë',Sunday:'e diel'};
  return formatter.formatToParts(date).map(part => part.type === 'month' ? months[part.value] || part.value : part.type === 'weekday' ? weekdays[part.value] || part.value : part.value).join('');
}
