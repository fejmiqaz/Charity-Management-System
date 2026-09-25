import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/api';
import { useAuth } from '../../context/AuthContext';
import { useLanguage } from '../../context/LanguageContext';
import { homeLocales, translateHome } from '../../i18n/home';
function useHomeText() {
  const {
    language
  } = useLanguage();
  return text => translateHome(language, text);
}
import useResource from '../../hooks/useResource';
import './home.css';
function Chart({
  title,
  description,
  bars = []
}) {
  const t = useHomeText(),
    {
      language
    } = useLanguage();
  return <figure className="home-chart"><figcaption><h3>{t(title)}</h3><p>{t(description)}</p></figcaption>{bars.length ? <ul>{bars.map(bar => <li key={bar.label}><span>{localizeMonth(bar.label, language)}</span><span className="home-track" aria-hidden="true"><i style={{
            width: `${Math.min(100, Math.max(0, bar.width))}%`
          }} /></span><strong>{bar.count}</strong></li>)}</ul> : <p>{t("Project history will appear as completed projects are published.")}</p>}</figure>;
}
function EventDate({
  date,
  compact = false
}) {
  const t = useHomeText(),
    {
      language
    } = useLanguage();
  // Public dates are local to the explicit server zone, not the visitor's zone.
  const value = new Date(`${date}Z`);
  if (Number.isNaN(value.getTime())) return <span>{t("Date to be announced")}</span>;
  return <time dateTime={date}>{new Intl.DateTimeFormat(homeLocales[language] || 'en-GB', {
      timeZone: 'UTC',
      ...(compact ? {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
      } : {
        weekday: 'long',
        day: 'numeric',
        month: 'long',
        hour: '2-digit',
        minute: '2-digit'
      })
    }).format(value)}</time>;
}
export default function Home() {
  const t = useHomeText(),
    {
      language,
      setLanguage
    } = useLanguage(),
    [theme, setTheme] = useState(document.documentElement.dataset.theme || 'light');
  const {
      user
    } = useAuth(),
    [menu, setMenu] = useState(false);
  const r = useResource(() => api('/public/home'), []),
    d = r.data;
  const destination = user ? '/dashboard' : '/login';
  useEffect(() => {
    const old = document.title;
    document.title = 'Charity · ' + translateHome(language, 'PEOPLE. PURPOSE. IMPACT.');
    return () => {
      document.title = old;
    };
  }, [language]);
  return <div className="public-home" id="top">
    <a className="home-skip" href="#home-main">{t("Skip to content")}</a>
    <header className="home-header"><div className="home-wrap home-nav"><Link className="home-brand" to="/"><span aria-hidden="true">♥</span><span>Charity<small>{t("PEOPLE. PURPOSE. IMPACT.")}</small></span></Link><nav className={menu ? 'is-open' : ''} aria-label={t("Public navigation")} id="home-navigation">{[['projects', 'Our work'], ['events', 'Events'], ['progress', 'Our progress']].map(([id, label]) => <a key={id} href={`#${id}`} onClick={() => setMenu(false)}>{t(label)}</a>)}</nav><div className="home-nav-actions"><select className="home-language" aria-label={t('Language')} value={language} onChange={e => setLanguage(e.target.value)}><option value="en">EN</option><option value="sq">SQ</option><option value="fr">FR</option><option value="de">DE</option></select><button aria-label={t(theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode')} title={t('Switch color theme')} onClick={() => {
            const next = theme === 'dark' ? 'light' : 'dark';
            setTheme(next);
            document.documentElement.dataset.theme = next;
            localStorage.setItem('charity-theme', next);
          }}><span aria-hidden="true">◐</span></button><Link className="home-button home-outline" to={destination}>{t(user ? 'My workspace' : 'Organization login')} ↗</Link><button className="home-menu" aria-label={t("Toggle navigation")} aria-controls="home-navigation" aria-expanded={menu} onClick={() => setMenu(!menu)}>☰</button></div></div></header>
    <main id="home-main">
      <section className="home-hero"><div className="home-wrap home-hero-grid"><div><span className="home-eyebrow"><i /> {t("Small actions. Lasting change.")}</span><h1>{t("Good work starts with")}<br /><em>{t("people.")}</em></h1><p>{t("Discover the projects we have completed and the moments that bring our community together.")}</p><div className="home-actions"><a className="home-button home-primary" href="#projects">{t("Explore our work")} <span>↗</span></a><a className="home-text-link" href="#events">{t("Upcoming events")} ↓</a></div></div><aside className="home-visual"><div className="home-next"><span className="home-eyebrow">{t(d?.events?.length ? 'Next public event' : 'More to come')}</span><h2>{d?.events?.[0]?.purpose || t('Room for the next good thing.')}</h2><p>{d?.events?.length ? <><EventDate date={d.events[0].date} /> · {d.publicZone}</> : r.loading ? t('Loading community updates…') : r.error ? t('Community updates are temporarily unavailable.') : t('New public events will appear here when announced.')}</p><a className="home-text-link" href="#events">{t("Upcoming events")} <span aria-hidden="true">↗</span></a></div></aside></div>
      <div className="home-wrap"><div className="home-stats">{[[d?.completedProjects, 'Completed projects shared'], [d?.events?.length, 'Upcoming public events'], [d?.impactYears, 'Project years represented']].map(([n, label]) => <div key={t(label)}><strong>{n ?? '—'}</strong><span>{t(label)}</span></div>)}<p>{t("People.")}<br />{t("Purpose.")}<br /><b>{t("Impact.")}</b></p></div></div></section>
      {r.error && <div className="home-wrap home-error" role="alert"><p>{t("Community updates couldn\u2019t be loaded. Please try again shortly.")}</p><button className="home-button home-outline" onClick={r.reload}>{t("Try again")}</button></div>}
      <section id="projects" className="home-section home-wrap"><div className="home-section-head"><div><span className="home-eyebrow">01 / {t("Completed projects")}</span><h2>{t("Good intentions.")}<br /><em>{t("Work delivered.")}</em></h2></div><p>{t("Explore the completed projects our organization has chosen to share publicly.")}</p></div>{r.loading ? <p role="status">{t("Loading published projects\u2026")}</p> : d && <div className="home-projects">{d.projects.length ? d.projects.map((project, i) => <article className="home-project" key={project.id ?? `${project.name}-${i}`}><span className="home-project-number">{String(i + 1).padStart(2, '0')}</span><div><span className="home-eyebrow">{project.yearValue ?? t('Community project')}</span><h3>{project.name}</h3></div><span className="home-completed">{t("\u2713 Completed")}</span></article>) : <div className="home-empty"><span aria-hidden="true">✳</span><h3>{t("More to share soon")}</h3><p>{t("Completed project highlights will appear here when ready to share.")}</p></div>}</div>}</section>
      <section id="events" className="home-events home-section"><div className="home-wrap"><div className="home-section-head"><div><span className="home-eyebrow">02 / {t("Coming together")}</span><h2>{t("The next dates")}<br /><em>{t("in our community.")}</em></h2></div><p>{t("Public events, with the closest upcoming date first.")}{d && ` ${t("Times shown in")} ${d.publicZone}.`}</p></div>{d && (d.events.length ? <div className="home-event-grid">{d.events.map((event, i) => <article className="home-event" key={event.id ?? i}><span className="home-event-date"><EventDate date={event.date} compact /></span><span className="home-event-symbol" aria-hidden="true">↗</span><h3>{event.purpose}</h3><p><EventDate date={event.date} /></p></article>)}</div> : <div className="home-empty"><span aria-hidden="true">↗</span><h3>{t("New dates are on their way")}</h3><p>{t("No upcoming public events have been announced yet.")}</p></div>)}</div></section>
      <section id="progress" className="home-section home-wrap"><div className="home-section-head"><div><span className="home-eyebrow">03 / {t("Our progress")}</span><h2>{t("A clearer picture")}<br /><em>{t("of our work.")}</em></h2></div><p>{t("Every count represents a published record. A transparent view of the work and moments we share.")}</p></div>{d && <div className="home-chart-grid"><Chart title={t("Completed projects by year")} description="Grouped by the project's assigned year." bars={d.projectChart} /><Chart title={t("What's ahead")} description="Upcoming public events over the next 12 calendar months." bars={d.eventChart} /></div>}</section>
      <section className="home-wrap home-portal"><div><span className="home-eyebrow">{t("For our organization")}</span><h2>{t("Your workspace.")}<br /><em>{t("Your next step.")}</em></h2><p>{t("Coordinate projects, organize events, and keep your work moving. Everything you need, together in one place.")}</p><Link className="home-button home-primary" to={destination}>{t(user ? 'Open my workspace' : 'Log in to the workspace')} ↗</Link></div></section>
    </main><footer className="home-wrap home-footer"><Link className="home-brand" to="/">♥ Charity</Link><span>© {d?.currentYear ?? new Date().getFullYear()} {t("Charity. People. Purpose. Impact.")}</span><a href="#top">{t("Back to top \u2191")}</a></footer>
  </div>;
}
function localizeMonth(label,language){const match=/^(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) (\d{4})$/.exec(label);if(!match)return label;const month=['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'].indexOf(match[1]);return new Intl.DateTimeFormat(homeLocales[language]||'en-GB',{month:'short',year:'numeric',timeZone:'UTC'}).format(new Date(Date.UTC(Number(match[2]),month,1)))}

