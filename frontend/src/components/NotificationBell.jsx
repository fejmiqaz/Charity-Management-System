import {notificationTitle, notificationMessage} from '../i18n/notifications.js';
import { T, useTranslate, useLanguage } from "../context/LanguageContext";
import { useEffect, useRef, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { notificationsApi } from '../api/api';
export default function NotificationBell() {
  const tr = useTranslate();
  const {language} = useLanguage();
  const [s, setS] = useState(null),
    menu = useRef(null),
    loc = useLocation();
  useEffect(() => {
    let active = true;
    const load = () => notificationsApi.summary().then(s => {
      if (active) setS(s);
    }).catch(() => {});
    load();
    window.addEventListener('charity-notifications-changed', load);
    return () => {
      active = false;
      window.removeEventListener('charity-notifications-changed', load);
    };
  }, []);
  useEffect(() => {
    if (menu.current) menu.current.open = false;
  }, [loc.pathname]);
  useEffect(() => {
    function close(e) {
      if (e.type === 'keydown' && e.key !== 'Escape') return;
      if (e.type === 'click' && menu.current?.contains(e.target)) return;
      if (menu.current) menu.current.open = false;
    }
    document.addEventListener('click', close);
    document.addEventListener('keydown', close);
    return () => {
      document.removeEventListener('click', close);
      document.removeEventListener('keydown', close);
    };
  }, []);
  const unread = s?.unread || 0;
  return <details className="notification-menu" id="notificationMenu" ref={menu}><summary className="notification-bell" title={tr("Notifications")} aria-label={tr("Notifications")}><i className="bi bi-bell" />{unread > 0 && <span className="notification-count">{unread > 99 ? '99+' : unread}</span>}</summary><div className="notification-panel"><div className="notification-panel-heading"><strong><T>{"Notifications"}</T></strong><span className="text-secondary">{tr('{count} unread', {count: unread})}</span></div>{!s?.items?.length ? <div className="notification-empty"><i className="bi bi-bell" /><strong><T>{"You're all caught up"}</T></strong><span><T>{"Task assignments and upcoming event reminders will appear here."}</T></span></div> : s.items.slice(0, 5).map(i => <Link className={`notification-preview ${i.unread ? 'is-unread' : ''}`} key={i.id} to={`/notifications#notification-${i.id}`}>{i.unread && <span className="notification-dot" />}<strong>{notificationTitle(language, i.title)}</strong><span>{notificationMessage(language, i.message)}</span></Link>)}<Link className="notification-view-all" to="/notifications"><T>{"View all notifications"}</T> <i className="bi bi-arrow-right" /></Link></div></details>;
}
