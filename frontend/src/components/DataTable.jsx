import { T } from "../context/LanguageContext";
export default function DataTable({
  columns,
  rows = [],
  empty = "No records found.",
  actions
}) {
  return <div className="card overflow-hidden"><div className="table-responsive"><table className="table table-branded mb-0"><thead><tr>{columns.map(c => <th key={c.key}>{c.label}</th>)}{actions && <th><T>{"Actions"}</T></th>}</tr></thead><tbody>{rows.length ? rows.map((r, i) => <tr key={r.id ?? i}>{columns.map(c => <td key={c.key}>{c.render ? c.render(r) : r[c.key] ?? "—"}</td>)}{actions && <td>{actions(r)}</td>}</tr>) : <tr><td colSpan={columns.length + (actions ? 1 : 0)} className="text-center text-secondary py-5">{empty}</td></tr>}</tbody></table></div></div>;
}
