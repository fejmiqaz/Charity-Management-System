import { T, useTranslate } from "../../context/LanguageContext";
import { useEffect, useState } from "react";
import { useWorkspace } from "../../context/WorkspaceContext";
import PageHeader from "../../components/PageHeader";
import DataTable from "../../components/DataTable";
import ErrorBox from "../../components/ErrorBox";
export default function YearResource({
  title,
  eyebrow,
  loader,
  columns
}) {
  const tr = useTranslate();
  const {
      yearId,
      year
    } = useWorkspace(),
    [rows, setRows] = useState([]),
    [error, setError] = useState(null);
  useEffect(() => {
    if (yearId) loader(yearId).then(x => setRows(Array.isArray(x) ? x : x?.content || [x].filter(Boolean))).catch(setError);
  }, [yearId, loader]);
  return <div className="container py-4"><PageHeader eyebrow={eyebrow} title={tr(title)} subtitle={year ? tr('{year} working year', {year: year.yearValue}) : "Select a working year from the sidebar."} /><ErrorBox error={error} />{yearId ? <DataTable rows={rows} columns={columns} /> : <div className="card p-5 text-center text-secondary"><T>{"No working year selected."}</T></div>}</div>;
}
