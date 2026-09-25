import { T, useTranslate } from "../../context/LanguageContext";
import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { budgetApi } from '../../api/api';
import { useWorkspace } from '../../context/WorkspaceContext';
import useResource from '../../hooks/useResource';
import { Back, Role, ResourceState } from '../../components/Template';
import { ConfirmButton, ErrorAlert } from '../../components/Common';
export default function BudgetList() {
  const tr = useTranslate();
  const {
      yearId
    } = useParams(),
    {
      years
    } = useWorkspace(),
    year = years.find(y => String(y.id) === yearId),
    [error, setError] = useState(null);
  const r = useResource(() => budgetApi.get(yearId).catch(e => {
    if (e.status === 404) return null;
    throw e;
  }), [yearId]);
  async function remove() {
    try {
      await budgetApi.remove(yearId);
      r.reload();
    } catch (e) {
      setError(e);
    }
  }
  const x = r.data;
  return <ResourceState {...r}><div className="container py-4"><div className="d-flex flex-wrap justify-content-between align-items-center mb-4"><div><h1 className="mb-1"><T>{"Budget for Year"}</T>{" "}{year?.yearValue}</h1><p className="text-secondary mb-0"><T>{"Manage the year's budget"}</T></p></div></div><div className="page-toolbar mb-4"><Back to="/years"><T>{"Back to Years"}</T></Back><Link className="btn btn-outline-dark" to={`/years/${yearId}`}><i className="bi bi-info-circle me-1" /><T>{"Year Details"}</T></Link>{!x && <Role roles={['HEAD', 'TREASURER']}><Link className="btn btn-success" to={`/years/${yearId}/budget/add`}><i className="bi bi-plus-lg me-1" /><T>{"Add Budget"}</T></Link></Role>}</div><ErrorAlert error={error} />{!x ? <div className="empty-state"><i className="bi bi-cash-coin display-6" /><p className="mt-3 mb-2"><T>{"No budget was found for this year."}</T></p><Role roles={['HEAD', 'TREASURER']}><Link className="btn btn-success btn-sm" to={`/years/${yearId}/budget/add`}><T>{"Create Budget"}</T></Link></Role></div> : <div className="card p-0"><div className="table-responsive"><table className="table table-branded table-hover align-middle mb-0"><thead><tr><th>ID</th><th><T>{"Amount"}</T></th><th><T>{"Description"}</T></th><th><T>{"Actions"}</T></th></tr></thead><tbody><tr><td>{x.id}</td><td className="fw-bold">{x.budgetAmount} €</td><td>{x.description}</td><td><div className="d-flex gap-2"><Link className="btn btn-sm btn-outline-dark" to={`/years/${yearId}/budget/details/${x.id}`}><i className="bi bi-info-circle me-1" /><T>{"Details"}</T></Link><Role roles={['HEAD', 'TREASURER']}><Link className="btn btn-sm btn-warning" to={`/years/${yearId}/budget/edit`}><i className="bi bi-pencil me-1" /><T>{"Edit"}</T></Link><ConfirmButton onConfirm={remove} confirmText={tr('Delete budget {name}?', {name: x.description || x.id})} /></Role></div></td></tr></tbody></table></div></div>}</div></ResourceState>;
}
