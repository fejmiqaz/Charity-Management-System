import {useState} from 'react';
import {api} from '../api/api';
import {useAuth} from '../context/AuthContext';
import {T, useTranslate} from '../context/LanguageContext';
import {ErrorAlert} from './Common';

export default function AccountSettings() {
  const {user, reload} = useAuth(), tr = useTranslate();
  const [form, setForm] = useState({currentPassword:'', password:'', confirmPassword:''});
  const [error, setError] = useState(null), [saved, setSaved] = useState(false), [busy, setBusy] = useState(false);
  async function password(e) {
    e.preventDefault(); setBusy(true); setError(null); setSaved(false);
    try { await api('/profile/password', {method:'POST', body:form}); await reload(); setForm({currentPassword:'', password:'', confirmPassword:''}); setSaved(true); }
    catch (e) {setError(e);} finally {setBusy(false);}
  }
  return <section className="mt-4 pt-4 border-top"><h2><T>{'Account access'}</T></h2><ErrorAlert error={error}/>
    {!user?.passwordSet && <p><T>{'Create a password before continuing to the workspace.'}</T></p>}
    {saved && <p role="status"><T>{'Password saved.'}</T></p>}
    <details open={user?.passwordSet === false}><summary><T>{user?.passwordSet ? 'Change password (optional)' : 'Create password'}</T></summary><form onSubmit={password}>
      {(user?.passwordSet ? ['currentPassword','password','confirmPassword'] : ['password','confirmPassword']).map(key => <div className="form-group" key={key}>
        <label htmlFor={`account-${key}`}>{tr(key === 'currentPassword' ? 'Current password' : key === 'password' ? 'New password' : 'Confirm Password')}</label>
        <input id={`account-${key}`} className="form-control" type="password" required minLength={key === 'currentPassword' ? undefined : 8} maxLength={72}
          autoComplete={key === 'currentPassword' ? 'current-password' : 'new-password'} value={form[key]} onChange={e => setForm({...form,[key]:e.target.value})}/>
      </div>)}
      <button className="btn btn-primary" disabled={busy}><T>{'Save password'}</T></button>
    </form></details>
  </section>;
}
