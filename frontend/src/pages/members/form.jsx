import { useTranslate } from "../../context/LanguageContext";
import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { membersApi, optionsApi } from '../../api/api';
import { useWorkspace } from '../../context/WorkspaceContext';
import useResource from '../../hooks/useResource';
import { ErrorAlert } from '../../components/Common';
import { Back, Field, Save, ResourceState } from '../../components/Template';
export default function MemberForm() {
  const tr = useTranslate();
  const {
      id
    } = useParams(),
    nav = useNavigate(),
    {
      yearId
    } = useWorkspace();
  const [f, setF] = useState({
      name: '',
      surname: '',
      country: '',
      city: '',
      email: '',
      phone: '',
      password: '',
      role: 'MEMBER',
      yearId: yearId ? Number(yearId) : null
    }),
    [error, setError] = useState(null),
    [busy, setBusy] = useState(false);
  const r = useResource(async () => {
    const enums = await optionsApi.enums();
    if (id) setF({
      ...(await membersApi.get(id)),
      password: ''
    });
    return enums;
  }, [id]);
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const body = {
        ...f
      };
      if (id && !body.password) delete body.password;
      id ? await membersApi.update(id, body) : await membersApi.create(body);
      nav('/members');
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  return <ResourceState {...r}><div className="container py-4"><h1 className="mb-4">{id ? tr('Edit Member') : tr('Add Member')}</h1><div className="card p-4" style={{
        maxWidth: 760
      }}><ErrorAlert error={error} /><form onSubmit={submit}><div className="row g-3">{[['name', 'First Name'], ['surname', 'Last Name'], ['country', 'Country'], ['city', 'City'], ['email', 'Email'], ['phone', 'Phone Number']].map(([name, label]) => <div className="col-md-6" key={name}><Field name={name} label={label} form={f} setForm={setF} error={error} type={name === 'email' ? 'email' : 'text'} required /></div>)}<div className="col-md-6"><Field name="password" label={id ? 'New Password' : 'Password'} type="password" autoComplete="new-password" form={f} setForm={setF} error={error} required={!id} minLength="8" placeholder={tr(id ? 'Leave blank to keep the current password' : 'Enter password')} help={id ? 'Leave this field blank if you do not want to change the password.' : null} /></div>{!id && <div className="col-md-6"><Field name="role" label="Role" form={f} setForm={setF} error={error}>{(r.data?.roles || []).map(role => <option key={role} value={role}>{tr(role)}</option>)}</Field></div>}</div><div className="mt-4"><Save busy={busy} label={id ? 'Save Changes' : 'Add Member'} /></div></form></div><div className="mt-3"><Back to="/members" /></div></div></ResourceState>;
}
