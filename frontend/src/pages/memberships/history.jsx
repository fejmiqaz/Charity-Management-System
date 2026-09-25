import {LocalizedDate} from '../../components/Localized';
import { T, useTranslate } from "../../context/LanguageContext";
export default function MembershipHistory({
  data
}) {
  const tr = useTranslate();
  return <section className="card p-4 mt-4"><h2 className="section-title"><T>{"My membership"}</T></h2><p><strong>{data.membershipYear}</strong> <span className={`badge ${data.membershipPaid ? 'badge-finished' : 'badge-pending'}`}>{data.membershipPaid ? tr('Paid membership') : tr('Regular member · no payment recorded')}</span></p><p className="text-secondary">{tr('Annual fee for new payments: €{amount}. Contact the treasurer to pay or correct a receipt.', {amount: data.membershipFee})}</p>{!data.membershipHistory.length ? <p><T>{"No membership payments recorded yet."}</T></p> : <div className="table-responsive"><table className="table table-branded"><thead><tr><th><T>{"Year"}</T></th><th><T>{"Amount"}</T></th><th><T>{"Paid on"}</T></th><th><T>{"Status"}</T></th></tr></thead><tbody>{data.membershipHistory.map(p => <tr key={p.id}><td>{p.membershipYear}</td><td>{p.amount} {p.currency}</td><td><LocalizedDate value={p.paidOn} /></td><td>{p.paid ? tr('Paid') : tr('Voided')}</td></tr>)}</tbody></table></div>}</section>;
}
