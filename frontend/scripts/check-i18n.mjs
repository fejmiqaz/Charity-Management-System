import fs from 'node:fs';
import path from 'node:path';
import {parse} from '@babel/parser';
import {catalogs} from '../src/i18n/index.js';
const neutral = new Set(['Charity','♥ Charity','♥   Charity','Ctrl K','EN','SQ','FR','DE','EUR','MKD','CHF','ID']);
const readable = text => /[A-Za-z]/.test(text) && !neutral.has(text.trim());
function walk(node, visitor, parent) {
  if (!node || typeof node !== 'object') return;
  if (node.type) visitor(node, parent);
  for (const [key,value] of Object.entries(node)) {
    if (['loc','extra','tokens','comments'].includes(key)) continue;
    if (Array.isArray(value)) value.forEach(child => walk(child,visitor,node));
    else if (value && typeof value === 'object') walk(value,visitor,node);
  }
}
export function auditTranslations() {
  const keys=new Set(), issues=[];
  for(const relative of fs.readdirSync('src',{recursive:true}).filter(file=>file.endsWith('.jsx'))) {
    const file=path.join('src',relative), ast=parse(fs.readFileSync(file,'utf8'),{sourceType:'module',plugins:['jsx']});
    const add=text=>{if(readable(text))keys.add(text.trim());};
    walk(ast,(node,parent)=>{
      const location=`${file}:${node.loc?.start.line}`;
      if(node.type==='JSXText') {
        const text=node.value.replace(/\s+/g,' ').trim();
        if(!readable(text))return;
        if(parent?.openingElement?.name?.name==='T')add(text);
        else issues.push(`${location}: untranslated JSX text ${JSON.stringify(text)}`);
      }
      if(node.type==='JSXElement' && node.openingElement.name.name==='T') {
        for(const child of node.children)if(child.expression?.type==='StringLiteral')add(child.expression.value);
      }
      if(node.type==='CallExpression' && ['tr','t'].includes(node.callee.name)) {
        if(node.arguments[0]?.type==='StringLiteral')add(node.arguments[0].value);
      }
      if(node.type==='JSXAttribute' && ['aria-label','placeholder','title','alt'].includes(node.name.name) && node.value?.type==='StringLiteral' && readable(node.value.value))issues.push(`${location}: untranslated ${node.name.name}`);
      if(node.type==='JSXAttribute' && ['label','help','empty','message','confirmLabel'].includes(node.name.name) && node.value?.type==='StringLiteral')add(node.value.value);
      if(node.type==='JSXElement' && node.openingElement.name.name==='option' && !node.openingElement.attributes.some(a=>a.name?.name==='value')) {
        const translated=node.children.some(c=>c.openingElement?.name?.name==='T'||c.expression?.callee?.name==='tr');
        if(translated)issues.push(`${location}: translated option must preserve its original value`);
      }
    });
  }
  for(const key of keys)for(const language of ['sq','fr','de'])if(!catalogs[language][key])issues.push(`Missing ${language}: ${key}`);
  return {keys:[...keys],issues};
}
if(process.argv[1]?.endsWith('check-i18n.mjs')) {
 const {keys,issues}=auditTranslations();
 if(issues.length){console.error(issues.join('\n'));process.exitCode=1;}
 else console.log(`Translation audit passed: ${keys.length} static UI keys covered in Albanian, French and German.`);
}
