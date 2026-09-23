import { T } from "../context/LanguageContext";
import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LanguageControl, ThemeButton } from './ThemeControls';
import NotificationBell from './NotificationBell';
import PageFinder from './PageFinder';
export default function Header() {
  const {
      user,
      signOut
    } = useAuth(),
    nav = useNavigate(),
    loc = useLocation(),
    [finder, setFinder] = useState(false),
    [menu, setMenu] = useState(false),
    [settings, setSettings] = useState(false),
    [error, setError] = useState(null);
  useEffect(() => {
    setMenu(false);
    setSettings(false);
  }, [loc.pathname]);
  useEffect(() => {
    document.body.classList.toggle('nav-open', menu);
    return () => document.body.classList.remove('nav-open');
  }, [menu]);
  useEffect(() => {
    const fn = e => {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        setFinder(true);
      }
      if (e.key === 'Escape') {
        setFinder(false);
        setMenu(false);
        setSettings(false);
      }
    };
    window.addEventListener('keydown', fn);
    return () => window.removeEventListener('keydown', fn);
  }, []);
  async function out(e) {
    e.preventDefault();
    try {
      await signOut();
      nav('/login?logout');
    } catch (e) {
      setError(e.message);
    }
  }
  return <><header className="workspace-topbar"><div className="topbar-left d-flex align-items-center gap-3"><button className="mobile-menu btn btn-outline-secondary" type="button" aria-controls="appSidebar" aria-expanded={menu} aria-label="Toggle navigation" onClick={() => setMenu(!menu)}><i className="bi bi-list" /></button><Link className="header-brand" to="/dashboard"><span className="header-logo"><i className="bi bi-heart-pulse-fill" /></span><span><T>{"Charity Management"}</T></span></Link></div><button className="mobile-search-trigger btn btn-outline-secondary" type="button" aria-label="Find a page" onClick={() => setFinder(true)}><i className="bi bi-search" /></button><NotificationBell /><button className="mobile-actions-toggle btn btn-outline-secondary" type="button" aria-controls="mobileActions" aria-expanded={settings} aria-label="Open account and settings" onClick={() => setSettings(true)}><i className="bi bi-three-dots-vertical" /></button><div className="mobile-actions-backdrop" hidden={!settings} onClick={() => setSettings(false)} /><div className={`mobile-actions ${settings ? 'open' : ''}`} id="mobileActions" aria-label="Account and settings"><div className="mobile-actions-heading"><strong>Account and settings</strong><button className="mobile-actions-close" type="button" aria-label="Close account and settings" onClick={() => setSettings(false)}><i className="bi bi-x-lg" /></button></div><Link className="mobile-profile-link" to="/profile"><i className="bi bi-person" /><span><T>{"Profile"}</T></span></Link><div className="topbar-tools"><button type="button" className="jump-trigger" aria-label="Find a page" onClick={() => setFinder(true)}><i className="bi bi-search" /><span><T>{"Find a page"}</T></span><kbd>Ctrl K</kbd></button></div><div className="topbar-right"><LanguageControl /><ThemeButton /><details className="account-menu"><summary className="account-label account-profile" aria-label="Open account menu"><i className="bi bi-person-circle" /><span className="account-copy"><strong>{user?.email || 'Account'}</strong></span><i className="bi bi-chevron-down" /></summary><div className="account-menu-panel"><Link to="/profile"><i className="bi bi-person" /><span><T>{"Profile"}</T></span></Link><form onSubmit={out}><button type="submit"><i className="bi bi-box-arrow-right" /><span><T>{"Sign out"}</T></span></button></form></div></details><form className="mobile-signout-form" onSubmit={out}><button className="mobile-signout" type="submit"><i className="bi bi-box-arrow-right" /><span><T>{"Sign out"}</T></span></button></form></div>{error && <p className="text-danger" role="alert">{error}</p>}</div></header>{menu && <button className="mobile-menu-backdrop" aria-label="Close navigation" onClick={() => setMenu(false)} />}<PageFinder open={finder} onClose={() => setFinder(false)} /></>;
}
