import {NavLink} from 'react-router-dom';
import {T, useTranslate} from '../context/LanguageContext';
import {useAuth} from '../context/AuthContext';
import {roleOf} from './Template';

export default function ProfileTabs() {
  const {user} = useAuth();
  const tr = useTranslate();
  return roleOf(user) === 'HEAD' && <nav className="profile-tabs" aria-label={tr('Account')}>
    <NavLink to="/profile" end><i className="bi bi-person" aria-hidden="true" /><T>{'Profile'}</T></NavLink>
    <NavLink to="/profile/activity"><i className="bi bi-clock-history" aria-hidden="true" /><T>{'Activity'}</T></NavLink>
    <NavLink to="/profile/security"><i className="bi bi-shield-lock" aria-hidden="true" /><T>{'Security'}</T></NavLink>
  </nav>;
}
