import {ErrorText} from '../../components/Localized';
import { T, useTranslate } from "../../context/LanguageContext";
import { useState } from 'react';
import { Navigate, Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import useBodyClass from '../../hooks/useBodyClass';
import AuthStory from './story';
import GoogleSignIn from '../../components/GoogleSignIn';

export default function Login() {
  const tr = useTranslate();
  useBodyClass('auth-page');
  const {
      user,
      signIn
    } = useAuth(),
    nav = useNavigate(),
    [params] = useSearchParams(),
    [f, setF] = useState({
      email: '',
      password: ''
    }),
    [error, setError] = useState(''),
    [busy, setBusy] = useState(false);
  if (user) return <Navigate to="/dashboard" replace />;
  async function submit(e) {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      await signIn(f.email, f.password);
      nav('/dashboard');
    } catch (e) {
      setError(e.status === 401 ? 'Invalid email or username, or password. If your username is shared, use your email.' : e.message);
    } finally {
      setBusy(false);
    }
  }
  return <><AuthStory /><div className="login-container"><main className="login-card"><div className="login-header"><h1><T>{"Sign In"}</T></h1><p><T>{"Enter your credentials to access the Charity Management System."}</T></p></div>{error && <div className="message error-message" role="alert"><ErrorText>{error}</ErrorText></div>}{[['googleError', 'Google sign-in failed. Sign in with your password and link the matching Google account from your profile first.'], ['logout', 'You have been logged out successfully.'], ['registered', 'Your account is ready. Sign in to get started.'], ['profileUpdated', 'Profile updated. Please sign in with your new email address.']].map(([key, text]) => params.has(key) && <div className="message success-message" role="status" key={key}><T>{text}</T></div>)}{params.get("username") && <p><T>{"Your username:"}</T> {params.get("username")}</p>}<form onSubmit={submit}><div className="form-group"><label htmlFor="username"><T>{"Email or username"}</T></label><input type="text" id="username" placeholder={tr("Email or username")} autoComplete="username" required value={f.email} onChange={e => setF({
              ...f,
              email: e.target.value
            })} /></div><div className="form-group"><label htmlFor="password"><T>{"Password"}</T></label><input type="password" id="password" placeholder={tr("Enter your password")} autoComplete="current-password" required value={f.password} onChange={e => setF({
              ...f,
              password: e.target.value
            })} /></div><button type="submit" className="login-btn" disabled={busy}>{busy ? tr('Signing in…') : tr('Sign In')}</button></form><GoogleSignIn /><p className="register-text"><T>{"Do not have an account?"}</T> <Link to="/register"><T>{"Create an account"}</T></Link></p><p className="register-text"><Link to="/"><T>{"Explore our charity"}</T></Link></p></main></div></>;
}
