import {api} from './api';
const root=(y,type,id)=>`/years/${y}/${type}/${id}`;
export const activityApi={
 createTask:(y,e,body)=>api(`${root(y,'events',e)}/tasks`,{method:'POST',body}),
 taskStatus:(y,e,t,completed)=>api(`${root(y,'events',e)}/tasks/${t}/status`,{method:'PATCH',body:{completed}}),
 deleteTask:(y,e,t)=>api(`${root(y,'events',e)}/tasks/${t}`,{method:'DELETE'}),
 payment:(y,e,t,body)=>api(`${root(y,'events',e)}/tasks/${t}/payments`,{method:'POST',body}),
 revenue:(y,p,body)=>api(`${root(y,'projects',p)}/revenues`,{method:'POST',body}),
 publication:(y,type,id,published)=>api(`${root(y,type,id)}/publication`,{method:'PUT',body:{published}}),
};
export const templateApi={year:id=>api(`/years/${id}/overview`),profile:()=>api('/profile/overview'),membershipStatus:year=>api(`/memberships/status?year=${year}`)};
