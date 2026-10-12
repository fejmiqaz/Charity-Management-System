import {useState} from 'react';
import {useAuth} from '../../context/AuthContext';
import {T, useTranslate} from '../../context/LanguageContext';
import {managementApi} from '../../api/api';
import useResource from '../../hooks/useResource';
import {roleOf} from '../../components/Template';
import AccessDenied from '../error/access-denied';
import {ErrorAlert} from '../../components/Common';
import ConfirmDialog from '../../components/ConfirmDialog';
import {LocalizedDate} from '../../components/Localized';
import ProfileTabs from '../../components/ProfileTabs';

export default function Security() {
  const {user} = useAuth();
  return roleOf(user) === 'HEAD' ? <SecurityContent /> : <AccessDenied />;
}
function SecurityContent() {
  const tr = useTranslate(), r = useResource(managementApi.security);
  const [ending, setEnding] = useState(null), [busy, setBusy] = useState(false), [error, setError] = useState(null), [saved, setSaved] = useState(false);
  async function revoke() {
    if (busy) return;
    setBusy(true); setError(null); setSaved(false);
    try {await managementApi.revokeSession(ending.reference); setEnding(null); setSaved(true); r.reload();}
    catch (e) {setError(e);} finally {setBusy(false);}
  }
  const data = r.data;
  return <div className="container py-4"><ProfileTabs /><div className="dashboard-heading"><div><p className="eyebrow"><T>{'HEAD ONLY'}</T></p><h1><T>{'Security'}</T></h1><p className="text-secondary"><T>{'Review account access and end sessions you do not recognize.'}</T></p></div><button className="btn btn-outline-primary" type="button" disabled={r.loading} onClick={r.reload}><i className="bi bi-arrow-clockwise" aria-hidden="true" /> <T>{'Refresh'}</T></button></div>
    <ErrorAlert error={r.error}/>{saved && <p className="alert alert-success" role="status"><T>{'Session ended. That browser must sign in again.'}</T></p>}
    {r.loading ? <p role="status"><T>{'Loading security overview…'}</T></p> : !r.error && data && <>
      <p className="text-secondary">{tr('Last checked:')} <LocalizedDate value={data.checkedAt} time /></p>
      <div className="security-metrics"><div className="card p-4"><span><T>{'Active sessions'}</T></span><strong>{data.sessions.length}</strong></div><div className="card p-4"><span><T>{'Failed sign-ins · last 24 hours'}</T></span><strong>{data.failedSignIns24h}</strong></div><div className="card p-4"><span><T>{'Accounts awaiting password setup'}</T></span><strong>{data.accounts.filter(a => a.enabled && !a.passwordSet).length}</strong></div></div>
      <section className="card p-4 mb-4"><h2 className="h4"><T>{'Active sessions'}</T></h2><p className="text-secondary"><T>{'An active session means a browser is signed in, not that the person is online right now. Sessions are tracked on this server and disappear after sign-out or expiry.'}</T></p>{!data.sessions.length ? <p><T>{'No active sessions found.'}</T></p> : data.sessions.map(session => <div className="access-account-row" key={session.reference}><div className="access-account-identity"><strong>{session.email} {session.current && <span className="access-self"><T>{'This session'}</T></span>}</strong><small><T>{'Signed in:'}</T> <LocalizedDate value={session.signedInAt} time /></small><small><T>{'Last activity:'}</T> <LocalizedDate value={session.lastSeenAt} time /></small></div><button className="btn btn-outline-danger" type="button" disabled={session.current} onClick={() => {setEnding(session); setError(null);}}><T>{'End session'}</T></button></div>)}</section>
      <section className="card p-4"><h2 className="h4"><T>{'Account security'}</T></h2><p className="text-secondary"><T>{'Password and Google connection status help identify unfinished account setup. Google connection is not two-factor authentication.'}</T></p>{data.accounts.map(account => <div className="access-account-row" key={account.id}><div className="access-account-identity"><strong>{account.name}</strong><span>{account.email}</span><small>{tr(account.role)}</small></div><div className="security-account-status"><span className="access-status">{tr(account.enabled ? 'Enabled' : 'Disabled')}</span><span className="access-status">{tr(account.passwordSet ? 'Password set' : 'Setup required')}</span><span className="access-status">{tr(account.googleLinked ? 'Google connected' : 'Google not connected')}</span></div></div>)}</section>
    </>}
    {ending && <ConfirmDialog title={tr('End session')} confirmLabel="End session" message={tr('End the session for {email}? They will need to sign in again.', {email:ending.email})} busy={busy} onCancel={() => setEnding(null)} onConfirm={revoke}><ErrorAlert error={error}/></ConfirmDialog>}
  </div>;
}
