import {useLanguage} from '../context/LanguageContext';
import {formatDate} from '../i18n/index.js';
import {translateError} from '../i18n/errors.js';
export function LocalizedDate({value, month=false, time=false}) {
  const {language}=useLanguage();
  return formatDate(language,value,month?{month:'long',year:'numeric'}:time?{dateStyle:'medium',timeStyle:'short'}:{dateStyle:'medium'});
}
export function ErrorText({children}) {
  const {language}=useLanguage();
  return translateError(language,children);
}
