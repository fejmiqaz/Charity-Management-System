import {useEffect, useState} from 'react';
import {api} from '../api/api';
import {useAuth} from '../context/AuthContext';
import {T, useTranslate} from '../context/LanguageContext';
import {ErrorAlert} from './Common';

export default function AccountSettings() {
  const {user, reload} = useAuth(), tr = useTranslate();
  const [form, setForm] = useState({currentPassword:'', password:'', confirmPassword:''});
  const [accounts, setAccounts] = useState([]), [error, setError] = useState(null), [saved, setSaved] = useState(false), [busy, setBusy] = useState(false);
  useEffect(() => { if (user?.role === 'HEAD') api('/accounts').then(setAccounts).catch(setError); }, [user?.role]);
  async function password(e) {
    e.preventDefault(); setBusy(true); setError(null); setSaved(false);
    try { await api('/profile/password', {method:'POST', body:form}); await reload(); setForm({currentPassword:'', password:'', confirmPassword:''}); setSaved(true); }
    catch (e) {setError(e);} finally {setBusy(false);}
  }
  async function role(id, value) {
    setBusy(true); setError(null);
    try { await api(`/accounts/${id}/role`, {method:'PUT', body:{role:value}}); setAccounts(await api('/accounts')); }
    catch(e) {setError(e);} finally {setBusy(false);}
  }
  return <section className="card p-4 my-4"><h2><T>{'Account access'}</T></h2><ErrorAlert error={error}/>
    <p><T>{'Create a password to sign in with your email or unique username.'}</T></p>
    {saved && <p role="status"><T>{'Password saved.'}</T></p>}
    <form onSubmit={password}>
      {(user?.passwordSet ? ['currentPassword','password','confirmPassword'] : ['password','confirmPassword']).map(key => <div className="form-group" key={key}>
        <label htmlFor={`account-${key}`}>{tr(key === 'currentPassword' ? 'Current password' : key === 'password' ? 'New password' : 'Confirm Password')}</label>
        <input id={`account-${key}`} className="form-control" type="password" required minLength={key === 'currentPassword' ? undefined : 8} maxLength={72}
          autoComplete={key === 'currentPassword' ? 'current-password' : 'new-password'} value={form[key]} onChange={e => setForm({...form,[key]:e.target.value})}/>
      </div>)}
      <button className="btn btn-primary" disabled={busy}><T>{'Save password'}</T></button>
    </form>
    {user?.role === 'HEAD' && <><h2 className="mt-4"><T>{'User roles'}</T></h2><div className="table-responsive"><table className="table"><thead><tr><th><T>{'Name'}</T></th><th><T>{'Email'}</T></th><th><T>{'Role'}</T></th></tr></thead><tbody>
      {accounts.map(a => <tr key={a.id}><td>{a.name}</td><td>{a.email}</td><td><select className="form-select" aria-label={tr('Role')} value={a.role} disabled={busy || a.id === user.id} onChange={e => role(a.id,e.target.value)}>
        {['HEAD','SUBHEAD','TREASURER','PROJECT_MANAGER','EVENT_MANAGER','VOLUNTEER','MEMBER','DONOR','SPONSOR'].map(r => <option key={r} value={r}>{tr(r)}</option>)}
      </select></td></tr>)}
    </tbody></table></div></>}
  </section>;
}
