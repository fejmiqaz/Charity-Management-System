import { T, useTranslate } from "../../context/LanguageContext";
import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { membersApi, backendUrl } from '../../api/api';
import { templateApi } from '../../api/activity';
import useResource from '../../hooks/useResource';
import { Role } from '../../components/Template';
import { ConfirmButton, ErrorAlert } from '../../components/Common';
import Pagination from '../../components/Pagination';
export default function Members() {
  const tr = useTranslate();
  const [params] = useSearchParams(),
    [f, setF] = useState({
      membershipYear: params.get('membershipYear') || new Date().getFullYear(),
      search: '',
      country: '',
      city: '',
      role: '',
      size: 20
    }),
    [query, setQuery] = useState(f),
    [page, setPage] = useState(0),
    [error, setError] = useState(null);
  const r = useResource(async () => {
    const {
      membershipYear,
      ...filter
    } = query;
    const qs = new URLSearchParams(Object.entries({
      ...filter,
      page
    }).filter(([, v]) => v !== ''));
    const [members, status] = await Promise.all([membersApi.list(qs.toString()), templateApi.membershipStatus(membershipYear)]);
    return {
      members,
      status
    };
  }, [query, page]);
  async function remove(id) {
    try {
      await membersApi.remove(id);
      r.reload();
    } catch (e) {
      setError(e);
    }
  }
  function clear() {
    const next = {
      ...f,
      search: '',
      country: '',
      city: '',
      role: ''
    };
    setF(next);
    setQuery(next);
    setPage(0);
  }
  const d = r.data?.members,
    s = r.data?.status;
  return <div className="container py-4"><div className="d-flex flex-wrap justify-content-between align-items-center mb-4"><div><h1 className="mb-1"><T>{"All Members"}</T></h1><p className="text-secondary mb-0"><T>{"Manage members and their personal information"}</T></p></div></div><div className="page-toolbar mb-4"><Link className="btn btn-dark" to="/dashboard"><i className="bi bi-house-door me-1" /><T>{"Home"}</T></Link><a className="btn btn-outline-primary" href={backendUrl(`/members/export.xlsx?${new URLSearchParams(query)}`)}><i className="bi bi-file-earmark-spreadsheet me-1" /><T>{"Members Report Excel"}</T></a><Role roles={['HEAD', 'SUBHEAD']}><Link className="btn btn-success" to="/members/new"><i className="bi bi-person-plus me-1" /><T>{"Add Member"}</T></Link></Role></div><ErrorAlert error={error || r.error} /><div className="membership-notice mb-4"><strong>{tr('Membership · {year}', {year: query.membershipYear})}</strong><span><T>{"Annual fee:"}</T> {s ? `€${s.fee}` : tr('Loading…')}</span><Role roles={['HEAD', 'SUBHEAD', 'TREASURER']}><Link to={`/memberships?year=${query.membershipYear}`}><T>{"Manage membership payments →"}</T></Link></Role></div><div className="card mb-4"><div className="card-body"><form className="row g-3 align-items-end" onSubmit={e => {
          e.preventDefault();
          setQuery({
            ...f
          });
          setPage(0);
        }}>{[['membershipYear', 'Membership year', 'col-md-2'], ['search', 'Search Member', 'col-md-4'], ['country', 'Country', 'col-md-2'], ['city', 'City', 'col-md-2']].map(([name, label, width]) => <div className={`col-12 ${width}`} key={name}><label className="form-label" htmlFor={name}><T>{label}</T></label><input className="form-control" id={name} type={name === 'membershipYear' ? 'number' : 'text'} min={name === 'membershipYear' ? 1900 : undefined} max={name === 'membershipYear' ? 2200 : undefined} required={name === 'membershipYear'} value={f[name]} placeholder={tr(name === 'search' ? 'Name, surname or email...' : undefined)} onChange={e => setF({
              ...f,
              [name]: e.target.value
            })} /></div>)}<div className="col-12 col-md-2"><label className="form-label" htmlFor="role"><T>{"Role"}</T></label><select id="role" className="form-select" value={f.role} onChange={e => setF({
              ...f,
              role: e.target.value
            })}><option value=""><T>{"All Roles"}</T></option>{['HEAD', 'SUBHEAD', 'TREASURER', 'PROJECT_MANAGER', 'EVENT_MANAGER', 'VOLUNTEER', 'MEMBER'].map(v => <option key={v} value={v}>{tr(v)}</option>)}</select></div><div className="col-md-2"><label className="form-label" htmlFor="size"><T>{"Per Page"}</T></label><select className="form-select" id="size" value={f.size} onChange={e => setF({
              ...f,
              size: e.target.value
            })}>{[10, 20, 50].map(n => <option key={n}>{n}</option>)}</select></div><div className="col-md-10 d-flex gap-2"><button className="btn btn-primary"><i className="bi bi-search me-1" /><T>{"Search"}</T></button><button className="btn btn-outline-secondary" type="button" onClick={clear}><i className="bi bi-arrow-counterclockwise me-1" /><T>{"Clear Filters"}</T></button></div></form></div></div>{r.loading ? <p role="status"><T>{"Loading members…"}</T></p> : d && !d.content.length ? <div className="empty-state"><i className="bi bi-people display-6" /><p className="mt-3"><T>{"No members matching your search were found."}</T></p><button className="btn btn-outline-dark btn-sm" onClick={clear}><T>{"Clear Filters"}</T></button></div> : d && <><div className="react-page-meta"><span><T>{"Total:"}</T> {d.totalElements}</span><span>{tr('Page {page} of {total}', {page: page + 1, total: d.totalPages || 1})}</span></div><div className="card p-0"><div className="table-responsive"><table className="table table-branded table-hover align-middle mb-0"><thead><tr>{['ID', 'First Name', 'Last Name', 'Email', 'Country', 'City', 'Role', tr('Membership · {year}', {year: query.membershipYear}), 'Actions'].map(t => <th key={t}><T>{t}</T></th>)}</tr></thead><tbody>{d.content.map(m => <tr key={m.id}><td>{m.id}</td><td className="fw-bold">{m.name}</td><td>{m.surname}</td><td>{m.email}</td><td>{m.country}</td><td>{m.city}</td><td><span className="badge text-bg-light border"><T>{m.role}</T></span></td><td><span className={`badge ${s.paidMemberships[m.id] ? 'badge-finished' : 'badge-pending'}`}>{s.paidMemberships[m.id] ? tr('Paid membership') : tr('Regular · unpaid')}</span>{s.paidMemberships[m.id] && <small className="d-block text-secondary mt-1">{s.paidMemberships[m.id].amount} {s.paidMemberships[m.id].currency}</small>}<Role roles={['HEAD', 'SUBHEAD', 'TREASURER']}><Link className="d-block small mt-1" to={`/memberships?year=${query.membershipYear}&memberId=${m.id}`}><T>{"Payment history"}</T></Link></Role></td><td><div className="d-flex flex-wrap gap-2"><Link className="btn btn-sm btn-outline-dark" to={`/members/${m.id}`}><i className="bi bi-info-circle me-1" /><T>{"Details"}</T></Link><Role roles={['HEAD']}><Link className="btn btn-sm btn-warning" to={`/members/${m.id}/edit`}><i className="bi bi-pencil me-1" /><T>{"Edit"}</T></Link><ConfirmButton confirmText={tr('Delete {name}?', {name: `${m.name} ${m.surname}`})} onConfirm={() => remove(m.id)} /></Role></div></td></tr>)}</tbody></table></div></div><Pagination data={d} onChange={setPage} /></>}</div>;
}
