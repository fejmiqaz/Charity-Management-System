import { T, useTranslate } from "../context/LanguageContext";
import { useState } from 'react';
import { activityApi } from '../api/activity';
import { backendUrl } from '../api/api';
import { ErrorAlert } from './Common';
export default function Publication({
  yearId,
  id,
  type,
  initial
}) {
  const tr = useTranslate();
  const [published, setPublished] = useState(!!initial),
    [error, setError] = useState(null),
    [busy, setBusy] = useState(false),
    [saved, setSaved] = useState(false);
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setSaved(false);
    try {
      await activityApi.publication(yearId, type, id, published);
      setSaved(true);
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  return <section className="card p-4 mt-4" aria-labelledby="publicationTitle"><h2 id="publicationTitle" className="section-title"><T>{"Public front page"}</T></h2>{type === 'projects' ? <><p><T>{"Approve only a title suitable for everyone to read. Publication shares the project title and year; descriptions, costs, members and attachments stay private. Only finished projects appear."}</T></p><p className="text-secondary"><T>{"Changing the title, year or status clears approval."}</T></p></> : <><p><T>{"Approve this event's purpose and scheduled date for public display. Check that the purpose contains no private details. Participant names stay private. Only upcoming approved events appear."}</T></p><p className="text-secondary"><T>{"Changing the purpose or date clears approval. Times use the front page's configured time zone."}</T></p></>}<ErrorAlert error={error} />{saved && <div className="alert alert-success" role="status"><T>{"Publication setting saved."}</T></div>}<form onSubmit={submit}><div className="form-check mb-3"><input className="form-check-input" id="published" type="checkbox" checked={published} onChange={e => {
          setPublished(e.target.checked);
          setSaved(false);
        }} /><label className="form-check-label" htmlFor="published"><T>{"Approved for public display"}</T></label></div><div className="d-flex flex-wrap gap-2"><button className="btn btn-primary" disabled={busy}>{busy ? tr('Saving…') : tr('Save publication setting')}</button><a className="btn btn-outline-primary" href={backendUrl('/')}><T>{"View front page"}</T></a></div></form></section>;
}
