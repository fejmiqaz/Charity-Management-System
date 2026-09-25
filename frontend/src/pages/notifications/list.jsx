import {LocalizedDate} from '../../components/Localized';
import {notificationTitle, notificationMessage} from '../../i18n/notifications.js';
import { T, useTranslate, useLanguage } from "../../context/LanguageContext";
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { notificationsApi } from '../../api/api';
import useResource from '../../hooks/useResource';
import { ErrorAlert } from '../../components/Common';
import ConfirmDialog from '../../components/ConfirmDialog';
export default function Notifications() {
  const tr = useTranslate();
  const {language} = useLanguage();
  const [page, setPage] = useState(0),
    [error, setError] = useState(null),
    [busy, setBusy] = useState(false),
    [clearOpen, setClearOpen] = useState(false),
    [success, setSuccess] = useState('');
  const r = useResource(async () => ({
    inbox: await notificationsApi.list(page),
    summary: await notificationsApi.summary()
  }), [page]);
  async function action(fn, message) {
    setBusy(true);
    setError(null);
    try {
      await fn();
      setSuccess(message);
      setClearOpen(false);
      r.reload();
      window.dispatchEvent(new Event('charity-notifications-changed'));
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  const d = r.data?.inbox;
  return <div className="container py-4 notification-inbox"><div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4"><div><h1 className="mb-1"><T>{"Notifications"}</T></h1><p className="text-secondary mb-0"><T>{"Your task assignments and upcoming events, all in one place."}</T></p></div><div className="d-flex gap-2">{r.data?.summary.unread > 0 && <button className="btn btn-outline-primary" disabled={busy} onClick={() => action(notificationsApi.readAll, 'All notifications marked as read.')}><i className="bi bi-check2-all me-1" /><T>{"Mark all as read"}</T></button>}{(d?.totalElements ?? d?.content?.length) > 0 && <button className="btn btn-outline-danger" onClick={() => setClearOpen(true)}><i className="bi bi-trash me-1" /><T>{"Clear all notifications"}</T></button>}</div></div><ErrorAlert error={error || r.error} />{success && <div className="alert alert-success" role="status"><T>{success}</T></div>}{clearOpen && <ConfirmDialog title={tr("Clear all notifications?")} message="This clears all read and unread notifications from your inbox and header. You will still receive new notifications and emails." confirmLabel="Clear all notifications" busy={busy} onCancel={() => setClearOpen(false)} onConfirm={() => action(async () => {
      await notificationsApi.clear();
      setPage(0);
    }, 'Notifications cleared.')} />} {r.loading ? <p role="status"><T>{"Loading notifications…"}</T></p> : d && (!d.content.length ? <div className="empty-state"><i className="bi bi-bell display-6" /><h2 className="h5 mt-3"><T>{"You're all caught up"}</T></h2><p className="text-secondary mb-0"><T>{"New task assignments and event reminders will appear here."}</T></p></div> : d.content.map(i => <article className={`card notification-card p-4 mb-3 ${i.unread ? 'is-unread' : ''}`} id={`notification-${i.id}`} key={i.id}><div className="d-flex align-items-start gap-3"><span className="notification-icon"><i className="bi bi-bell" /></span><div className="flex-grow-1 notification-copy"><div className="d-flex flex-wrap align-items-center gap-2 mb-2"><h2 className="h5 mb-0">{notificationTitle(language, i.title)}</h2>{i.unread && <span className="badge rounded-pill text-bg-primary"><T>{"New"}</T></span>}</div><p className="notification-message text-secondary">{notificationMessage(language, i.message)}</p><div className="d-flex flex-wrap gap-3 align-items-center">{i.eventPath && <Link className="btn btn-sm btn-outline-primary" to={i.eventPath}><T>{"View event"}</T></Link>}{i.unread && <button className="btn btn-sm btn-outline-secondary" disabled={busy} onClick={() => action(() => notificationsApi.read(i.id), 'Notification marked as read.')}><T>{"Mark as read"}</T></button>}<time className="small text-secondary"><LocalizedDate value={i.createdAt} time /></time></div></div></div></article>))}{d?.totalPages > 1 && <nav className="d-flex justify-content-between mt-4" aria-label={tr("Notification pages")}><button className="btn btn-outline-secondary" disabled={page === 0} onClick={() => setPage(page - 1)}><T>{"Newer"}</T></button><span className="text-secondary">{tr('Page {page} of {total}', {page: page + 1, total: d.totalPages})}</span><button className="btn btn-outline-secondary" disabled={page + 1 >= d.totalPages} onClick={() => setPage(page + 1)}><T>{"Older"}</T></button></nav>}</div>;
}
