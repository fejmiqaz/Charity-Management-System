import { T } from "../../context/LanguageContext";
import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { membersApi, backendUrl } from '../../api/api';
import { templateApi } from '../../api/activity';
import useResource from '../../hooks/useResource';
import { Role } from '../../components/Template';
import { ConfirmButton, ErrorAlert } from '../../components/Common';
import Pagination from '../../components/Pagination';
export default function Members() {
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
  return <div className="container py-4"><div className="d-flex flex-wrap justify-content-between align-items-center mb-4"><div><h1 className="mb-1"><T>{"All Members"}</T></h1><p className="text-secondary mb-0">Manage members and their personal information</p></div></div><div className="page-toolbar mb-4"><Link className="btn btn-dark" to="/dashboard"><i className="bi bi-house-door me-1" />Home</Link><a className="btn btn-outline-primary" href={backendUrl(`/members/export.xlsx?${new URLSearchParams(query)}`)}><i className="bi bi-file-earmark-spreadsheet me-1" /><T>{"Members Report Excel"}</T></a><Role roles={['HEAD', 'SUBHEAD']}><Link className="btn btn-success" to="/members/new"><i className="bi bi-person-plus me-1" />Add Member</Link></Role></div><ErrorAlert error={error || r.error} /><div className="membership-notice mb-4"><strong>Membership · {query.membershipYear}</strong><span>Annual fee: {s ? `€${s.fee}` : 'Loading…'}</span><Role roles={['HEAD', 'SUBHEAD', 'TREASURER']}><Link to={`/memberships?year=${query.membershipYear}`}>Manage membership payments →</Link></Role></div><div className="card mb-4"><div className="card-body"><form className="row g-3 align-items-end" onSubmit={e => {
          e.preventDefault();
          setQuery({
            ...f
          });
          setPage(0);
        }}>{[['membershipYear', 'Membership year', 'col-md-2'], ['search', 'Search Member', 'col-md-4'], ['country', 'Country', 'col-md-2'], ['city', 'City', 'col-md-2']].map(([name, label, width]) => <div className={`col-12 ${width}`} key={name}><label className="form-label" htmlFor={name}>{label}</label><input className="form-control" id={name} type={name === 'membershipYear' ? 'number' : 'text'} min={name === 'membershipYear' ? 1900 : undefined} max={name === 'membershipYear' ? 2200 : undefined} required={name === 'membershipYear'} value={f[name]} placeholder={name === 'search' ? 'Name, surname or email...' : undefined} onChange={e => setF({
              ...f,
              [name]: e.target.value
            })} /></div>)}<div className="col-12 col-md-2"><label className="form-label" htmlFor="role"><T>{"Role"}</T></label><select id="role" className="form-select" value={f.role} onChange={e => setF({
              ...f,
              role: e.target.value
            })}><option value=""><T>{"All Roles"}</T></option>{['HEAD', 'SUBHEAD', 'TREASURER', 'PROJECT_MANAGER', 'EVENT_MANAGER', 'VOLUNTEER', 'MEMBER'].map(v => <option key={v}>{v}</option>)}</select></div><div className="col-md-2"><label className="form-label" htmlFor="size">Per Page</label><select className="form-select" id="size" value={f.size} onChange={e => setF({
              ...f,
              size: e.target.value
            })}>{[10, 20, 50].map(n => <option key={n}>{n}</option>)}</select></div><div className="col-md-10 d-flex gap-2"><button className="btn btn-primary"><i className="bi bi-search me-1" /><T>{"Search"}</T></button><button className="btn btn-outline-secondary" type="button" onClick={clear}><i className="bi bi-arrow-counterclockwise me-1" />Clear Filters</button></div></form></div></div>{r.loading ? <p role="status">Loading members…</p> : d && !d.content.length ? <div className="empty-state"><i className="bi bi-people display-6" /><p className="mt-3">No members matching your search were found.</p><button className="btn btn-outline-dark btn-sm" onClick={clear}>Clear Filters</button></div> : d && <><div className="react-page-meta"><span>Total: {d.totalElements}</span><span>Page {page + 1} of {d.totalPages || 1}</span></div><div className="card p-0"><div className="table-responsive"><table className="table table-branded table-hover align-middle mb-0"><thead><tr>{['ID', 'First Name', 'Last Name', 'Email', 'Country', 'City', 'Role', `Membership · ${query.membershipYear}`, 'Actions'].map(t => <th key={t}>{t}</th>)}</tr></thead><tbody>{d.content.map(m => <tr key={m.id}><td>{m.id}</td><td className="fw-bold">{m.name}</td><td>{m.surname}</td><td>{m.email}</td><td>{m.country}</td><td>{m.city}</td><td><span className="badge text-bg-light border">{m.role}</span></td><td><span className={`badge ${s.paidMemberships[m.id] ? 'badge-finished' : 'badge-pending'}`}>{s.paidMemberships[m.id] ? 'Paid membership' : 'Regular · unpaid'}</span>{s.paidMemberships[m.id] && <small className="d-block text-secondary mt-1">{s.paidMemberships[m.id].amount} {s.paidMemberships[m.id].currency}</small>}<Role roles={['HEAD', 'SUBHEAD', 'TREASURER']}><Link className="d-block small mt-1" to={`/memberships?year=${query.membershipYear}&memberId=${m.id}`}><T>{"Payment history"}</T></Link></Role></td><td><div className="d-flex flex-wrap gap-2"><Link className="btn btn-sm btn-outline-dark" to={`/members/${m.id}`}><i className="bi bi-info-circle me-1" /><T>{"Details"}</T></Link><Role roles={['HEAD']}><Link className="btn btn-sm btn-warning" to={`/members/${m.id}/edit`}><i className="bi bi-pencil me-1" /><T>{"Edit"}</T></Link><ConfirmButton confirmText={`Delete ${m.name} ${m.surname}?`} onConfirm={() => remove(m.id)} /></Role></div></td></tr>)}</tbody></table></div></div><Pagination data={d} onChange={setPage} /></>}</div>;
}
