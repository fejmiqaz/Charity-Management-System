import {useEffect, useState} from 'react';
import {useLanguage, useTranslate} from '../context/LanguageContext';
import {formatDate, formatNumber} from '../i18n/index.js';
const cache = new Map(), currencies = ['MKD', 'EUR', 'CHF'];
function load(base) {
  if (!cache.has(base)) cache.set(base, fetch(`https://api.frankfurter.dev/v2/rates?base=${base.toLowerCase()}&quotes=${currencies.filter(c => c !== base).join(',').toLowerCase()}`)
    .then(r => {if (!r.ok) throw Error(); return r.json();})
    .then(rows => {if (!Array.isArray(rows)) throw Error(); return rows;})
    .catch(e => {cache.delete(base); throw e;}));
  return cache.get(base);
}
export default function CurrencyPreview({amount, currency}) {
  const tr = useTranslate(), {language} = useLanguage();
  const [state, setState] = useState({base:null, items:null, error:false});
  const value = Number(amount), valid = amount !== '' && Number.isFinite(value) && value >= 0;
  useEffect(() => {
    if (!valid) return;
    let active = true;
    setState({base:currency, items:null, error:false});
    load(currency).then(items => {if (active) setState({base:currency, items, error:false});})
      .catch(() => {if (active) setState({base:currency, items:null, error:true});});
    return () => {active = false;};
  }, [currency, valid]);
  let text = '';
  if (valid) {
    if (state.base !== currency || !state.items && !state.error) text = tr('Loading conversion estimate…');
    else if (state.error) text = tr('Conversion estimate unavailable.');
    else {
      const amounts = currencies.filter(c => c !== currency).map(c => {
        const rate = state.items.find(r => r.base?.toUpperCase() === currency && r.quote?.toUpperCase() === c)?.rate;
        return rate == null ? tr('{currency} unavailable', {currency:c}) : `${formatNumber(language, value * rate, {minimumFractionDigits:2, maximumFractionDigits:2})} ${c}`;
      }).join(' · ');
      text = tr('Estimate: {amounts} · rates {date}', {amounts, date:state.items[0]?.date ? formatDate(language,state.items[0].date) : tr('latest available')});
    }
  }
  return <small className="form-text" aria-live="polite">{text}</small>;
}
