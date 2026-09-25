import {ErrorText} from '../../components/Localized';
import { T, useTranslate } from "../../context/LanguageContext";
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../../api/api';
import { ErrorAlert } from '../../components/Common';
import useBodyClass from '../../hooks/useBodyClass';
import AuthStory from './story';
export default function Register() {
  const tr = useTranslate();
  useBodyClass('auth-page');
  const nav = useNavigate(),
    [f, setF] = useState({
      name: '',
      surname: '',
      country: '',
      city: '',
      phone: '',
      email: '',
      password: '',
      confirmPassword: ''
    }),
    [error, setError] = useState(null),
    [busy, setBusy] = useState(false);
  async function submit(e) {
    e.preventDefault();
    if (f.password !== f.confirmPassword) {
      setError(new Error('Passwords must match.'));
      return;
    }
    setBusy(true);
    setError(null);
    try {
      await api('/auth/register', {
        method: 'POST',
        body: f
      });
      nav('/login?registered');
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  return <><AuthStory /><div className="auth-container"><div className="auth-card"><h1><T>{"Create Account"}</T></h1><p className="auth-description"><T>{"Register to access the Charity Management System."}</T></p><ErrorAlert error={error} /><form onSubmit={submit}><div className="form-grid">{[['name', 'First Name', 'Enter your first name'], ['surname', 'Last Name', 'Enter your last name'], ['country', 'Country', 'Country'], ['city', 'City', 'City'], ['phone', 'Phone Number', 'Phone number'], ['email', 'Email', 'Enter your email'], ['password', 'Password', 'Minimum 8 characters'], ['confirmPassword', 'Confirm Password', 'Repeat your password']].map(([name, label, placeholder]) => <div className="form-group" key={name}><label htmlFor={name}><T>{label}</T></label><input className="form-control" id={name} type={name.toLowerCase().includes('password') ? 'password' : name === 'email' ? 'email' : 'text'} value={f[name]} onChange={e => setF({
                ...f,
                [name]: e.target.value
              })} placeholder={tr(placeholder)} required minLength={name.toLowerCase().includes('password') ? 8 : undefined} maxLength={name === 'email' ? 254 : undefined} autoComplete={name.toLowerCase().includes('password') ? 'new-password' : undefined} />{error?.fields?.[name] && <small className="field-error"><ErrorText>{error.fields[name]}</ErrorText></small>}</div>)}</div><button type="submit" className="submit-button" disabled={busy}>{busy ? tr('Creating account…') : tr('Create Account')}</button></form><p className="auth-footer"><T>{"Already have an account?"}</T> <Link to="/login"><T>{"Log in"}</T></Link></p></div></div></>;
}
