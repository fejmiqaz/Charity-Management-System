import {createContext,useContext,useEffect,useState} from 'react';
import {authApi} from '../api/api';
const C=createContext(null);
export function AuthProvider({children}){
  const [user,setUser]=useState(null),[loading,setLoading]=useState(true);
  async function reload(){const next=await authApi.me();setUser(next);return next}
  useEffect(()=>{let active=true;authApi.me().then(next=>{if(active)setUser(next)}).catch(()=>{if(active)setUser(null)}).finally(()=>{if(active)setLoading(false)});return()=>{active=false}},[]);
  async function signIn(email,password){const next=await authApi.login(email,password);setUser(next)}
  async function signOut(){await authApi.logout();setUser(null)}
  return <C.Provider value={{user,loading,signIn,signOut,reload,clearSession:()=>setUser(null)}}>{children}</C.Provider>;
}
export const useAuth=()=>useContext(C);
