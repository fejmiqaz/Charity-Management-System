import { T } from "../context/LanguageContext";
import { useEffect, useId, useRef } from 'react';
import { createPortal } from 'react-dom';
export default function ConfirmDialog({
  title = 'Confirm Deletion',
  message,
  onCancel,
  onConfirm,
  busy = false,
  confirmLabel = 'Delete',
  children
}) {
  const ref = useRef(null),
    titleId = useId();
  useEffect(() => {
    const previous = document.activeElement;
    const overflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    ref.current?.querySelector('button')?.focus();
    return () => {
      document.body.style.overflow = overflow;
      previous?.focus();
    };
  }, []);
  function keyDown(e) {
    if (e.key === 'Escape' && !busy) onCancel();
    if (e.key === 'Tab') {
      const buttons = [...ref.current.querySelectorAll('button:not(:disabled),input,select,a[href]')];
      const first = buttons[0],
        last = buttons.at(-1);
      if (e.shiftKey && document.activeElement === first) {
        e.preventDefault();
        last?.focus();
      } else if (!e.shiftKey && document.activeElement === last) {
        e.preventDefault();
        first?.focus();
      }
    }
  }
  return createPortal(<><div className="modal-backdrop fade show" /><div className="modal fade show d-block" role="dialog" aria-modal="true" aria-labelledby={titleId} tabIndex="-1" onKeyDown={keyDown}><div className="modal-dialog modal-dialog-centered" ref={ref}><div className="modal-content"><div className="modal-header"><h2 className="modal-title h5" id={titleId}><i className="bi bi-exclamation-triangle text-danger me-2" />{title}</h2><button className="btn-close" type="button" disabled={busy} aria-label="Close" onClick={onCancel} /></div><div className="modal-body"><p className="mb-0">{message}</p>{children}</div><div className="modal-footer"><button className="btn btn-secondary" type="button" disabled={busy} onClick={onCancel}><T>{"Cancel"}</T></button><button className="btn btn-danger" type="button" disabled={busy} onClick={onConfirm}>{busy ? 'Please wait…' : confirmLabel}</button></div></div></div></div></>, document.body);
}
