import {useEffect} from 'react';
import {useBlocker} from 'react-router-dom';
import {T, useTranslate} from '../context/LanguageContext';
import ConfirmDialog from './ConfirmDialog';

export default function UnsavedChangesGuard({dirty}) {
  const blocker = useBlocker(({currentLocation, nextLocation}) => dirty && (currentLocation.pathname !== nextLocation.pathname || currentLocation.search !== nextLocation.search));
  const tr = useTranslate();
  useEffect(() => {
    if (!dirty) return;
    const unload = e => {e.preventDefault(); e.returnValue = '';};
    window.addEventListener('beforeunload', unload);
    return () => window.removeEventListener('beforeunload', unload);
  }, [dirty]);
  return <>{dirty && <p className="profile-unsaved" role="status"><i className="bi bi-pencil" aria-hidden="true" /> <T>{'You have unsaved changes.'}</T></p>}{blocker.state === 'blocked' && <ConfirmDialog title={tr('Unsaved changes')} confirmLabel="Leave without saving" message="Your profile changes have not been saved. Leave this page?" onCancel={() => blocker.reset()} onConfirm={() => blocker.proceed()}/>}</>;
}
