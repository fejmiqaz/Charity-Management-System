import {useState} from 'react';
import {useAuth} from '../../context/AuthContext';
import {T, useTranslate} from '../../context/LanguageContext';
import {managementApi, accountsApi} from '../../api/api';
import useResource from '../../hooks/useResource';
import {roleOf} from '../../components/Template';
import AccessDenied from '../error/access-denied';
import {ErrorAlert} from '../../components/Common';
import {LocalizedDate} from '../../components/Localized';
import Pagination from '../../components/Pagination';
import ProfileTabs from '../../components/ProfileTabs';

export const activityActions = {CREATE:'Added', UPDATE:'Changed', DELETE:'Deleted', SIGN_IN:'Signed in', SIGN_OUT:'Signed out', SIGN_IN_FAILED:'Sign-in failed', PASSWORD_CHANGED:'Password changed', ROLE_CHANGED:'Role changed', SESSION_REVOKED:'Session ended'};

export default function Activity() {
  const {user} = useAuth();
  return roleOf(user) === 'HEAD' ? <ActivityContent /> : <AccessDenied />;
}
function ActivityContent() {
  const tr = useTranslate();
  const [page, setPage] = useState(0), [action, setAction] = useState(''), [actorId, setActorId] = useState('');
  const r = useResource(() => managementApi.activity({page, size:20, action, ...(actorId ? {actorId} : {})}), [page, action, actorId]);
  const people = useResource(accountsApi.list);
  return <div className="container py-4"><ProfileTabs /><div className="dashboard-heading"><div><p className="eyebrow"><T>{'HEAD ONLY'}</T></p><h1><T>{'Activity'}</T></h1><p className="text-secondary"><T>{'See who added, changed or deleted records across your organization.'}</T></p></div><button className="btn btn-outline-primary" type="button" disabled={r.loading} onClick={r.reload}><i className="bi bi-arrow-clockwise" aria-hidden="true" /> <T>{'Refresh'}</T></button></div>
    <p className="access-notice"><T>{'History starts when activity tracking is enabled. Changes show the affected record; passwords and private form contents are never recorded.'}</T></p>
    <div className="card p-3 mb-4"><div className="row g-3"><div className="col-md-6"><label htmlFor="activity-action" className="form-label"><T>{'Action'}</T></label><select className="form-select" id="activity-action" value={action} onChange={e => {setAction(e.target.value); setPage(0);}}><option value=""><T>{'All actions'}</T></option>{Object.entries(activityActions).map(([value,label]) => <option key={value} value={value}>{tr(label)}</option>)}</select></div><div className="col-md-6"><label htmlFor="activity-person" className="form-label"><T>{'Person'}</T></label><select className="form-select" id="activity-person" value={actorId} disabled={people.loading || Boolean(people.error)} onChange={e => {setActorId(e.target.value); setPage(0);}}><option value=""><T>{'All people'}</T></option>{people.data?.map(a => <option key={a.id} value={a.id}>{a.name} · {a.email}</option>)}</select></div></div><ErrorAlert error={people.error}/></div>
    <ErrorAlert error={r.error}/>
    {r.loading ? <p role="status"><T>{'Loading activity…'}</T></p> : !r.error && (!r.data?.content.length ? <div className="empty-state"><i className="bi bi-clock-history" aria-hidden="true" /><h2 className="h5"><T>{'No activity to show'}</T></h2><p><T>{'New actions will appear here. Try changing your filters or refreshing.'}</T></p></div> : <><div className="audit-list card">{r.data.content.map(entry => <article className="audit-row" key={entry.id}><span className="access-icon"><i className={`bi ${entry.action === 'DELETE' ? 'bi-trash' : entry.action.startsWith('SIGN_') ? 'bi-person-lock' : 'bi-pencil-square'}`} aria-hidden="true" /></span><div className="audit-copy"><div className="audit-title"><strong>{entry.actorName || entry.actorEmail || tr('Unknown account')}</strong><span className="access-status">{tr(activityActions[entry.action] || entry.action)}</span></div>{entry.actorName && <small>{entry.actorEmail}</small>}<p>{tr(entry.resource)} {entry.recordId && tr('Record #{id}', {id:entry.recordId})}</p><details><summary><T>{'View record reference'}</T></summary><code>{entry.path}</code></details></div><time dateTime={entry.occurredAt}><LocalizedDate value={entry.occurredAt} time /></time></article>)}</div><Pagination data={r.data} onChange={setPage}/></>)}
  </div>;
}
