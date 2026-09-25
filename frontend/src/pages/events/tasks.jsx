import {LocalizedDate} from '../../components/Localized';
import { T, useTranslate } from "../../context/LanguageContext";
import CurrencyPreview from "../../components/CurrencyPreview";
import { useState } from 'react';
import { eventsApi, optionsApi } from '../../api/api';
import { activityApi } from '../../api/activity';
import useResource from '../../hooks/useResource';
import { ErrorAlert, ConfirmButton, useMoney } from '../../components/Common';
import { Role, Totals, MemberSelect, Field, roleOf } from '../../components/Template';
import { useAuth } from '../../context/AuthContext';
function PaymentForm({
  task,
  yearId,
  eventId,
  onSaved
}) {
  const tr = useTranslate();
  const today = new Date().toLocaleDateString('en-CA');
  const [f, setF] = useState({
      memberId: '',
      amount: '',
      currency: 'EUR',
      paidOn: today,
      note: ''
    }),
    [error, setError] = useState(null),
    [busy, setBusy] = useState(false);
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await activityApi.payment(yearId, eventId, task.id, {
        ...f,
        memberId: f.memberId ? Number(f.memberId) : null,
        amount: Number(f.amount)
      });
      setF({
        ...f,
        amount: '',
        note: ''
      });
      onSaved();
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  return <details className="mt-3"><summary><T>{"Record a payment"}</T></summary><ErrorAlert error={error} /><form className="payment-grid mt-3" onSubmit={submit}><select className="form-select" aria-label={tr("Payment member")} value={f.memberId} onChange={e => setF({
        ...f,
        memberId: e.target.value
      })}><option value=""><T>{"General task payment"}</T></option>{task.members.map(m => <option key={m.id} value={m.id}>{m.name} {m.surname}</option>)}</select><input className="form-control" aria-label={tr("Payment amount")} type="number" min="0.01" step="0.01" placeholder={tr("Amount")} required value={f.amount} onChange={e => setF({
        ...f,
        amount: e.target.value
      })} /><select className="form-select" aria-label={tr("Payment currency")} value={f.currency} onChange={e => setF({
        ...f,
        currency: e.target.value
      })}>{['MKD', 'EUR', 'CHF'].map(c => <option key={c}>{c}</option>)}</select><input className="form-control" aria-label={tr("Payment date")} type="date" max={today} required value={f.paidOn} onChange={e => setF({
        ...f,
        paidOn: e.target.value
      })} /><input className="form-control" aria-label={tr("Payment note")} placeholder={tr("Payment note")} maxLength="400" value={f.note} onChange={e => setF({
        ...f,
        note: e.target.value
      })} /><CurrencyPreview amount={f.amount} currency={f.currency} /><button className="btn btn-primary" disabled={busy}>{busy ? tr('Saving…') : tr('Save payment')}</button></form></details>;
}
export default function Tasks({
  yearId,
  id
}) {
  const money = useMoney();
  const tr = useTranslate();
  const {
      user
    } = useAuth(),
    canEdit = ['HEAD', 'SUBHEAD', 'EVENT_MANAGER'].includes(roleOf(user));
  const r = useResource(async () => ({
    tasks: await eventsApi.tasks(yearId, id),
    members: canEdit ? await optionsApi.members() : []
  }), [yearId, id, canEdit]);
  const [f, setF] = useState({
      title: '',
      description: '',
      price: '0',
      memberIds: []
    }),
    [error, setError] = useState(null),
    [busy, setBusy] = useState(false),
    [success, setSuccess] = useState('');
  async function action(fn, message) {
    setError(null);
    setBusy(true);
    try {
      await fn();
      setSuccess(message);
      r.reload();
      return true;
    } catch (e) {
      setError(e);
      return false;
    } finally {
      setBusy(false);
    }
  }
  async function submit(e) {
    e.preventDefault();
    if (await action(() => activityApi.createTask(yearId, id, {
      ...f,
      price: Number(f.price)
    }), 'Task added.')) setF({
      title: '',
      description: '',
      price: '0',
      memberIds: []
    });
  }
  return <section className="card p-4 mt-4"><div className="section-heading"><div><h2><T>{"Event tasks"}</T></h2><span><T>{"Assign work, track costs and record payments"}</T></span></div></div><ErrorAlert error={error || r.error} />{success && <div className="alert alert-success" role="status"><T>{success}</T></div>}{canEdit && <form className="task-entry-grid mb-4" onSubmit={submit}><Field name="title" label="Task title" form={f} setForm={setF} maxLength="160" required /><Field name="price" label="Planned cost" form={f} setForm={setF} type="number" min="0" step="0.01" required /><div className="task-wide"><Field name="description" label="Description" form={f} setForm={setF} maxLength="800" /></div><div className="task-wide"><MemberSelect label="Assigned members" id="taskMembers" members={r.data?.members || []} value={f.memberIds} onChange={memberIds => setF({
          ...f,
          memberIds
        })} /></div><div className="task-wide"><button className="btn btn-primary" disabled={busy}><i className="bi bi-plus-lg" /> <T>{"Add task"}</T></button></div></form>}{r.loading ? <p role="status"><T>{"Loading tasks…"}</T></p> : !r.data?.tasks.length ? <div className="empty-state"><T>{"No tasks have been added to this event."}</T></div> : r.data.tasks.map(task => <article className="task-card" key={task.id}><div className="task-card-head"><div><h3>{task.title}</h3><p>{task.description}</p></div><span className={`badge ${task.completed ? 'badge-finished' : 'badge-pending'}`}>{task.completed ? tr('Completed') : tr('Open')}</span></div><div className="task-meta"><strong>{tr('{amount} planned', {
            amount: money(task.price, 'EUR')
          })}</strong><span>{tr('{count} assigned', {
            count: task.members.length
          })}</span><span><Totals values={task.paymentTotals} /></span></div><div className="d-flex flex-wrap gap-2 mb-3">{task.members.map(m => <span className="badge text-bg-light border" key={m.id}>{m.name} {m.surname}</span>)}</div>{canEdit && <div className="task-actions"><button className="btn btn-sm btn-outline-primary" disabled={busy} onClick={() => action(() => activityApi.taskStatus(yearId, id, task.id, !task.completed), 'Task updated.')}>{task.completed ? tr('Reopen task') : tr('Mark completed')}</button><ConfirmButton confirmText={tr('Delete {name}? This permanently removes all payments recorded for this task and cancels pending assignment emails.', {
          name: task.title
        })} onConfirm={() => action(() => activityApi.deleteTask(yearId, id, task.id), 'Task deleted.')} /></div>}<Role roles={['HEAD', 'SUBHEAD', 'TREASURER', 'EVENT_MANAGER']}><PaymentForm task={task} yearId={yearId} eventId={id} onSaved={() => {
          setSuccess('Payment saved.');
          r.reload();
        }} /></Role>{task.payments.length > 0 && <div className="table-responsive mt-3"><table className="table table-sm"><thead><tr><th><T>{"Paid on"}</T></th><th><T>{"Member"}</T></th><th><T>{"Amount"}</T></th><th><T>{"Note"}</T></th></tr></thead><tbody>{task.payments.map(p => <tr key={p.id}><td><LocalizedDate value={p.paidOn} /></td><td>{p.memberId ? task.members.filter(m => m.id === p.memberId).map(m => `${m.name} ${m.surname}`).join('') || tr('Member #{id}', {
                  id: p.memberId
                }) : tr('General')}</td><td>{p.amount} {p.currency}</td><td>{p.note}</td></tr>)}</tbody></table></div>}</article>)}</section>;
}
