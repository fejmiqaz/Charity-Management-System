import { T, useTranslate } from "../../context/LanguageContext";
import { useParams } from 'react-router-dom';
import { membersApi } from '../../api/api';
import useResource from '../../hooks/useResource';
import { Back, Rows, ResourceState } from '../../components/Template';
export default function MemberDetails() {
  const tr = useTranslate();
  const {
      id
    } = useParams(),
    r = useResource(() => membersApi.get(id), [id]),
    m = r.data;
  return <ResourceState {...r}>{m && <div className="container py-4" style={{
      maxWidth: 800
    }}><h1 className="mb-3"><T>{"Member Details"}</T></h1><nav className="detail-toolbar mb-4" aria-label={tr("Member actions")}><Back to="/members"><T>{"Back to Members"}</T></Back></nav><div className="card detail-hero text-center p-4 mb-4"><i className="bi bi-person-circle display-4 mb-2" /><span className="display-serif" style={{
          fontSize: 36
        }}>{m.name} {m.surname}</span></div><div className="card p-4 mb-4"><h2 className="section-title"><T>{"Personal Information"}</T></h2><Rows items={[["ID", m.id], ["First Name", m.name], ["Last Name", m.surname], ["Country", m.country], ["City", m.city]]} /></div><div className="card p-4"><h2 className="section-title"><T>{"Contact Information"}</T></h2><div className="kv-row"><span className="kv-label"><T>{"Email"}</T></span><a className="kv-value text-decoration-none" href={`mailto:${m.email}`}>{m.email}</a></div><div className="kv-row"><span className="kv-label"><T>{"Phone Number"}</T></span><a className="kv-value text-decoration-none" href={`tel:${m.phone}`}>{m.phone}</a></div></div></div>}</ResourceState>;
}
