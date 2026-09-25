import { T, useTranslate } from "../context/LanguageContext";
import { NavLink, useLocation, useNavigate } from "react-router-dom";
import { useWorkspace } from "../context/WorkspaceContext";
import { useAuth } from "../context/AuthContext";
const cleanRole = u => String(u?.role || u?.authority || '').replace(/^ROLE_/, '');
const allowed = (role, list) => !list || list.includes(role);
function N({
  to,
  icon,
  children,
  end = false
}) {
  return <NavLink to={to} end={end} className={({
    isActive
  }) => isActive ? 'active' : ''}><i className={`bi ${icon}`}></i><span>{children}</span></NavLink>;
}
export default function Sidebar() {
  const tr = useTranslate();
  const {
    years,
    yearId,
    setYearId
  } = useWorkspace();
  const {
    user
  } = useAuth();
  const role = cleanRole(user);
  const loc = useLocation(),
    nav = useNavigate();
  function changeYear(e) {
    const id = e.target.value;
    setYearId(id);
    const path = loc.pathname;
    if (path.includes('/projects')) nav(`/years/${id}/projects`);else if (path.includes('/donations')) nav(`/years/${id}/donations`);else if (path.includes('/events')) nav(`/years/${id}/events`);else if (path.includes('/budget')) nav(`/years/${id}/budget`);else if (/^\/years\/\d+/.test(path)) nav(`/years/${id}`);
  }
  return <aside className="app-sidebar" id="appSidebar" aria-label={tr("Main navigation")}><a className="app-brand" href="/dashboard" onClick={e => {
      e.preventDefault();
      nav('/dashboard');
    }}><span className="brand-symbol"><i className="bi bi-heart-pulse-fill"></i></span><span>Charity<span className="brand-subtitle"><T>{"PEOPLE. PURPOSE. IMPACT."}</T></span></span></a><div className="sidebar-label"><T>{"WORKSPACE"}</T></div><nav className="sidebar-links"><N to="/dashboard" icon="bi-grid-1x2"><T>{"Overview"}</T></N>{allowed(role, ['HEAD', 'SUBHEAD', 'TREASURER', 'MEMBER']) && <N to="/members" icon="bi-people"><T>{"Members"}</T></N>}{allowed(role, ['HEAD', 'SUBHEAD', 'MEMBER']) && <N to="/years" icon="bi-calendar3"><T>{"Yearly records"}</T></N>}</nav>{allowed(role, ['HEAD', 'SUBHEAD', 'TREASURER']) && <nav className="sidebar-links"><N to="/memberships" icon="bi-person-check"><T>{"Memberships"}</T></N></nav>}{yearId && <div className="sidebar-year"><label htmlFor="workspaceYear" className="sidebar-label"><T>{"WORKING YEAR"}</T></label><select id="workspaceYear" className="form-select" value={yearId} onChange={changeYear}>{years.map(y => <option key={y.id} value={y.id}>{y.yearValue}</option>)}</select></div>}{yearId ? <nav className="sidebar-links" aria-label={tr("Year sections")}>{allowed(role, ['HEAD', 'SUBHEAD', 'PROJECT_MANAGER', 'VOLUNTEER', 'MEMBER']) && <N to={`/years/${yearId}/projects`} icon="bi-kanban"><T>{"Projects"}</T></N>}{allowed(role, ['HEAD', 'SUBHEAD', 'TREASURER', 'MEMBER']) && <N to={`/years/${yearId}/donations`} icon="bi-heart"><T>{"Donations"}</T></N>}{allowed(role, ['HEAD', 'SUBHEAD', 'EVENT_MANAGER', 'VOLUNTEER', 'MEMBER']) && <N to={`/years/${yearId}/events`} icon="bi-calendar-event"><T>{"Events"}</T></N>}{allowed(role, ['HEAD', 'SUBHEAD', 'TREASURER', 'MEMBER']) && <N to={`/years/${yearId}/budget`} icon="bi-wallet2"><T>{"Budget"}</T></N>}<N to="/converter" icon="bi-currency-exchange"><T>{"Currency converter"}</T></N><a className="btn btn-outline-light mb-3" href="/" target="_blank" rel="noreferrer"><T>{"View public website ↗"}</T></a></nav> : <p className="sidebar-hint"><T>{"Create a yearly record to start organizing projects, donations, and events."}</T></p>}</aside>;
}
