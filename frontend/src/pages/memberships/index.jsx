import {LocalizedDate} from '../../components/Localized';
import { T, useTranslate } from "../../context/LanguageContext";
import CurrencyPreview from "../../components/CurrencyPreview";
import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { api, membershipsApi, optionsApi, backendUrl } from '../../api/api';
import useResource from '../../hooks/useResource';
import { ErrorAlert } from '../../components/Common';
import { Field, Totals } from '../../components/Template';
function Correction({
  receipt,
  onSaved
}) {
  const tr = useTranslate();
  const [reason, setReason] = useState(''),
    [busy, setBusy] = useState(false),
    [error, setError] = useState(null);
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    try {
      await membershipsApi.void(receipt.id, reason);
      onSaved();
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  return receipt.paid ? <details><summary><T>{"Correct receipt"}</T></summary><ErrorAlert error={error} /><form className="mt-2" onSubmit={submit}><label className="form-label" htmlFor={`reason-${receipt.id}`}><T>{"Reason for voiding"}</T></label><input className="form-control mb-2" id={`reason-${receipt.id}`} maxLength="500" required value={reason} onChange={e => setReason(e.target.value)} /><button className="btn btn-sm btn-danger" disabled={busy}>{busy ? tr('Saving…') : tr('Void receipt')}</button></form></details> : receipt.voidReason;
}
export default function Memberships() {
  const tr = useTranslate();
  const [params, setParams] = useSearchParams(),
    year = Number(params.get('year') || new Date().getFullYear()),
    memberId = params.get('memberId'),
    [inputYear, setInputYear] = useState(year),
    today = new Date().toLocaleDateString('en-CA');
  const [pay, setPay] = useState({
      memberId: memberId || '',
      amount: '',
      currency: 'EUR',
      paidOn: today
    }),
    [fee, setFee] = useState({
      amount: ''
    }),
    [error, setError] = useState(null),
    [busy, setBusy] = useState(false),
    [success, setSuccess] = useState('');
  const r = useResource(async () => {
    const d = await api(`/memberships/overview?${new URLSearchParams({
      year,
      ...(memberId ? {
        memberId
      } : {})
    })}`);
    const members = await optionsApi.members();
    setFee({
      amount: d.fee
    });
    setPay(p => ({
      ...p,
      memberId: memberId || '',
      amount: d.fee
    }));
    return {
      ...d,
      members
    };
  }, [year, memberId]);
  async function action(e, fn, message) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await fn();
      setSuccess(message);
      r.reload();
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  const d = r.data;
  return <div className="container py-4"><p className="eyebrow"><T>{"ANNUAL MEMBERSHIP"}</T></p><h1><T>{"Support that keeps us going."}</T></h1><p className="text-secondary"><T>{"Record dues received from members. Membership covers the selected calendar year."}</T></p><ErrorAlert error={error || r.error} />{success && <div className="alert alert-success" role="status"><T>{success}</T></div>}<form className="d-flex flex-wrap gap-2 align-items-end mb-4" onSubmit={e => {
      e.preventDefault();
      setParams({
        year: inputYear
      });
    }}><div><label className="form-label" htmlFor="year"><T>{"Membership year"}</T></label><input className="form-control" id="year" type="number" min="1900" max="2200" required value={inputYear} onChange={e => setInputYear(e.target.value)} /></div><button className="btn btn-primary"><T>{"View year"}</T></button><Link className="btn btn-outline-secondary" to={`/members?membershipYear=${year}`}><T>{"Member statuses"}</T></Link><a className="btn btn-danger" href={backendUrl(`/memberships/export.pdf?year=${year}`)}><i className="bi bi-file-earmark-pdf me-1" /><T>{"Memberships Report PDF"}</T></a><a className="btn btn-outline-primary" href={backendUrl(`/memberships/export.xlsx?year=${year}`)}><i className="bi bi-file-earmark-spreadsheet me-1" /><T>{"Memberships Report Excel"}</T></a></form>{r.loading ? <p role="status"><T>{"Loading memberships…"}</T></p> : d && <><div className="membership-summary mb-4"><div className="card p-4"><span className="stat-label"><T>{"Annual fee"}</T></span><strong className="stat-value">€{d.fee}</strong><small><T>{"Default for new receipts this year"}</T></small></div><div className="card p-4"><span className="stat-label">{tr('Membership income · {year}', {year})}</span><strong className="stat-value"><Totals values={d.totals} /></strong><small><T>{"Voided receipts excluded"}</T></small></div></div><div className="row g-4 mb-4"><section className="col-lg-8"><div className="card p-4 h-100"><h2 className="section-title"><T>{"Record a received payment"}</T></h2><p className="small text-secondary"><T>{"This records an offline payment already received; it does not charge the member. Do not record it again as a donation."}</T></p><form className="row g-3" onSubmit={e => action(e, () => membershipsApi.record({
              ...pay,
              memberId: Number(pay.memberId),
              year,
              amount: Number(pay.amount)
            }), 'Payment recorded.')}><div className="col-md-7"><Field name="memberId" label="Member" form={pay} setForm={setPay} required><option value=""><T>{"Select a member"}</T></option>{d.members.map(m => <option key={m.id} value={m.id}>{m.name} {m.surname}</option>)}</Field></div><div className="col-md-5"><Field name="paidOn" label="Payment received on" type="date" max={today} form={pay} setForm={setPay} required /></div><div className="col-md-7"><Field name="amount" label="Received amount" type="number" min="0.01" step="0.01" form={pay} setForm={setPay} required /></div><div className="col-md-5"><Field name="currency" label="Currency" form={pay} setForm={setPay}>{['MKD', 'EUR', 'CHF'].map(c => <option key={c}>{c}</option>)}</Field></div><CurrencyPreview amount={pay.amount} currency={pay.currency} /><div><button className="btn btn-primary" disabled={busy}>{tr('Record payment for {name}', {name: year})}</button></div></form></div></section><section className="col-lg-4"><div className="card p-4 h-100"><h2 className="section-title"><T>{"Set this year's fee"}</T></h2><form onSubmit={e => action(e, () => membershipsApi.fee(year, Number(fee.amount)), 'Fee saved.')}><Field name="feeAmount" label="Fee (EUR)" form={{
                feeAmount: fee.amount
              }} setForm={update => setFee({
                amount: update({
                  feeAmount: fee.amount
                }).feeAmount
              })} type="number" min="0.01" max="9999999999.99" step="0.01" required /><p className="small text-secondary"><T>{"Existing payments stay paid at their recorded amount. Other years keep their own fee."}</T></p><button className="btn btn-outline-primary" disabled={busy}><T>{"Save fee"}</T></button></form></div></section></div><div className="section-heading"><h2>{memberId ? tr('Member payment history · all years') : tr('Payment ledger')}</h2>{memberId && <Link to={`/memberships?year=${year}`}><T>{"View everyone"}</T></Link>}</div>{!d.payments.length ? <div className="empty-state"><T>{"No payments recorded. Previous years are not automatically treated as debts."}</T></div> : <div className="card table-responsive"><table className="table table-branded mb-0"><thead><tr>{['Member', 'Year', 'Amount', 'Received', 'Status', 'Recorded by', 'Correction'].map(t => <th key={t}><T>{t}</T></th>)}</tr></thead><tbody>{d.payments.map(p => <tr key={p.id}><td>{p.memberName}</td><td>{p.membershipYear}</td><td>{p.amount} {p.currency}</td><td><LocalizedDate value={p.paidOn} /></td><td><span className={`badge ${p.paid ? 'badge-finished' : 'badge-cancelled'}`}>{p.paid ? tr('Paid') : tr('Voided')}</span></td><td>{p.recordedBy}</td><td><Correction receipt={p} onSaved={() => {
                  setSuccess('Receipt voided.');
                  r.reload();
                }} /></td></tr>)}</tbody></table></div>}</>}</div>;
}
