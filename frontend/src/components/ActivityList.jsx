import { T } from "../context/LanguageContext";
import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { projectsApi, eventsApi, donationsApi, backendUrl, optionsApi, api } from '../api/api';
import { useWorkspace } from '../context/WorkspaceContext';
import useResource from '../hooks/useResource';
import { Role, Back, Totals } from './Template';
import { ErrorAlert, ConfirmButton, pretty } from './Common';
const config = {
  projects: {
    api: projectsApi,
    label: 'Projects',
    singular: 'Project',
    icon: 'bi-kanban',
    description: "Manage this year's projects and expenses",
    writers: ['HEAD', 'SUBHEAD', 'PROJECT_MANAGER'],
    reporters: ['HEAD', 'SUBHEAD', 'TREASURER', 'PROJECT_MANAGER']
  },
  events: {
    api: eventsApi,
    label: 'Events',
    singular: 'Event',
    icon: 'bi-calendar-event',
    description: "Manage this year's events and participants",
    writers: ['HEAD', 'SUBHEAD', 'TREASURER'],
    reporters: ['HEAD', 'SUBHEAD', 'TREASURER']
  },
  donations: {
    api: donationsApi,
    label: 'Donations',
    singular: 'Donation',
    icon: 'bi-heart',
    description: "Manage this year's donations and donating members",
    writers: ['HEAD', 'SUBHEAD'],
    reporters: ['HEAD', 'SUBHEAD']
  }
};
export default function ActivityList({
  type
}) {
  const {
      yearId
    } = useParams(),
    {
      years,
      setYearId
    } = useWorkspace(),
    c = config[type],
    year = years.find(y => String(y.id) === yearId),
    [filter, setFilter] = useState({
      type: '',
      status: ''
    }),
    [applied, setApplied] = useState({
      type: '',
      status: ''
    }),
    [error, setError] = useState(null);
  useEffect(() => {
    setYearId(yearId);
  }, [yearId, setYearId]);
  const query = new URLSearchParams(Object.entries(applied).filter(([, v]) => v)).toString();
  const r = useResource(async () => ({
    items: await c.api.list(yearId, query),
    enums: await optionsApi.enums(),
    summary: type === 'projects' ? await api('/years/' + yearId + '/projects/summary') : null
  }), [yearId, type, query]);
  const all = r.data?.items || [],
    items = all;
  const filtersActive = !!(applied.type || applied.status),
    base = `/years/${yearId}/${type}`;
  async function remove(id) {
    try {
      await c.api.remove(yearId, id);
      r.reload();
    } catch (e) {
      setError(e);
    }
  }
  const totalCost = Number(r.data?.summary?.totalProjectCost || 0),
    budget = Number(r.data?.summary?.budgetAmount || 0),
    currencyTotals = all.reduce((a, d) => ({
      ...a,
      [d.currency]: (a[d.currency] || 0) + Number(d.donationAmount || 0)
    }), {});
  return <div className="container py-4"><div className="d-flex flex-wrap justify-content-between align-items-center mb-4"><div><h1 className="mb-1">{c.label} for Year {year?.yearValue}</h1><p className="text-secondary mb-0">{c.description}</p></div></div><div className="page-toolbar mb-4"><Back to="/years"><T>{"Back to Years"}</T></Back><Link className="btn btn-outline-dark" to={`/years/${yearId}`}><i className="bi bi-info-circle me-1" /><T>{"Year Details"}</T></Link><Role roles={c.reporters}><a className="btn btn-danger" href={backendUrl(`/pdf/years/${yearId}/${type}`)}><i className="bi bi-file-earmark-pdf me-1" />{c.label} Report PDF</a></Role><a className="btn btn-outline-primary" href={backendUrl(`${base}/export.xlsx${query ? '?' + query : ''}`)}><i className="bi bi-file-earmark-spreadsheet me-1" />{c.label} Report Excel</a><Role roles={c.writers}><Link className="btn btn-success" to={`${base}/add`}><i className="bi bi-plus-lg me-1" />Add {c.singular}</Link></Role></div><ErrorAlert error={error || r.error} />{type !== 'donations' && <form className="card p-3 mb-4" onSubmit={e => {
      e.preventDefault();
      setApplied({
        ...filter
      });
    }}><div className="row g-3 align-items-end"><div className="col-sm-4"><label htmlFor="filterType" className="form-label">Type</label><select className="form-select" id="filterType" value={filter.type} onChange={e => setFilter({
            ...filter,
            type: e.target.value
          })}><option value="">All types</option>{(r.data?.enums[type === 'projects' ? 'projectTypes' : 'eventTypes'] || []).map(t => <option key={t} value={t}>{t === 'TASK_BASED' ? 'With tasks' : pretty(t)}</option>)}</select></div><div className="col-sm-4"><label htmlFor="filterStatus" className="form-label"><T>{"Status"}</T></label><select className="form-select" id="filterStatus" value={filter.status} onChange={e => setFilter({
            ...filter,
            status: e.target.value
          })}><option value="">All statuses</option>{(r.data?.enums[type === 'projects' ? 'projectStatuses' : 'eventStatuses'] || []).map(s => <option key={s} value={s}>{pretty(s)}</option>)}</select></div><div className="col-sm-4 d-flex gap-2"><button className="btn btn-primary">Apply filters</button><button type="button" className="btn btn-outline-dark" onClick={() => {
            setFilter({
              type: '',
              status: ''
            });
            setApplied({
              type: '',
              status: ''
            });
          }}>Clear filters</button></div></div></form>}
 {r.loading ? <p role="status">Loading {type}…</p> : !r.error && (!items.length ? <div className="empty-state"><i className={`bi ${c.icon} display-6`} /><p className="mt-3 mb-2">{filtersActive ? `No ${type} match the selected filters.` : `No ${type} were found for this year.`}</p>{!filtersActive && <Role roles={c.writers}><Link className="btn btn-success btn-sm" to={`${base}/add`}>Create the First {c.singular}</Link></Role>}</div> : <div className="card p-0 mb-4"><div className="table-responsive"><table className="table table-branded table-hover align-middle mb-0"><thead><tr>{(type === 'projects' ? ['Name', 'Type', 'Status', 'Created Date', 'Cost', 'Members', 'Actions'] : type === 'events' ? ['ID', 'Purpose', 'Type', 'Date', 'Status', 'Members', 'Actions'] : ['ID', 'Amount', 'Members', 'Actions']).map(s => <th key={s}>{s}</th>)}</tr></thead><tbody>{items.map(x => <tr key={x.id}>{type === 'projects' ? <><td className="fw-bold">{x.name}</td><td><span className="badge text-bg-light border">{x.projectType === 'REVENUE' ? 'Revenue' : 'Standard'}</span></td><td><span className={`badge rounded-pill badge-${['FINISHED', 'CANCELLED', 'ONGOING'].includes(x.status) ? x.status.toLowerCase() : 'pending'}`}>{x.status}</span></td><td>{x.dateCreated}</td><td className="fw-bold">{x.projectPrice} €</td></> : type === 'events' ? <><td>{x.id}</td><td className="fw-bold">{x.purpose}</td><td><span className="badge text-bg-light border">{x.eventType === 'TASK_BASED' ? 'With tasks' : 'Normal'}</span></td><td>{x.date?.replace('T', ' ')}</td><td><span className="badge text-bg-light border">{!x.date ? 'Unscheduled' : new Date(x.date) > new Date() ? 'Upcoming' : 'Past'}</span></td></> : <><td>{x.id}</td><td className="fw-bold">{x.donationAmount} {x.currency}</td></>}<td>{x.memberNames?.length ? <div className="d-flex flex-wrap gap-1">{x.memberNames.map((n, i) => <span className="badge text-bg-light border" key={i}>{n}</span>)}</div> : <span className="text-secondary">No Members</span>}</td><td><div className="d-flex flex-wrap gap-2"><Link className="btn btn-sm btn-outline-dark" to={`${base}/${x.id}`}><i className="bi bi-info-circle me-1" /><T>{"Details"}</T></Link><Role roles={c.writers}><Link className="btn btn-sm btn-warning" to={`${base}/${x.id}/edit`}><i className="bi bi-pencil me-1" /><T>{"Edit"}</T></Link><ConfirmButton confirmText={`Delete ${x.name || x.purpose || 'Donation #' + x.id}?`} onConfirm={() => remove(x.id)} /></Role></div></td></tr>)}</tbody></table></div></div>)}
 {!r.loading && !r.error && type === 'projects' && <div className="card card-accent-top budget p-4" style={{
      maxWidth: 560,
      margin: '30px auto 0'
    }}><h2 className="section-title" style={{
        borderBottomColor: 'var(--ink-900)'
      }}>Financial Summary</h2><div className="summary-row"><span>Total Project Cost (all projects this year)</span><strong>{totalCost} €</strong></div><div className="summary-row"><span>Year Budget</span><strong>{budget} €</strong></div><div className="summary-row total"><span><T>{"Status"}</T></span><strong className={totalCost <= budget ? 'status-ok' : 'status-bad'}>{totalCost <= budget ? 'Within Budget' : 'Over Budget'}</strong></div></div>}
 {!r.loading && !r.error && type === 'donations' && <div className="card card-accent-top emerald p-4" style={{
      maxWidth: 520,
      margin: '30px auto 0'
    }}><h2 className="section-title">Donation Summary</h2><div className="summary-row total"><span><T>{"Total Donation Amount"}</T></span><strong><Totals values={currencyTotals} /></strong></div></div>}
 </div>;
}
