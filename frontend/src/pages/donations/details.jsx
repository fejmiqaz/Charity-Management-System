import { T, useTranslate } from "../../context/LanguageContext";
import { useParams } from 'react-router-dom';
import { donationsApi, membersApi, backendUrl } from '../../api/api';
import useResource from '../../hooks/useResource';
import { Back, Rows, ResourceState, Role } from '../../components/Template';
export default function DonationDetails() {
  const tr = useTranslate();
  const {
      yearId,
      id
    } = useParams(),
    r = useResource(async () => {
      const donation = await donationsApi.get(yearId, id);
      const members = await Promise.all((donation.memberIds || []).map(mid => membersApi.get(mid)));
      return {
        donation,
        members
      };
    }, [yearId, id]),
    x = r.data?.donation;
  return <ResourceState {...r}>{x && <div className="container py-4" style={{
      maxWidth: 1000
    }}><h1 className="mb-3"><T>{"Donation Details"}</T></h1><nav className="detail-toolbar mb-4" aria-label={tr("Donation actions")}><Back to={`/years/${yearId}/donations`}><T>{"Back to Donations"}</T></Back><Role roles={['HEAD', 'SUBHEAD']}><a className="btn btn-danger" href={backendUrl(`/pdf/years/${yearId}/donations/${id}`)}><i className="bi bi-file-earmark-pdf me-1" /><T>{"Donation Report PDF"}</T></a></Role></nav><div className="card detail-hero text-center p-4 mb-4"><i className="bi bi-heart-fill display-4 mb-2" /><span className="display-serif" style={{
          fontSize: 36
        }}>{x.donationAmount} {x.currency}</span></div><div className="card p-4 mb-4"><h2 className="section-title"><T>{"Donation Information"}</T></h2><Rows items={[["ID", x.id], ["Amount", `${x.donationAmount} ${x.currency}`]]} /></div><div className="card p-4"><h2 className="section-title"><T>{"Donating Members"}</T></h2>{!r.data.members.length ? <div className="empty-state"><i className="bi bi-people display-6" /><p className="mb-0 mt-2"><T>{"No members are associated with this donation."}</T></p></div> : <div className="table-responsive"><table className="table table-branded table-hover align-middle mb-0"><thead><tr>{['Name', 'Surname', 'Email', 'Phone Number', 'Country', 'City', 'Amount'].map(s => <th key={s}><T>{s}</T></th>)}</tr></thead><tbody>{r.data.members.map(m => <tr key={m.id}>{['name', 'surname', 'email', 'phone', 'country', 'city'].map(k => <td key={k}>{m[k]}</td>)}<td className="fw-bold">{x.donationAmount} {x.currency}</td></tr>)}</tbody></table></div>}</div></div>}</ResourceState>;
}
