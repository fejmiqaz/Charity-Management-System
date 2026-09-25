import { T } from "../context/LanguageContext";
export default function PageHeader({
  eyebrow,
  title,
  subtitle,
  actions
}) {
  return <div className="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4"><div>{eyebrow && <p className="eyebrow mb-1"><T>{eyebrow}</T></p>}<h1 className="mb-1"><T>{title}</T></h1>{subtitle && <p className="text-secondary mb-0"><T>{subtitle}</T></p>}</div>{actions && <div className="d-flex gap-2">{actions}</div>}</div>;
}
