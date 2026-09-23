import { T } from "../../context/LanguageContext";
export default function MembershipHistory({
  data
}) {
  return <section className="card p-4 mt-4"><h2 className="section-title">My membership</h2><p><strong>{data.membershipYear}</strong> <span className={`badge ${data.membershipPaid ? 'badge-finished' : 'badge-pending'}`}>{data.membershipPaid ? 'Paid membership' : 'Regular member · no payment recorded'}</span></p><p className="text-secondary">Annual fee for new payments: €{data.membershipFee}. Contact the treasurer to pay or correct a receipt.</p>{!data.membershipHistory.length ? <p>No membership payments recorded yet.</p> : <div className="table-responsive"><table className="table table-branded"><thead><tr><th><T>{"Year"}</T></th><th><T>{"Amount"}</T></th><th>Paid on</th><th><T>{"Status"}</T></th></tr></thead><tbody>{data.membershipHistory.map(p => <tr key={p.id}><td>{p.membershipYear}</td><td>{p.amount} {p.currency}</td><td>{p.paidOn}</td><td>{p.paid ? 'Paid' : 'Voided'}</td></tr>)}</tbody></table></div>}</section>;
}
