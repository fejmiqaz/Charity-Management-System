import { T } from "../../context/LanguageContext";
import { useParams } from 'react-router-dom';
import { eventsApi, backendUrl } from '../../api/api';
import useResource from '../../hooks/useResource';
import { Back, Rows, MemberBadges, ResourceState, Role } from '../../components/Template';
import Publication from '../../components/Publication';
import Tasks from './tasks';
export default function EventDetails() {
  const {
      yearId,
      id
    } = useParams(),
    r = useResource(() => eventsApi.get(yearId, id), [yearId, id]),
    x = r.data;
  return <ResourceState {...r}>{x && <div className="container py-4" style={{
      maxWidth: 900
    }}><h1 className="mb-3"><T>{"Event Details"}</T></h1><nav className="detail-toolbar mb-4" aria-label="Event actions"><Back to={`/years/${yearId}/events`}>Back to Events</Back><a className="btn btn-danger" href={backendUrl(`/pdf/years/${yearId}/events/${id}`)}><i className="bi bi-file-earmark-pdf me-1" /><T>{"Event Report PDF"}</T></a><a className="btn btn-outline-primary" href={backendUrl(`/years/${yearId}/events/${id}/export.xlsx`)}><i className="bi bi-file-earmark-spreadsheet me-1" /><T>{"Event Report Excel"}</T></a></nav><div className="card detail-hero text-center p-4 mb-4"><i className="bi bi-calendar-event display-4 mb-2" /><span className="display-serif" style={{
          fontSize: 36
        }}>{x.purpose}</span></div><div className="card p-4 mb-4"><h2 className="section-title">Event Information</h2><Rows items={[["ID", x.id], ["Purpose", x.purpose], ["Event type", x.eventType === 'TASK_BASED' ? 'Event with tasks' : 'Normal event'], ['Date and Time', x.date?.replace('T', ' ')], ['Year', x.yearValue ?? 'No Year']]} /></div><div className="card p-4"><h2 className="section-title">Participating Members</h2><MemberBadges names={x.memberNames} empty="No members have been assigned to this event." /></div>{x.eventType === 'TASK_BASED' && <Tasks yearId={yearId} id={id} />}<Role roles={['HEAD']}><Publication yearId={yearId} id={id} type="events" initial={x.publicVisible} /></Role></div>}</ResourceState>;
}
