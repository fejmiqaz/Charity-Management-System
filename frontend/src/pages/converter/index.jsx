import {formatDate} from '../../i18n/index.js';
import { T, useTranslate, useLanguage } from "../../context/LanguageContext";
import { useEffect, useState } from 'react';
export const currencies = ['MKD', 'EUR', 'CHF'];
export function useRates(from) {
  const [rates, setRates] = useState(null),
    [error, setError] = useState('');
  useEffect(() => {
    const c = new AbortController();
    setRates(null);
    setError('');
    fetch(`https://api.frankfurter.dev/v2/rates?base=${encodeURIComponent(from)}&quotes=${currencies.filter(x => x !== from).join(',').toLowerCase()}`, {
      signal: c.signal
    }).then(r => {
      if (!r.ok) throw Error();
      return r.json();
    }).then(data => {
      if (!Array.isArray(data)) throw Error();
      setRates(data);
    }).catch(e => {
      if (e.name !== 'AbortError') setError('Exchange rates are unavailable right now. Try again later.');
    });
    return () => c.abort();
  }, [from]);
  return {
    rates,
    error
  };
}
export default function Converter() {
  const tr = useTranslate();
  const {locale, language} = useLanguage();
  const [amount, setAmount] = useState('100'),
    [from, setFrom] = useState('EUR'),
    {
      rates,
      error
    } = useRates(from);
  return <div className="container py-4"><h1><T>{"Currency converter"}</T></h1><p className="text-secondary"><T>{"Estimates use the latest available reference rates. Recorded donations and payments retain their original currency."}</T></p><div className="card p-4" style={{
      maxWidth: 650
    }}><div className="row g-3 align-items-end"><div className="col-sm-6"><label htmlFor="convertAmount" className="form-label"><T>{"Amount"}</T></label><input id="convertAmount" className="form-control" type="number" min="0" step="0.01" required value={amount} onChange={e => setAmount(e.target.value)} /></div><div className="col-sm-6"><label htmlFor="convertFrom" className="form-label"><T>{"From"}</T></label><select id="convertFrom" className="form-select" value={from} onChange={e => setFrom(e.target.value)}>{currencies.map(c => <option key={c}>{c}</option>)}</select></div></div><div id="conversionResults" className="mt-4" aria-live="polite">{tr(error) || (!rates ? tr('Loading rates…') : amount === '' || Number(amount) < 0 ? tr('Enter a valid amount.') : currencies.map(quote => {
          const rate = quote === from ? 1 : rates.find(r => r.base?.toUpperCase() === from && r.quote?.toUpperCase() === quote)?.rate;
          return <div className="summary-row" key={quote}><span>{quote}</span><strong>{rate == null ? tr('Rate unavailable') : `${(Number(amount) * rate).toLocaleString(locale, {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
              })} ${quote}`}</strong></div>;
        }))}</div><small id="conversionSource" className="text-secondary">{rates && tr('Source: Frankfurter reference rates · {date}. Estimates only.', {date: rates[0]?.date ? formatDate(language, rates[0].date) : tr('latest available')})}</small></div></div>;
}
