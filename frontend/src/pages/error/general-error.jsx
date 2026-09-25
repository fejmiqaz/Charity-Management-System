import {ErrorText} from '../../components/Localized';
import { T, useTranslate } from "../../context/LanguageContext";
import { Link } from 'react-router-dom';
export default function GeneralError({
  error
}) {
  const tr = useTranslate();
  return <div className="container py-4"><div className="card p-4"><p className="eyebrow"><T>{"LET'S GET YOU BACK ON TRACK"}</T></p><h1><T>{"We couldn't complete that."}</T></h1><p className="text-secondary" role="alert"><ErrorText>{error?.message || tr('Please try again.')}</ErrorText></p><div><Link className="btn btn-primary" to="/dashboard"><T>{"Back to overview"}</T></Link></div></div></div>;
}
