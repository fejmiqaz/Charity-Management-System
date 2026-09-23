import { T } from "../../context/LanguageContext";
import { Link, useParams } from 'react-router-dom';
import { backendUrl } from '../../api/api';
import { templateApi } from '../../api/activity';
import useResource from '../../hooks/useResource';
import { ResourceState, Totals, Role } from '../../components/Template';
import { money } from '../../components/Common';
function Table({
  rows = [],
  columns,
  empty
}) {
  return rows.length ? <div className="table-responsive"><table className="table table-branded align-middle"><thead><tr>{columns.map(([label]) => <th scope="col" key={label}>{label}</th>)}</tr></thead><tbody>{rows.map((x, i) => <tr key={x.id ?? i}>{columns.map(([label, render]) => <td key={label}>{render(x)}</td>)}</tr>)}</tbody></table></div> : <p className="empty-state">{empty}</p>;
}
export default function YearDetails() {
  const {
      id
    } = useParams(),
    r = useResource(() => templateApi.year(id), [id]),
    d = r.data;
  if (!d) return <ResourceState {...r} />;
  const names = x => x.memberNames?.join(', ') || 'None assigned';
  const memberLink = m => <Link to={`/members/${m.id}`}>{m.name} {m.surname}</Link>;
  const sectionTitle = (title, path) => <div className="section-heading"><h2>{title}</h2><Link to={`/years/${id}/${path}`}>Open {path}</Link></div>;
  const paid = m => d.paidMemberships?.[m.id];
  return <ResourceState {...r}><div className="container py-4"><div className="section-heading"><div><p className="eyebrow">YEAR AT A GLANCE</p><h1>{d.year.yearValue} · Year details</h1><p className="text-secondary">All records for this year, together in one place.</p></div><div className="d-flex flex-wrap gap-2"><Link className="btn btn-outline-primary" to="/years">Back to years</Link><Role roles={['HEAD', 'SUBHEAD']}><a className="btn btn-primary" href={backendUrl(`/pdf/years/${id}`)}><T>{"Year Report PDF"}</T></a><a className="btn btn-outline-primary" href={backendUrl(`/years/${id}/export.xlsx`)}><i className="bi bi-file-earmark-spreadsheet me-1" /><T>{"Year Report Excel"}</T></a></Role></div></div><nav className="year-section-nav mb-4" aria-label="Year sections">{[['finances', 'bi-graph-up-arrow', 'Financial summary'], ['budget', 'bi-wallet2', 'Budget'], ...(d.projects ? [['projects', 'bi-kanban', 'Projects']] : []), ...(d.events ? [['events', 'bi-calendar-event', 'Events']] : []), ['donations', 'bi-heart', 'Donations'], ['donors', 'bi-person-heart', 'Donating members'], ['members', 'bi-people', 'Members'], ...(d.membershipPayments ? [['memberships', 'bi-receipt', 'Membership payments']] : [])].map(([key, icon, label]) => <a href={`#${key}`} key={key}><i className={`bi ${icon}`} /><span>{label}</span></a>)}</nav>
 <section id="finances" className="card p-4 mb-4"><h2 className="section-title">Financial summary</h2><div className="summary-row"><span>Allocated budget</span><strong>{money(d.budgetAmount, 'EUR')}</strong></div>{[['Total donations', d.donationTotals], ['Membership income', d.membershipTotals], ['Project income', d.projectIncomeTotals], ['Task payments', d.taskPaymentTotals], ['Recorded activity balance (donations + income + memberships − task payments)', d.activityBalance]].map(([label, values]) => <div className="summary-row" key={label}><span>{label}</span><strong><Totals values={values} /></strong></div>)}<div className="summary-row"><span>Total project cost</span><strong>{money(d.totalProjectCosts, 'EUR')}</strong></div><div className="summary-row total"><span>Remaining EUR budget</span><strong>{money(d.remainingBudget, 'EUR')}</strong></div><p className={`mb-0 mt-3 ${d.exceedsBudget ? 'status-bad' : 'status-ok'}`}>{d.exceedsBudget ? 'Over budget' : 'Within budget'}</p></section>
 <section id="budget" className="card p-4 mb-4">{sectionTitle('Budget', 'budget')}{!d.budget ? <p className="empty-state">No budget has been assigned for this year.</p> : <div><p><strong>Allocated amount: </strong>{money(d.budget.budgetAmount, 'EUR')}</p><p style={{
            whiteSpace: 'pre-wrap',
            overflowWrap: 'anywhere'
          }}>{d.budget.description || 'No budget description provided.'}</p><p><strong>Assigned members: </strong>{d.budget.memberNames?.join(', ') || 'None assigned'}</p><p className="mb-0"><strong>Linked donations: </strong>{d.budget.donationIds?.length ? d.budget.donationIds.map(did => <Link className="me-2" key={did} to={`/years/${id}/donations/${did}`}>Donation #{did}</Link>) : 'None linked'}</p></div>}</section>
 {d.projects && <section id="projects" className="card p-4 mb-4">{sectionTitle(`Projects (${d.projects.length})`, 'projects')}<Table rows={d.projects} empty="There are no projects for this year." columns={[["Project / description", p => <><Link className="fw-bold" to={`/years/${id}/projects/${p.id}`}>{p.name}</Link><p className="text-secondary mb-0 mt-1" style={{
            whiteSpace: 'pre-wrap'
          }}>{p.description}</p></>], ['Status', p => p.status || 'Not set'], ['Created', p => p.dateCreated || 'Not set'], ['Cost', p => money(p.projectPrice, 'EUR')], ['Members', names]]} /></section>}
 {d.events && <section id="events" className="card p-4 mb-4">{sectionTitle(`Events (${d.events.length})`, 'events')}<Table rows={d.events} empty="There are no events for this year." columns={[["Purpose", e => <Link to={`/years/${id}/events/${e.id}`}>{e.purpose}</Link>], ['Date and time', e => e.date?.replace('T', ' ') || 'Not scheduled'], ['Members', names]]} /></section>}
 <section id="donations" className="card p-4 mb-4">{sectionTitle(`Donations (${d.donations.length})`, 'donations')}<Table rows={d.donations} empty="There are no donations for this year." columns={[["Donation", x => <Link to={`/years/${id}/donations/${x.id}`}>Donation #{x.id}</Link>], ['Amount', x => money(x.donationAmount, x.currency)], ['Donating members', x => x.memberNames?.join(', ') || 'No member linked']]} /></section>
 <section id="donors" className="card p-4 mb-4"><h2 className="section-title">Donating members</h2><Table rows={d.donatingMembers} empty="No members are linked to this year's donations." columns={[["Name", memberLink], ['Email', m => m.email], ['Phone', m => m.phone], ['Country / city', m => `${m.country || ''} / ${m.city || ''}`]]} /></section>
 <section id="members" className="card p-4 mb-4"><h2 className="section-title">Members assigned to this year ({d.yearMembers.length})</h2><p className="text-secondary">Membership status for {d.year.yearValue} · Annual fee: {d.membershipFee} EUR</p><Table rows={d.yearMembers} empty="No members are assigned to this year." columns={[["Name", memberLink], ['Contact', m => <>{m.email}<br />{m.phone}</>], ['Country / city', m => `${m.country || ''} / ${m.city || ''}`], ['Role', m => m.role], ['Membership', m => <><span className={`badge ${paid(m) ? 'text-bg-success' : 'text-bg-secondary'}`}>{paid(m) ? 'Paid' : 'Unpaid'}</span>{paid(m) && <small className="d-block mt-1">{paid(m).amount} {paid(m).currency} · {paid(m).paidOn}</small>}</>]]} /></section>
 {d.membershipPayments && <section id="memberships" className="card p-4 mb-4"><div className="section-heading"><h2>Membership payments</h2><Link to={`/memberships?year=${d.year.yearValue}`}>Manage payments</Link></div><p className="text-secondary">All receipts for this calendar year, including members assigned to other years. Voided receipts are excluded from income.</p><Table rows={d.membershipPayments} empty="No membership payments recorded for this year." columns={[["Member", p => p.memberName], ['Amount', p => money(p.amount, p.currency)], ['Paid on', p => p.paidOn], ['Status', p => p.paid ? 'Paid' : 'Voided'], ['Void reason', p => p.voidReason]]} /></section>}
 </div></ResourceState>;
}
