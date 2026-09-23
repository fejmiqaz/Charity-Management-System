import { T } from "../../context/LanguageContext";
import { Link, useSearchParams } from 'react-router-dom';
import { templateApi } from '../../api/activity';
import useResource from '../../hooks/useResource';
import { ResourceState } from '../../components/Template';
import MembershipHistory from '../memberships/history';
export default function Profile() {
  const r = useResource(templateApi.profile),
    a = r.data,
    [params] = useSearchParams();
  return <ResourceState {...r}>{a && <div className="container py-4"><div className="dashboard-heading"><div><p className="eyebrow">YOUR ACCOUNT</p><h1><T>{"My profile"}</T></h1><p className="text-secondary">Your details, all in one place.</p></div>{a.member && <Link className="btn btn-primary" to="/profile/edit"><i className="bi bi-pencil me-2" /><T>{"Edit my information"}</T></Link>}</div>{params.has('saved') && <div className="alert alert-success" role="status">Your information has been saved.</div>}<div className="profile-layout"><section className="profile-identity"><div className="profile-avatar"><i className="bi bi-person" /></div><h2>{a.name}</h2><p>{a.email}</p><span className="badge text-bg-light">{a.role?.replaceAll('_', ' ')}</span><p className="profile-note">Your role is managed by your organization.</p></section><section className="card p-4"><h2 className="section-title">Personal information</h2>{a.member ? <dl className="profile-details">{[['First name', a.member.name], ['Last name', a.member.surname], ['Email address', a.email], ['Phone number', a.member.phone || 'Not provided'], ['Country', a.member.country || 'Not provided'], ['City', a.member.city || 'Not provided']].map(([label, value]) => <div key={label}><dt>{label}</dt><dd>{value}</dd></div>)}</dl> : <p className="text-secondary">Your login account has no linked member record. Contact your organization head to set up a member profile.</p>}</section></div>{a.member && <MembershipHistory data={a} />}</div>}</ResourceState>;
}
