import { T } from "../../context/LanguageContext";
import CurrencyPreview from "../../components/CurrencyPreview";
import { useState } from 'react';
import { projectsApi } from '../../api/api';
import { activityApi } from '../../api/activity';
import useResource from '../../hooks/useResource';
import { ErrorAlert } from '../../components/Common';
import { Role, Totals, Field } from '../../components/Template';
export default function Revenues({
  yearId,
  id
}) {
  const r = useResource(() => projectsApi.revenues(yearId, id), [yearId, id]);
  const [form, setForm] = useState({
      month: new Date().toISOString().slice(0, 7),
      customer: '',
      amount: '',
      currency: 'EUR',
      note: ''
    }),
    [error, setError] = useState(null),
    [busy, setBusy] = useState(false),
    [success, setSuccess] = useState('');
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await activityApi.revenue(yearId, id, {
        ...form,
        amount: Number(form.amount)
      });
      setForm(f => ({
        ...f,
        customer: '',
        amount: '',
        note: ''
      }));
      setSuccess('Income recorded.');
      r.reload();
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  const totals = (r.data || []).reduce((a, x) => ({
    ...a,
    [x.currency]: (a[x.currency] || 0) + Number(x.amount)
  }), {});
  return <section className="card p-4 mt-4"><div className="section-heading"><div><h2><T>{"Monthly project income"}</T></h2><span>Advertising, services and other recurring revenue</span></div><strong className="revenue-total"><Totals values={totals} /></strong></div><ErrorAlert error={error || r.error} />{success && <div className="alert alert-success" role="status">{success}</div>}<Role roles={['HEAD', 'SUBHEAD', 'TREASURER', 'PROJECT_MANAGER']}><form className="revenue-entry-grid mb-4" onSubmit={submit}>{[['month', 'Month', 'month'], ['customer', 'Customer or advertiser', 'text'], ['amount', 'Income', 'number'], ['currency', 'Currency', 'select'], ['note', 'Note', 'text']].map(([name, label, type]) => <Field key={name} name={name} label={label} type={type === 'select' ? 'text' : type} form={form} setForm={setForm} required={name !== 'note'} min={type === 'number' ? '0.01' : undefined} step={type === 'number' ? '0.01' : undefined} maxLength={name === 'customer' ? 160 : name === 'note' ? 400 : undefined}>{type === 'select' ? ['MKD', 'EUR', 'CHF'].map(c => <option key={c}>{c}</option>) : null}</Field>)}<CurrencyPreview amount={form.amount} currency={form.currency} /><button className="btn btn-primary" disabled={busy}><i className="bi bi-plus-lg" /> {busy ? 'Saving…' : 'Record income'}</button></form></Role>{r.loading ? <p role="status">Loading income…</p> : !r.data?.length ? <div className="empty-state">No monthly income has been recorded yet.</div> : <div className="table-responsive"><table className="table table-branded"><thead><tr><th>Month</th><th>Customer</th><th><T>{"Amount"}</T></th><th>Note</th></tr></thead><tbody>{r.data.map(x => <tr key={x.id}><td>{x.revenueMonth}</td><td>{x.customer}</td><td className="fw-bold text-success">{x.amount} {x.currency}</td><td>{x.note}</td></tr>)}</tbody></table></div>}</section>;
}
