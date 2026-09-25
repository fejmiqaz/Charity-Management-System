import { T } from "../../context/LanguageContext";
import { Link } from 'react-router-dom';
import { LanguageControl, ThemeButton } from '../../components/ThemeControls';
export default function AuthStory() {
  return <><div className="auth-preferences"><LanguageControl auth /><ThemeButton auth /></div><aside className="auth-story"><Link className="auth-brand" to="/">♥ &nbsp; Charity</Link><div><p className="auth-eyebrow"><T>{"PEOPLE. PURPOSE. IMPACT."}</T></p><h2><T>{"Make room"}</T><br /><T>{"for good."}</T></h2><p><T>{"One place to bring your people together,"}</T><br /><T>{"organize your work, and make a difference."}</T></p><div className="auth-flower" aria-hidden="true">✳</div></div><small><T>{"A little organization. A lot of possibility."}</T></small></aside></>;
}
