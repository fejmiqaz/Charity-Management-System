import {translate} from './index.js';
export function translateError(language, message) {
  if (typeof message !== 'string') return message;
  const translated = translate(language, message);
  if (translated !== message) return translated;
  const patterns = [
    [/^(.+) is required$/, '{field} is required', ['field']],
    [/^(.+) must be between (\d+) and (\d+) characters$/, '{field} must be between {min} and {max} characters', ['field','min','max']],
    [/^(.+) must contain between (\d+) and (\d+) characters$/, '{field} must be between {min} and {max} characters', ['field','min','max']],
    [/^(.+) (?:cannot be longer than|must contain at most) (\d+) characters$/, '{field} must contain at most {max} characters', ['field','max']],
    [/^size must be between (\d+) and (\d+)$/, 'Use between {min} and {max} characters.', ['min','max']],
    [/^Request failed \((\d+)\)$/, 'Request failed ({status})', ['status']],
  ];
  for (const [pattern,key,names] of patterns) {
    const match=pattern.exec(message);
    if(match) return translate(language,key,Object.fromEntries(names.map((name,i)=>[name,name==='field'?translate(language,match[i+1]):match[i+1]])));
  }
  return translated;
}
