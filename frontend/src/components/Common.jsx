import {formatNumber} from '../i18n/index.js';
import {ErrorText} from './Localized';
import { T, useTranslate, useLanguage } from "../context/LanguageContext";
import { useState } from "react";
import ConfirmDialog from "./ConfirmDialog";
export const money = (n, symbol = '€', locale = 'en-GB') => `${Number(n || 0).toLocaleString(locale, {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})} ${symbol}`;
export const arr = x => Array.isArray(x) ? x : x?.content || [];
export const pretty = s => String(s ?? '').replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
export function ErrorAlert({
  error
}) {
  const tr = useTranslate();
  if (!error) return null;
  const fields = error?.fields || error?.payload?.fields || {};
  return <div className="alert alert-danger" role="alert"><strong><ErrorText>{error?.message || error?.payload?.message || tr('Something went wrong.')}</ErrorText></strong>{Object.keys(fields).length > 0 && <ul className="react-error-fields">{Object.entries(fields).map(([k, v]) => <li key={k}><ErrorText>{v}</ErrorText></li>)}</ul>}</div>;
}
export function Empty({
  icon = 'bi-inbox',
  title = 'Nothing here yet',
  text,
  children
}) {
  return <div className="empty-state text-center"><i className={`bi ${icon} display-6`}></i><h2 className="h5 mt-3"><T>{title}</T></h2>{text && <p className="text-secondary mb-0"><T>{text}</T></p>}{children && <div className="react-empty-actions">{children}</div>}</div>;
}
export function ConfirmButton({
  className = 'btn btn-sm btn-danger',
  label = 'Delete',
  confirmText = 'Are you sure?',
  onConfirm,
  icon = 'bi-trash'
}) {
  const [open, setOpen] = useState(false),
    [busy, setBusy] = useState(false),
    [error, setError] = useState(null);
  async function confirm() {
    if (busy) return;
    setBusy(true);
    setError(null);
    try {
      await onConfirm?.();
      setOpen(false);
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  return <><button type="button" className={className} onClick={() => setOpen(true)}><i className={`bi ${icon} me-1`} /><T>{label}</T></button>{open && <ConfirmDialog message={confirmText} onCancel={() => setOpen(false)} onConfirm={confirm} busy={busy}><ErrorAlert error={error} /></ConfirmDialog>}</>;
}

export function useMoney(){const {language}=useLanguage();return (n,symbol='€')=>`${formatNumber(language,n || 0,{minimumFractionDigits:2,maximumFractionDigits:2})} ${symbol}`;}
