import { T, useTranslate } from "../../context/LanguageContext";
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { yearsApi } from '../../api/api';
import { useWorkspace } from '../../context/WorkspaceContext';
import useResource from '../../hooks/useResource';
import { Role } from '../../components/Template';
import { ConfirmButton, ErrorAlert } from '../../components/Common';
import Pagination from '../../components/Pagination';
export default function Years() {
  const tr = useTranslate();
  const [form, setForm] = useState({
      yearValue: '',
      sortDir: 'desc',
      size: 10
    }),
    [query, setQuery] = useState(form),
    [page, setPage] = useState(0),
    [error, setError] = useState(null),
    {
      setYearId,
      refreshYears
    } = useWorkspace();
  const r = useResource(() => yearsApi.list(new URLSearchParams(Object.entries({
    ...query,
    page
  }).filter(([, v]) => v !== '')).toString()), [query, page]);
  function clear() {
    const next = {
      yearValue: '',
      sortDir: 'desc',
      size: 10
    };
    setForm(next);
    setQuery(next);
    setPage(0);
  }
  async function remove(id) {
    try {
      await yearsApi.remove(id);
      setYearId('');
      refreshYears();
      r.reload();
    } catch (e) {
      setError(e);
    }
  }
  const d = r.data;
  return <div className="container py-4"><div className="d-flex flex-wrap justify-content-between align-items-center mb-4"><div><h1 className="mb-1"><T>{"All Years"}</T></h1><p className="text-secondary mb-0"><T>{"Search, filter, and manage the organization's years"}</T></p></div></div><div className="page-toolbar mb-4"><Link className="btn btn-dark" to="/dashboard"><i className="bi bi-house-door me-1" /><T>{"Home"}</T></Link><Role roles={['HEAD']}><Link className="btn btn-success" to="/years/new"><i className="bi bi-plus-lg me-1" /><T>{"Add Year"}</T></Link></Role></div><ErrorAlert error={error || r.error} /><div className="card p-4 mb-4"><h2 className="section-title"><T>{"Search & Filtering"}</T></h2><form className="row g-3 align-items-end" onSubmit={e => {
        e.preventDefault();
        setQuery({
          ...form
        });
        setPage(0);
      }}><div className="col-md-4"><label htmlFor="yearValue" className="form-label"><T>{"Search by Year"}</T></label><input id="yearValue" type="number" min="2000" max="2100" className="form-control" placeholder={tr("Example: 2026")} value={form.yearValue} onChange={e => setForm({
            ...form,
            yearValue: e.target.value
          })} /></div><div className="col-md-3"><label className="form-label" htmlFor="sortDir"><T>{"Sort Order"}</T></label><select className="form-select" id="sortDir" value={form.sortDir} onChange={e => setForm({
            ...form,
            sortDir: e.target.value
          })}><option value="desc"><T>{"Newest First"}</T></option><option value="asc"><T>{"Oldest First"}</T></option></select></div><div className="col-md-2"><label className="form-label" htmlFor="size"><T>{"Per Page"}</T></label><select className="form-select" id="size" value={form.size} onChange={e => setForm({
            ...form,
            size: e.target.value
          })}>{[5, 10, 20, 50].map(n => <option key={n}>{n}</option>)}</select></div><div className="col-md-3 d-flex flex-wrap gap-2"><button className="btn btn-primary"><i className="bi bi-search me-1" /><T>{"Search"}</T></button><button type="button" className="btn btn-secondary" onClick={clear}><i className="bi bi-arrow-counterclockwise me-1" /><T>{"Clear"}</T></button></div></form></div>{r.loading ? <p role="status"><T>{"Loading years…"}</T></p> : d && (!d.content.length ? <div className="empty-state"><i className="bi bi-calendar-x display-6" /><p className="mt-3 mb-2"><T>{"No years matching your search were found."}</T></p><button className="btn btn-secondary btn-sm" onClick={clear}><T>{"Clear Filters"}</T></button></div> : <><div className="d-flex justify-content-between align-items-center mb-3"><span className="text-secondary"><T>{"Total:"}</T> <strong>{d.totalElements}</strong></span><span className="text-secondary">{tr('Page {page} of {total}', {page: page + 1, total: d.totalPages})}</span></div><div className="year-grid">{d.content.map(y => <article className="year-card" key={y.id}><div className="year-card-heading"><div><p className="eyebrow"><T>{"YEARLY WORKSPACE"}</T></p><h2>{y.yearValue}</h2></div><span className="metric-icon mint"><i className="bi bi-calendar3" /></span></div><p className="text-secondary"><T>{"Everything your organization is working on this year."}</T></p><nav className="year-shortcuts" aria-label={tr("Year sections")}>{[['projects', 'bi-kanban', 'Projects'], ['donations', 'bi-heart', 'Donations'], ['events', 'bi-calendar-event', 'Events'], ['budget', 'bi-wallet2', 'Budget']].map(([path, icon, label]) => <Link to={`/years/${y.id}/${path}`} key={path}><i className={`bi ${icon}`} /><T>{label}</T><i className="bi bi-arrow-up-right" /></Link>)}</nav><div className="year-card-footer"><Link className="btn btn-sm btn-outline-dark" to={`/years/${y.id}`}><T>{"Year summary"}</T></Link><Role roles={['HEAD']}><div className="d-flex gap-2"><Link className="btn btn-sm btn-warning" to={`/years/${y.id}/edit`}><T>{"Edit"}</T></Link><ConfirmButton confirmText={tr('Delete year {year}?', {year: y.yearValue})} onConfirm={() => remove(y.id)} /></div></Role></div></article>)}</div><Pagination data={d} onChange={setPage} /></>)}</div>;
}
