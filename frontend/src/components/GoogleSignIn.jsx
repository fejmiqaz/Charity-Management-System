import {useEffect, useState} from 'react';
import {api, backendUrl} from '../api/api';
import {useTranslate} from '../context/LanguageContext';

export default function GoogleSignIn({link = false}) {
  const tr = useTranslate();
  const [enabled, setEnabled] = useState(null), [error, setError] = useState(false);
  useEffect(() => {
    let active = true;
    api('/auth/providers').then(p => { if (active) setEnabled(p.google); }).catch(() => {if (active) setError(true);});
    return () => { active = false; };
  }, []);
  if (link && error) return <p className="text-secondary">{tr('Google sign-in availability could not be checked.')}</p>;
  if (link && enabled === false) return <p className="text-secondary">{tr('Google sign-in is not enabled for this organization.')}</p>;
  return enabled && <p className="register-text"><a className="btn btn-outline-primary" href={backendUrl('/oauth2/authorization/google')}>
    {link ? tr('Link Google account') : tr('Sign in with Google')}
  </a></p>;
}
