import { T, useTranslate } from "../context/LanguageContext";
import CurrencyPreview from "./CurrencyPreview";
import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { projectsApi, eventsApi, donationsApi, optionsApi } from '../api/api';
import useResource from '../hooks/useResource';
import { ErrorAlert } from './Common';
import { Back, Field, MemberSelect, ResourceState, Save } from './Template';
const configs = {
  projects: {
    name: 'Project',
    api: projectsApi,
    defaults: {
      name: '',
      description: '',
      status: 'PLANNED',
      projectType: 'STANDARD',
      projectPrice: '',
      memberIds: []
    },
    width: 760
  },
  events: {
    name: 'Event',
    api: eventsApi,
    defaults: {
      purpose: '',
      date: '',
      eventType: 'NORMAL',
      memberIds: []
    },
    width: 720
  },
  donations: {
    name: 'Donation',
    api: donationsApi,
    defaults: {
      donationAmount: '',
      currency: 'EUR',
      memberIds: []
    },
    width: 720
  }
};
export default function ActivityForm({
  type
}) {
  const tr = useTranslate();
  const {
      yearId,
      id
    } = useParams(),
    nav = useNavigate(),
    c = configs[type];
  const [form, setForm] = useState(c.defaults),
    [error, setError] = useState(null),
    [busy, setBusy] = useState(false);
  const resource = useResource(async () => {
    const [members, enums, record] = await Promise.all([optionsApi.members(), optionsApi.enums(), id ? c.api.get(yearId, id) : Promise.resolve(null)]);
    if (record) setForm({
      ...c.defaults,
      ...record,
      date: record.date?.slice(0, 16)
    });
    return {
      members,
      enums
    };
  }, [type, yearId, id]);
  async function submit(e) {
    e.preventDefault();
    if (busy) return;
    setBusy(true);
    setError(null);
    try {
      const body = {
        ...form
      };
      if (type === 'projects') body.projectPrice = Number(body.projectPrice);
      if (type === 'donations') body.donationAmount = Number(body.donationAmount);
      id ? await c.api.update(yearId, id, body) : await c.api.create(yearId, body);
      nav(`/years/${yearId}/${type}`);
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  const field = (name, label, props = {}) => <Field name={name} label={label} form={form} setForm={setForm} error={error} {...props} />;
  return <ResourceState {...resource}><div className="container py-4"><h1 className="mb-4">{tr(`${id ? 'Edit' : 'Add'} ${c.name}`)}</h1><div className="card p-4" style={{
        maxWidth: c.width
      }}><ErrorAlert error={error} /><form onSubmit={submit}>
 {type === 'projects' && <>{field('projectType', 'Project type', {
              help: 'Revenue projects can record income received for each month.',
              children: (resource.data?.enums.projectTypes || ['STANDARD', 'REVENUE']).map(t => <option key={t} value={t}>{t === 'REVENUE' ? tr('Revenue-generating project') : tr('Standard charity project')}</option>)
            })}{field('name', 'Name', {
              required: true
            })}{field('description', 'Description', {
              type: 'textarea',
              required: true
            })}{field('status', 'Status', {
              children: (resource.data?.enums.projectStatuses || []).map(s => <option key={s} value={s}>{tr(s)}</option>)
            })}</>}
 {type === 'events' && <>{field('eventType', 'Event type', {
              help: 'Task-based events can track work, assigned members, costs and payments.',
              children: (resource.data?.enums.eventTypes || ['NORMAL', 'TASK_BASED']).map(t => <option key={t} value={t}>{t === 'TASK_BASED' ? tr('Event with tasks') : tr('Normal event')}</option>)
            })}{field('purpose', 'Purpose', {
              required: true
            })}{field('date', 'Date and Time', {
              type: 'datetime-local',
              required: true
            })}</>}
 {type === 'donations' && <div className="mb-3"><label htmlFor="donationAmount" className="form-label"><T>{"Donation Amount"}</T></label><div className="input-group"><input className="form-control" id="donationAmount" type="number" min="0.01" step="0.01" required value={form.donationAmount} onChange={e => setForm({
                ...form,
                donationAmount: e.target.value
              })} /><select className="form-select" style={{
                maxWidth: 110
              }} aria-label={tr("Currency")} value={form.currency} onChange={e => setForm({
                ...form,
                currency: e.target.value
              })}>{['MKD', 'EUR', 'CHF'].map(c => <option key={c}>{c}</option>)}</select></div><CurrencyPreview amount={form.donationAmount} currency={form.currency} /></div>}
 <MemberSelect members={resource.data?.members || []} value={form.memberIds} onChange={memberIds => setForm({
            ...form,
            memberIds
          })} label={type === 'donations' ? 'Select Members' : 'Members'} />
 {type === 'projects' && <div className="mb-3"><label htmlFor="projectPrice" className="form-label"><T>{"Cost"}</T></label><div className="input-group"><input id="projectPrice" className="form-control" type="number" min="0" step="0.01" required value={form.projectPrice} onChange={e => setForm({
                ...form,
                projectPrice: e.target.value
              })} /><span className="input-group-text">€</span></div></div>}<Save busy={busy} /></form></div><div className="mt-3"><Back to={`/years/${yearId}/${type}`} /></div></div></ResourceState>;
}
