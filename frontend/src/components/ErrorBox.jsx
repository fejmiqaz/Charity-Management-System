import {ErrorText} from './Localized';
export default function ErrorBox({
  error
}) {
  return error ? <div className="alert alert-danger"><ErrorText>{error?.payload?.message || error?.message || String(error)}</ErrorText></div> : null;
}
