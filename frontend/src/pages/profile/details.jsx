import { T, useTranslate } from "../../context/LanguageContext";
import { Link, useSearchParams } from 'react-router-dom';
import { templateApi } from '../../api/activity';
import useResource from '../../hooks/useResource';
import { ResourceState } from '../../components/Template';
import MembershipHistory from '../memberships/history';
export default function Profile() {
  const tr = useTranslate();
  const r = useResource(templateApi.profile),
    a = r.data,
    [params] = useSearchParams();
  return <ResourceState {...r}>{a && <div className="container py-4"><div className="dashboard-heading"><div><p className="eyebrow"><T>{"YOUR ACCOUNT"}</T></p><h1><T>{"My profile"}</T></h1><p className="text-secondary"><T>{"Your details, all in one place."}</T></p></div>{a.member && <Link className="btn btn-primary" to="/profile/edit"><i className="bi bi-pencil me-2" /><T>{"Edit my information"}</T></Link>}</div>{params.has('saved') && <div className="alert alert-success" role="status"><T>{"Your information has been saved."}</T></div>}<div className="profile-layout"><section className="profile-identity"><div className="profile-avatar"><i className="bi bi-person" /></div><h2>{a.name}</h2><p>{a.email}</p><span className="badge text-bg-light"><T>{a.role}</T></span><p className="profile-note"><T>{"Your role is managed by your organization."}</T></p></section><section className="card p-4"><h2 className="section-title"><T>{"Personal information"}</T></h2>{a.member ? <dl className="profile-details">{[['First name', a.member.name], ['Last name', a.member.surname], ['Email address', a.email], ['Phone number', a.member.phone || tr('Not provided')], ['Country', a.member.country || tr('Not provided')], ['City', a.member.city || tr('Not provided')]].map(([label, value]) => <div key={label}><dt><T>{label}</T></dt><dd>{value}</dd></div>)}</dl> : <p className="text-secondary"><T>{"Your login account has no linked member record. Contact your organization head to set up a member profile."}</T></p>}</section></div>{a.member && <MembershipHistory data={a} />}</div>}</ResourceState>;
}
