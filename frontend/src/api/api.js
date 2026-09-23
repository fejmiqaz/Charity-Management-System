export const BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080").replace(/\/$/, "");
let csrf = null;
export class ApiError extends Error { constructor(status,payload){super(payload?.message||`Request failed (${status})`);this.name="ApiError";this.status=status;this.payload=payload;this.fields=payload?.fields||{};} }
async function request(url, options) {
  try { return await fetch(url, options); }
  catch { throw new ApiError(0, {message:'Unable to reach the server. Check your connection and try again.'}); }
}
async function safeJson(response){try{return await response.json()}catch{return null}}
export async function refreshCsrf(){const r=await request(`${BASE_URL}/api/auth/csrf`,{credentials:"include",headers:{Accept:"application/json"}});if(!r.ok)throw new ApiError(r.status,await safeJson(r));csrf=await r.json();return csrf;}
export async function api(path,{method="GET",body,headers={}}={}){const write=!['GET','HEAD','OPTIONS'].includes(method.toUpperCase());if(write&&!csrf)await refreshCsrf();const r=await request(`${BASE_URL}/api${path}`,{method,credentials:"include",headers:{Accept:"application/json",...(body!==undefined?{"Content-Type":"application/json"}:{}),...(write&&csrf?{[csrf.headerName]:csrf.token}:{}),...headers},...(body!==undefined?{body:JSON.stringify(body)}:{})});if(!r.ok)throw new ApiError(r.status,await safeJson(r));if(r.status===204)return undefined;const text=await r.text();if(!text.trim())return undefined;try{return JSON.parse(text)}catch{throw new ApiError(r.status,{message:"The server returned an unexpected response. Please try again."})}}
export const backendUrl=(path)=>`${BASE_URL}${path.startsWith('/')?path:`/${path}`}`;
export async function login(email,password){await refreshCsrf();const x=await api('/auth/login',{method:'POST',body:{email,password}});csrf=null;return x;}
export async function logout(){await api('/auth/logout',{method:'POST'});csrf=null;}
export const authApi={me:()=>api('/auth/me'),login,logout};
export const dashboardApi={get:()=>api('/dashboard')};
export const yearsApi={list:(params='')=>api(`/years${params?`?${params}`:''}`),get:id=>api(`/years/${id}`),create:body=>api('/years',{method:'POST',body}),update:(id,body)=>api(`/years/${id}`,{method:'PUT',body}),remove:id=>api(`/years/${id}`,{method:'DELETE'})};
export const membersApi={list:(params='')=>api(`/members${params?`?${params}`:''}`),get:id=>api(`/members/${id}`),create:body=>api('/members',{method:'POST',body}),update:(id,body)=>api(`/members/${id}`,{method:'PUT',body}),remove:id=>api(`/members/${id}`,{method:'DELETE'})};
export const projectsApi={list:(y,params='')=>api(`/years/${y}/projects${params?`?${params}`:''}`),get:(y,id)=>api(`/years/${y}/projects/${id}`),create:(y,body)=>api(`/years/${y}/projects`,{method:'POST',body}),update:(y,id,body)=>api(`/years/${y}/projects/${id}`,{method:'PUT',body}),remove:(y,id)=>api(`/years/${y}/projects/${id}`,{method:'DELETE'}),revenues:(y,id)=>api(`/years/${y}/projects/${id}/revenues`)};
export const donationsApi={list:y=>api(`/years/${y}/donations`),get:(y,id)=>api(`/years/${y}/donations/${id}`),create:(y,body)=>api(`/years/${y}/donations`,{method:'POST',body}),update:(y,id,body)=>api(`/years/${y}/donations/${id}`,{method:'PUT',body}),remove:(y,id)=>api(`/years/${y}/donations/${id}`,{method:'DELETE'})};
export const eventsApi={list:(y,params='')=>api(`/years/${y}/events${params?`?${params}`:''}`),get:(y,id)=>api(`/years/${y}/events/${id}`),create:(y,body)=>api(`/years/${y}/events`,{method:'POST',body}),update:(y,id,body)=>api(`/years/${y}/events/${id}`,{method:'PUT',body}),remove:(y,id)=>api(`/years/${y}/events/${id}`,{method:'DELETE'}),tasks:(y,e)=>api(`/years/${y}/events/${e}/tasks`)};
export const budgetApi={get:y=>api(`/years/${y}/budget`),create:(y,body)=>api(`/years/${y}/budget`,{method:'POST',body}),update:(y,body)=>api(`/years/${y}/budget`,{method:'PUT',body}),remove:y=>api(`/years/${y}/budget`,{method:'DELETE'})};
export const membershipsApi={list:year=>api(`/memberships?year=${year}`),member:id=>api(`/memberships/members/${id}`),record:body=>api('/memberships/payments',{method:'POST',body}),fee:(year,amount)=>api(`/memberships/${year}/fee`,{method:'PUT',body:{amount}}),void:(id,reason)=>api(`/memberships/payments/${id}/void`,{method:'POST',body:{reason}})};
export const profileApi={get:()=>api('/profile'),update:body=>api('/profile',{method:'PUT',body})};
export const notificationsApi={list:(page=0)=>api(`/notifications?page=${page}`),summary:()=>api('/notifications/summary'),read:id=>api(`/notifications/${id}/read`,{method:'POST'}),readAll:()=>api('/notifications/read-all',{method:'POST'}),clear:()=>api('/notifications',{method:'DELETE'})};
export const optionsApi={years:()=>api('/options/years'),members:()=>api('/options/members'),enums:()=>api('/options/enums')};
