import { T } from "../../context/LanguageContext";
import { useParams } from 'react-router-dom';
import { budgetApi, backendUrl } from '../../api/api';
import useResource from '../../hooks/useResource';
import { Back, Rows, Role, ResourceState } from '../../components/Template';
export default function BudgetDetails() {
  const {
      yearId
    } = useParams(),
    r = useResource(() => budgetApi.get(yearId), [yearId]),
    x = r.data;
  return <ResourceState {...r}>{x && <div className="container py-4" style={{
      maxWidth: 800
    }}><h1 className="mb-3"><T>{"Budget Details"}</T></h1><nav className="detail-toolbar mb-4" aria-label="Budget actions"><Back to={`/years/${yearId}/budget`}>Back to Budget</Back><Role roles={['HEAD', 'TREASURER']}><a className="btn btn-danger" href={backendUrl(`/pdf/years/${yearId}/budgets`)}><i className="bi bi-file-earmark-pdf me-1" /><T>{"Budget Report PDF"}</T></a></Role></nav><div className="card p-4"><h2 className="section-title">Budget Information</h2><Rows items={[["ID", x.id], ["Amount", `${x.budgetAmount} €`], ["Description", x.description]]} /></div></div>}</ResourceState>;
}
