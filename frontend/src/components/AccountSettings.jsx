import {useState} from 'react';
import {profileApi} from '../api/api';
import {useAuth} from '../context/AuthContext';
import {T, useTranslate} from '../context/LanguageContext';
import {ErrorAlert} from './Common';
import {Link} from 'react-router-dom';
import {roleOf} from './Template';
import UserRoles from './UserRoles';

export default function AccountSettings() {
  const {user, reload} = useAuth(), tr = useTranslate();
  const [form, setForm] = useState({currentPassword:'', password:'', confirmPassword:''});
  const [error, setError] = useState(null), [saved, setSaved] = useState(false), [busy, setBusy] = useState(false);
  const [visible, setVisible] = useState(false);
  const setup = user?.passwordSet === false;
  const mismatch = Boolean(form.confirmPassword && form.password !== form.confirmPassword);
  async function password(e) {
    e.preventDefault(); if (busy || mismatch) return;
    setBusy(true); setError(null); setSaved(false);
    try { await profileApi.password(form); await reload(); setForm({currentPassword:'', password:'', confirmPassword:''}); setVisible(false); setSaved(true); }
    catch (e) {setError(e);} finally {setBusy(false);}
  }
  return <section className="account-access" aria-labelledby="account-access-title">
    <div className="access-heading"><span className="access-icon"><i className="bi bi-shield-lock" aria-hidden="true" /></span><div><h2 id="account-access-title"><T>{'Account access'}</T></h2><p><T>{'Manage your password and account permissions.'}</T></p></div></div>
    <div className="access-panel"><div className="access-panel-heading"><h3><T>{'Password'}</T></h3><span className="access-status"><i className={`bi ${setup ? 'bi-exclamation-circle' : 'bi-check-circle'}`} aria-hidden="true" /><T>{setup ? 'Setup required' : 'Password set'}</T></span></div>
    {setup && <p className="access-notice"><T>{'Create a password before continuing to the workspace.'}</T></p>}
    <ErrorAlert error={error}/>
    {saved && <div className="alert alert-success" role="status"><T>{'Password saved.'}</T> <Link to="/dashboard"><T>{'Continue to workspace'}</T></Link></div>}
    <details open={setup}><summary><span><T>{setup ? 'Create password' : 'Change password (optional)'}</T></span><i className="bi bi-chevron-down" aria-hidden="true" /></summary><form onSubmit={password} className="access-password-form" aria-busy={busy}>
      <p className="text-secondary"><T>{'Create a password to sign in with your email or unique username.'}</T></p>
      <fieldset disabled={busy}>
      {(setup ? ['password','confirmPassword'] : ['currentPassword','password','confirmPassword']).map(key => <div className="form-group" key={key}>
        <label htmlFor={`account-${key}`}>{tr(key === 'currentPassword' ? 'Current password' : key === 'password' ? 'New password' : 'Confirm Password')}</label>
        <input id={`account-${key}`} className="form-control" type={visible ? 'text' : 'password'} required minLength={key === 'currentPassword' ? undefined : 8} maxLength={72}
          aria-describedby={key === 'password' ? 'account-password-help' : key === 'confirmPassword' && mismatch ? 'account-password-match' : undefined} aria-invalid={key === 'confirmPassword' && mismatch ? true : undefined}
          autoComplete={key === 'currentPassword' ? 'current-password' : 'new-password'} value={form[key]} onChange={e => {setForm({...form,[key]:e.target.value}); setSaved(false);}}/>
        {key === 'password' && <small id="account-password-help" className="text-secondary"><T>{'Use 8–72 characters. Accented letters and symbols may reach the limit sooner.'}</T></small>}
        {key === 'confirmPassword' && mismatch && <small id="account-password-match" className="text-danger" role="status"><T>{'Passwords do not match.'}</T></small>}
      </div>)}
      <div className="access-actions"><button className="btn btn-primary" disabled={mismatch}>{busy ? tr('Saving…') : tr(setup ? 'Create password' : 'Save password')}</button><button type="button" className="btn btn-outline-secondary" aria-pressed={visible} onClick={() => setVisible(!visible)}><i className={`bi ${visible ? 'bi-eye-slash' : 'bi-eye'}`} aria-hidden="true" /> {tr(visible ? 'Hide passwords' : 'Show passwords')}</button></div>
      </fieldset>
    </form></details></div>
    {roleOf(user) === 'HEAD' && !setup ? <UserRoles /> : <div className="access-role-note"><i className="bi bi-person-lock" aria-hidden="true" /><T>{'Your role is managed by your organization.'}</T></div>}
  </section>;
}
