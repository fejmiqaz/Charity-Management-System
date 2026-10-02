import {useEffect, useState} from 'react';
import {api, backendUrl} from '../api/api';
import {useTranslate} from '../context/LanguageContext';

export default function GoogleSignIn({link = false}) {
  const tr = useTranslate();
  const [enabled, setEnabled] = useState(false);
  useEffect(() => {
    let active = true;
    api('/auth/providers').then(p => { if (active) setEnabled(p.google); }).catch(() => {});
    return () => { active = false; };
  }, []);
  return enabled && <p className="register-text"><a className="btn btn-outline-primary" href={backendUrl('/oauth2/authorization/google')}>
    {link ? tr('Link Google account') : tr('Sign in with Google')}
  </a></p>;
}
