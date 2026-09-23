import {Link} from 'react-router-dom';
import {LanguageControl,ThemeButton} from '../../components/ThemeControls';
export default function AuthStory(){return <><div className="auth-preferences"><LanguageControl auth/><ThemeButton auth/></div><aside className="auth-story"><Link className="auth-brand" to="/">♥ &nbsp; Charity</Link><div><p className="auth-eyebrow">PEOPLE. PURPOSE. IMPACT.</p><h2>Make room<br/>for good.</h2><p>One place to bring your people together,<br/>organize your work, and make a difference.</p><div className="auth-flower" aria-hidden="true">✳</div></div><small>A little organization. A lot of possibility.</small></aside></>}

