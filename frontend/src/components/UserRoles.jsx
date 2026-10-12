import {useState} from 'react';
import {accountsApi, optionsApi} from '../api/api';
import {useAuth} from '../context/AuthContext';
import {T, useTranslate} from '../context/LanguageContext';
import useResource from '../hooks/useResource';
import {ErrorAlert} from './Common';
import {roleDescriptions} from './roleDescriptions';

export default function UserRoles() {
  const {user} = useAuth(), tr = useTranslate();
  const r = useResource(async () => {
    const [accounts, enums] = await Promise.all([accountsApi.list(), optionsApi.enums()]);
    return {accounts, roles: enums.roles};
  });
  const [drafts, setDrafts] = useState({}), [busy, setBusy] = useState(null), [error, setError] = useState(null), [saved, setSaved] = useState(null);
  async function save(account) {
    const role = drafts[account.id];
    if (busy !== null || !role || role === account.role || String(account.id) === String(user.id)) return;
    setBusy(account.id); setError(null); setSaved(null);
    try {
      await accountsApi.role(account.id, role);
      r.setData(data => ({...data, accounts:data.accounts.map(a => a.id === account.id ? {...a, role} : a)}));
      setDrafts(current => {const next = {...current}; delete next[account.id]; return next;});
      setSaved(account.name || account.email);
    } catch (e) {setError(e);} finally {setBusy(null);}
  }
  return <div className="access-panel account-roles" aria-labelledby="account-roles-title">
    <div className="access-panel-heading"><h3 id="account-roles-title"><T>{'User roles'}</T></h3><span className="access-status"><i className="bi bi-shield-check" aria-hidden="true" /><T>{'Head only'}</T></span></div>
    <p className="text-secondary"><T>{'Choose a role, then save to update workspace permissions. Your own role cannot be changed here.'}</T></p>
    <ErrorAlert error={error || r.error}/>
    {saved && <div className="alert alert-success" role="status">{tr('Role updated for {name}.', {name:saved})}</div>}
    {r.loading ? <p role="status"><T>{'Loading accounts…'}</T></p> : r.error ? <button className="btn btn-outline-secondary" type="button" onClick={r.reload}><T>{'Try again'}</T></button> : <div className="access-account-list">{r.data?.accounts.map(account => {
      const self = String(account.id) === String(user.id), selected = drafts[account.id] ?? account.role;
      return <div className="access-account-row" key={account.id}>
        <div className="access-account-identity"><strong>{account.name || account.email} {self && <span className="access-self"><T>{'You'}</T></span>}</strong><span>{account.email}</span><small>{tr('Current role: {role}', {role:tr(account.role)})}</small></div>
        <div className="access-role-choice"><div className="access-role-controls"><label className="visually-hidden" htmlFor={`account-role-${account.id}`}>{tr('Role for {name}', {name:account.name || account.email})}</label><select className="form-select" id={`account-role-${account.id}`} aria-describedby={`role-help-${account.id}`} value={selected} disabled={self || busy !== null} onChange={e => {setDrafts({...drafts,[account.id]:e.target.value}); setSaved(null);}}>{r.data.roles.map(role => <option key={role} value={role}>{tr(role)}</option>)}</select><button type="button" className="btn btn-primary" disabled={self || busy !== null || selected === account.role} aria-label={tr('Save role for {name}', {name:account.name || account.email})} onClick={() => save(account)}>{busy === account.id ? tr('Saving…') : tr('Save')}</button></div><small id={`role-help-${account.id}`} className="text-secondary">{tr(roleDescriptions[selected] || 'Your role is managed by your organization.')}</small></div>
      </div>;
    })}</div>}
  </div>;
}
