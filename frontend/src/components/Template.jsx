import AccessDenied from "../pages/error/access-denied";
import GeneralError from "../pages/error/general-error";
import { T } from "../context/LanguageContext";
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { money } from './Common';
export const roleOf = user => String(user?.role || '').replace(/^ROLE_/, '');
export function Role({
  roles,
  children
}) {
  const {
    user
  } = useAuth();
  return roles.includes(roleOf(user)) ? children : null;
}
export function ResourceState({
  loading,
  error,
  children
}) {
  if (loading) return <div className="container py-4" role="status">Loading…</div>;
  if(error?.status===403)return <AccessDenied/>;
  if(error)return <GeneralError error={error}/>;
  return children;
}
export function Back({
  to,
  children = 'Back'
}) {
  return <Link className="btn btn-dark" to={to}><i className="bi bi-arrow-left me-1" /><T>{children}</T></Link>;
}
export function Rows({
  items
}) {
  return items.map(([label, value]) => <div className="kv-row" key={label}><span className="kv-label"><T>{label}</T></span><span className="kv-value">{value ?? '—'}</span></div>);
}
export function Totals({
  values
}) {
  return Object.entries(values || {}).map(([currency, amount]) => <span className="d-block" key={currency}>{money(amount, currency)}</span>);
}
export function MemberBadges({
  names = [],
  empty
}) {
  return names.length ? <div className="d-flex flex-wrap gap-2">{names.map((n, i) => <span className="badge text-bg-light border p-2" key={i}>{n}</span>)}</div> : <div className="empty-state"><i className="bi bi-people display-6" /><p className="mb-0 mt-2">{empty}</p></div>;
}
export function Field({
  name,
  label,
  form,
  setForm,
  error,
  type = 'text',
  children,
  help,
  ...props
}) {
  const invalid = error?.fields?.[name];
  const inputProps = {
    id: name,
    name,
    className: `${children ? 'form-select' : 'form-control'}${invalid ? ' is-invalid' : ''}`,
    value: form[name] ?? '',
    onChange: e => setForm(f => ({
      ...f,
      [name]: e.target.value
    })),
    ...props
  };
  return <div className="mb-3"><label className="form-label" htmlFor={name}><T>{label}</T></label>{children ? <select {...inputProps}>{children}</select> : type === 'textarea' ? <textarea {...inputProps} rows="4" /> : <input {...inputProps} type={type} />} {help && <div className="form-text">{help}</div>}{invalid && <div className="invalid-feedback d-block">{invalid}</div>}</div>;
}
export function MemberSelect({
  members,
  value,
  onChange,
  label = 'Members',
  id = 'memberIds'
}) {
  return <div className="mb-3"><label className="form-label" htmlFor={id}><T>{label}</T></label><select className="form-select" id={id} multiple size="6" value={(value || []).map(String)} onChange={e => onChange([...e.target.selectedOptions].map(o => Number(o.value)))}>{members.map(m => <option value={m.id} key={m.id}>{m.name} {m.surname}</option>)}</select><div className="form-text">Hold Ctrl to select more than one member.</div></div>;
}
export function Save({
  busy,
  label = 'Save'
}) {
  return <button className="btn btn-success" type="submit" disabled={busy}><i className="bi bi-check-lg me-1" />{busy ? 'Saving…' : label}</button>;
}
