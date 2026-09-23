import { T } from "../../context/LanguageContext";
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { profileApi } from '../../api/api';
import { useAuth } from '../../context/AuthContext';
import useResource from '../../hooks/useResource';
import { Field, ResourceState } from '../../components/Template';
import { ErrorAlert } from '../../components/Common';
export default function ProfileForm() {
  const nav = useNavigate(),
    {
      reload,
      clearSession
    } = useAuth(),
    [f, setF] = useState({}),
    [error, setError] = useState(null),
    [busy, setBusy] = useState(false);
  const r = useResource(async () => {
    setF(await profileApi.get());
    return true;
  });
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    try {
      const result = await profileApi.update(f);
      if (result.loginRequired) {
        clearSession();
        nav('/login?profileUpdated');
      } else {
        await reload();
        nav('/profile?saved');
      }
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  return <ResourceState {...r}><div className="container py-4"><p className="eyebrow">MY PROFILE</p><h1>Edit personal information</h1><p className="text-secondary mb-4">Keep your contact details up to date. All fields are required.</p><div className="card p-4 profile-form"><ErrorAlert error={error} /><form onSubmit={submit}><div className="row g-4">{[['name', 'First name'], ['surname', 'Last name'], ['email', 'Email address'], ['phone', 'Phone number'], ['country', 'Country'], ['city', 'City']].map(([name, label]) => <div className="col-md-6" key={name}><Field name={name} label={label} form={f} setForm={setF} error={error} required type={name === 'email' ? 'email' : name === 'phone' ? 'tel' : 'text'} /></div>)}</div><div className="profile-email-note mt-4"><i className="bi bi-info-circle me-2" />If you change your email, you will be signed out and can sign in again with the new address.</div><div className="d-flex gap-2 mt-4"><button className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Save changes'}</button><Link className="btn btn-outline-secondary" to="/profile"><T>{"Cancel"}</T></Link></div></form></div></div></ResourceState>;
}
