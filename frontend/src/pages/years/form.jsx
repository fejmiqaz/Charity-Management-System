import { useTranslate } from "../../context/LanguageContext";
import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { yearsApi } from '../../api/api';
import useResource from '../../hooks/useResource';
import { ErrorAlert } from '../../components/Common';
import { Back, Field, Save, ResourceState } from '../../components/Template';
export default function YearForm() {
  const tr = useTranslate();
  const {
      id
    } = useParams(),
    nav = useNavigate(),
    [f, setF] = useState({
      yearValue: new Date().getFullYear()
    }),
    [error, setError] = useState(null),
    [busy, setBusy] = useState(false);
  const r = useResource(async () => {
    if (id) setF(await yearsApi.get(id));
    return true;
  }, [id]);
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    try {
      const body = {
        yearValue: Number(f.yearValue)
      };
      id ? await yearsApi.update(id, body) : await yearsApi.create(body);
      nav('/years');
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  return <ResourceState {...r}><div className="container py-4"><h1 className="mb-4">{id ? tr('Edit Year') : tr('Add Year')}</h1><div className="card p-4" style={{
        maxWidth: 650
      }}><ErrorAlert error={error} /><form onSubmit={submit}><Field name="yearValue" label="Year" form={f} setForm={setF} type="number" min="1900" max="2200" required error={error} /><Save busy={busy} /></form></div><div className="mt-3"><Back to="/years" /></div></div></ResourceState>;
}
