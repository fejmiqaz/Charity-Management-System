import {LocalizedDate} from '../../components/Localized';
import { T, useTranslate } from "../../context/LanguageContext";
import { Link, useParams } from 'react-router-dom';
import { backendUrl } from '../../api/api';
import { templateApi } from '../../api/activity';
import useResource from '../../hooks/useResource';
import { ResourceState, Totals, Role } from '../../components/Template';
import { useMoney } from '../../components/Common';
function Table({
  rows = [],
  columns,
  empty
}) {
  return rows.length ? <div className="table-responsive"><table className="table table-branded align-middle"><thead><tr>{columns.map(([label]) => <th scope="col" key={label}><T>{label}</T></th>)}</tr></thead><tbody>{rows.map((x, i) => <tr key={x.id ?? i}>{columns.map(([label, render]) => <td key={label}>{render(x)}</td>)}</tr>)}</tbody></table></div> : <p className="empty-state"><T>{empty}</T></p>;
}
export default function YearDetails() {
  const money = useMoney();
  const tr = useTranslate();
  const {
      id
    } = useParams(),
    r = useResource(() => templateApi.year(id), [id]),
    d = r.data;
  if (!d) return <ResourceState {...r} />;
  const names = x => x.memberNames?.join(', ') || tr('None assigned');
  const memberLink = m => <Link to={`/members/${m.id}`}>{m.name} {m.surname}</Link>;
  const sectionTitle = (title, path) => <div className="section-heading"><h2><T>{title}</T></h2><Link to={`/years/${id}/${path}`}>{tr(`Open ${path}`)}</Link></div>;
  const paid = m => d.paidMemberships?.[m.id];
  return <ResourceState {...r}><div className="container py-4"><div className="section-heading"><div><p className="eyebrow"><T>{"YEAR AT A GLANCE"}</T></p><h1>{tr('{year} · Year details', {
              year: d.year.yearValue
            })}</h1><p className="text-secondary"><T>{"All records for this year, together in one place."}</T></p></div><div className="d-flex flex-wrap gap-2"><Link className="btn btn-outline-primary" to="/years"><T>{"Back to years"}</T></Link><Role roles={['HEAD', 'SUBHEAD']}><a className="btn btn-primary" href={backendUrl(`/pdf/years/${id}`)}><T>{"Year Report PDF"}</T></a><a className="btn btn-outline-primary" href={backendUrl(`/years/${id}/export.xlsx`)}><i className="bi bi-file-earmark-spreadsheet me-1" /><T>{"Year Report Excel"}</T></a></Role></div></div><nav className="year-section-nav mb-4" aria-label={tr("Year sections")}>{[['finances', 'bi-graph-up-arrow', 'Financial summary'], ['budget', 'bi-wallet2', 'Budget'], ...(d.projects ? [['projects', 'bi-kanban', 'Projects']] : []), ...(d.events ? [['events', 'bi-calendar-event', 'Events']] : []), ['donations', 'bi-heart', 'Donations'], ['donors', 'bi-person-heart', 'Donating members'], ['members', 'bi-people', 'Members'], ...(d.membershipPayments ? [['memberships', 'bi-receipt', 'Membership payments']] : [])].map(([key, icon, label]) => <a href={`#${key}`} key={key}><i className={`bi ${icon}`} /><span><T>{label}</T></span></a>)}</nav>
 <section id="finances" className="card p-4 mb-4"><h2 className="section-title"><T>{"Financial summary"}</T></h2><div className="summary-row"><span><T>{"Allocated budget"}</T></span><strong>{money(d.budgetAmount, 'EUR')}</strong></div>{[['Total donations', d.donationTotals], ['Membership income', d.membershipTotals], ['Project income', d.projectIncomeTotals], ['Task payments', d.taskPaymentTotals], ['Recorded activity balance (donations + income + memberships − task payments)', d.activityBalance]].map(([label, values]) => <div className="summary-row" key={label}><span><T>{label}</T></span><strong><Totals values={values} /></strong></div>)}<div className="summary-row"><span><T>{"Total project cost"}</T></span><strong>{money(d.totalProjectCosts, 'EUR')}</strong></div><div className="summary-row total"><span><T>{"Remaining EUR budget"}</T></span><strong>{money(d.remainingBudget, 'EUR')}</strong></div><p className={`mb-0 mt-3 ${d.exceedsBudget ? 'status-bad' : 'status-ok'}`}>{d.exceedsBudget ? tr('Over budget') : tr('Within budget')}</p></section>
 <section id="budget" className="card p-4 mb-4">{sectionTitle('Budget', 'budget')}{!d.budget ? <p className="empty-state"><T>{"No budget has been assigned for this year."}</T></p> : <div><p><strong><T>{"Allocated amount:"}</T> </strong>{money(d.budget.budgetAmount, 'EUR')}</p><p style={{
            whiteSpace: 'pre-wrap',
            overflowWrap: 'anywhere'
          }}>{d.budget.description || tr('No budget description provided.')}</p><p><strong><T>{"Assigned members:"}</T> </strong>{d.budget.memberNames?.join(', ') || tr('None assigned')}</p><p className="mb-0"><strong><T>{"Linked donations:"}</T> </strong>{d.budget.donationIds?.length ? d.budget.donationIds.map(did => <Link className="me-2" key={did} to={`/years/${id}/donations/${did}`}>{tr('Donation #{id}', {
                id: did
              })}</Link>) : tr('None linked')}</p></div>}</section>
 {d.projects && <section id="projects" className="card p-4 mb-4">{sectionTitle(tr('Projects ({count})', {
          count: d.projects.length
        }), 'projects')}<Table rows={d.projects} empty="There are no projects for this year." columns={[["Project / description", p => <><Link className="fw-bold" to={`/years/${id}/projects/${p.id}`}>{p.name}</Link><p className="text-secondary mb-0 mt-1" style={{
            whiteSpace: 'pre-wrap'
          }}>{p.description}</p></>], ['Status', p => tr(p.status || 'Not set')], ['Created', p => p.dateCreated ? <LocalizedDate value={p.dateCreated} /> : tr('Not set')], ['Cost', p => money(p.projectPrice, 'EUR')], ['Members', names]]} /></section>}
 {d.events && <section id="events" className="card p-4 mb-4">{sectionTitle(tr('Events ({count})', {
          count: d.events.length
        }), 'events')}<Table rows={d.events} empty="There are no events for this year." columns={[["Purpose", e => <Link to={`/years/${id}/events/${e.id}`}>{e.purpose}</Link>], ['Date and time', e => e.date ? <LocalizedDate value={e.date} time /> : tr('Not scheduled')], ['Members', names]]} /></section>}
 <section id="donations" className="card p-4 mb-4">{sectionTitle(tr('Donations ({count})', {
          count: d.donations.length
        }), 'donations')}<Table rows={d.donations} empty="There are no donations for this year." columns={[["Donation", x => <Link to={`/years/${id}/donations/${x.id}`}>{tr('Donation #{id}', {
            id: x.id
          })}</Link>], ['Amount', x => money(x.donationAmount, x.currency)], ['Donating members', x => x.memberNames?.join(', ') || tr('No member linked')]]} /></section>
 <section id="donors" className="card p-4 mb-4"><h2 className="section-title"><T>{"Donating members"}</T></h2><Table rows={d.donatingMembers} empty="No members are linked to this year's donations." columns={[["Name", memberLink], ['Email', m => m.email], ['Phone', m => m.phone], ['Country / city', m => `${m.country || ''} / ${m.city || ''}`]]} /></section>
 <section id="members" className="card p-4 mb-4"><h2 className="section-title">{tr('Members assigned to this year ({count})', {
            count: d.yearMembers.length
          })}</h2><p className="text-secondary">{tr('Membership status for {year} · Annual fee: {amount} EUR', {
            year: d.year.yearValue,
            amount: d.membershipFee
          })}</p><Table rows={d.yearMembers} empty="No members are assigned to this year." columns={[["Name", memberLink], ['Contact', m => <>{m.email}<br />{m.phone}</>], ['Country / city', m => `${m.country || ''} / ${m.city || ''}`], ['Role', m => tr(m.role)], ['Membership', m => <><span className={`badge ${paid(m) ? 'text-bg-success' : 'text-bg-secondary'}`}>{paid(m) ? tr('Paid') : tr('Unpaid')}</span>{paid(m) && <small className="d-block mt-1">{paid(m).amount} {paid(m).currency} · <LocalizedDate value={paid(m).paidOn} /></small>}</>]]} /></section>
 {d.membershipPayments && <section id="memberships" className="card p-4 mb-4"><div className="section-heading"><h2><T>{"Membership payments"}</T></h2><Link to={`/memberships?year=${d.year.yearValue}`}><T>{"Manage payments"}</T></Link></div><p className="text-secondary"><T>{"All receipts for this calendar year, including members assigned to other years. Voided receipts are excluded from income."}</T></p><Table rows={d.membershipPayments} empty="No membership payments recorded for this year." columns={[["Member", p => p.memberName], ['Amount', p => money(p.amount, p.currency)], ['Paid on', p => <LocalizedDate value={p.paidOn} />], ['Status', p => tr(p.paid ? 'Paid' : 'Voided')], ['Void reason', p => p.voidReason]]} /></section>}
 </div></ResourceState>;
}
