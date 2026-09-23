import {useLocation} from "react-router-dom";
import {createContext, useCallback, useContext, useEffect, useMemo, useState} from 'react';
import {optionsApi} from '../api/api';
import {useAuth} from './AuthContext';
const C = createContext(null);
export function WorkspaceProvider({children}) {
  const {user} = useAuth();
  const {pathname} = useLocation();
  const viewingYears = pathname === "/years";
  const [years, setYears] = useState([]);
  const [yearId, setId] = useState(() => localStorage.getItem('charity-working-year') || '');
  const [error, setError] = useState(null);
  const [revision, setRevision] = useState(0);
  const refreshYears = useCallback(() => setRevision(value => value + 1), []);
  const setYearId = useCallback(value => {
    setId(String(value));
    localStorage.setItem('charity-working-year', String(value));
  }, []);
  useEffect(() => {
    if (!user) { setYears([]); return; }
    let active = true;
    optionsApi.years().then(result => {
      if (!active) return;
      const list = Array.isArray(result) ? result : result?.content || [];
      setYears(list);
      setError(null);
      setId(current => {
        const next = list.some(y => String(y.id) === current) ? current : String(list[0]?.id ?? '');
        localStorage.setItem('charity-working-year', next);
        return next;
      });
    }).catch(e => { if (active) setError(e); });
    return () => { active = false; };
  }, [user, viewingYears, revision]);
  useEffect(() => {
    const routeYear = pathname.match(/^\/years\/(\d+)(?:\/|$)/)?.[1];
    if (routeYear && years.some(y => String(y.id) === routeYear)) setYearId(routeYear);
  }, [pathname, years, setYearId]);
  const year = useMemo(() => years.find(y => String(y.id) === yearId) || null, [years, yearId]);
  return <C.Provider value={{years, yearId, setYearId, refreshYears, year, error}}>{children}</C.Provider>;
}
export const useWorkspace = () => useContext(C);
