import { useState } from 'react';
import { useLanguage, T, useTranslate } from '../context/LanguageContext';
export function ThemeButton({
  auth = false
}) {
  const tr = useTranslate();
  const [theme, setTheme] = useState(document.documentElement.dataset.theme || 'light');
  function toggle() {
    const next = theme === 'dark' ? 'light' : 'dark';
    document.documentElement.dataset.theme = next;
    localStorage.setItem('charity-theme', next);
    setTheme(next);
  }
  return <button type="button" className={auth ? '' : 'theme-toggle'} id="themeToggle" onClick={toggle} aria-label={tr(theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode')} title={tr("Switch color theme")}>{auth ? <span aria-hidden="true">◐</span> : <><i className={`bi ${theme === 'dark' ? 'bi-sun' : 'bi-moon-stars'}`} /><span><T>{"Theme"}</T></span></>}</button>;
}
export function LanguageControl({
  auth = false
}) {
  const tr = useTranslate();
  const {
    language,
    setLanguage
  } = useLanguage();
  const select = <select id={auth ? 'authLanguage' : 'appLanguage'} value={language} onChange={e => setLanguage(e.target.value)} aria-label={tr("Language")}><option value="en">EN</option><option value="sq">SQ</option><option value="fr">FR</option><option value="de">DE</option></select>;
  return auth ? select : <label className="language-control" htmlFor="appLanguage"><i className="bi bi-translate" /><span className="visually-hidden"><T>{"Language"}</T></span>{select}</label>;
}
