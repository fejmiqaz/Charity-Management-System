import {useState} from "react";
import ConfirmDialog from "./ConfirmDialog";
export const money=(n,symbol='€')=>`${Number(n||0).toLocaleString(undefined,{minimumFractionDigits:2,maximumFractionDigits:2})} ${symbol}`;
export const arr=x=>Array.isArray(x)?x:(x?.content||[]);
export const pretty=s=>String(s??'').replaceAll('_',' ').toLowerCase().replace(/\b\w/g,c=>c.toUpperCase());
export function ErrorAlert({error}){if(!error)return null;const fields=error?.fields||error?.payload?.fields||{};return <div className="alert alert-danger" role="alert"><strong>{error?.message||error?.payload?.message||'Something went wrong.'}</strong>{Object.keys(fields).length>0&&<ul className="react-error-fields">{Object.entries(fields).map(([k,v])=><li key={k}>{v}</li>)}</ul>}</div>}
export function Empty({icon='bi-inbox',title='Nothing here yet',text,children}){return <div className="empty-state text-center"><i className={`bi ${icon} display-6`}></i><h2 className="h5 mt-3">{title}</h2>{text&&<p className="text-secondary mb-0">{text}</p>}{children&&<div className="react-empty-actions">{children}</div>}</div>}
export function ConfirmButton({className='btn btn-sm btn-danger',label='Delete',confirmText='Are you sure?',onConfirm,icon='bi-trash'}){
 const [open,setOpen]=useState(false),[busy,setBusy]=useState(false),[error,setError]=useState(null);
 async function confirm(){if(busy)return;setBusy(true);setError(null);try{await onConfirm?.();setOpen(false)}catch(e){setError(e)}finally{setBusy(false)}}
 return <><button type="button" className={className} onClick={()=>setOpen(true)}><i className={`bi ${icon} me-1`}/>{label}</button>{open&&<ConfirmDialog message={confirmText} onCancel={()=>setOpen(false)} onConfirm={confirm} busy={busy}><ErrorAlert error={error}/></ConfirmDialog>}</>
}
