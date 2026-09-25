import { T, useTranslate } from "../context/LanguageContext";
export default function Pagination({
  data,
  onChange
}) {
  const tr = useTranslate();
  const page = data.page ?? 0,
    total = data.totalPages ?? 1;
  if (total <= 1) return null;
  const start = Math.max(0, Math.min(page - 2, total - 5)),
    pages = Array.from({
      length: Math.min(5, total)
    }, (_, i) => start + i);
  return <nav className="mt-4" aria-label={tr("Pagination")}><ul className="pagination justify-content-center flex-wrap"><li className={`page-item ${page === 0 ? 'disabled' : ''}`}><button className="page-link" disabled={page === 0} onClick={() => onChange(page - 1)}><i className="bi bi-chevron-left" /> <T>{"Previous"}</T></button></li>{pages.map(n => <li className={`page-item ${n === page ? 'active' : ''}`} key={n}><button className="page-link" aria-current={n === page ? 'page' : undefined} onClick={() => onChange(n)}>{n + 1}</button></li>)}<li className={`page-item ${page + 1 >= total ? 'disabled' : ''}`}><button className="page-link" disabled={page + 1 >= total} onClick={() => onChange(page + 1)}><T>{"Next"}</T> <i className="bi bi-chevron-right" /></button></li></ul></nav>;
}
