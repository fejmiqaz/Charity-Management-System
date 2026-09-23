import {createContext,useContext,useState} from 'react';
import translations from '../i18n/translations';
const Context=createContext({language:'en',setLanguage:()=>{}});
export function LanguageProvider({children}){const [language,setValue]=useState(()=>localStorage.getItem('charity-language')||'en');function setLanguage(next){localStorage.setItem('charity-language',next);document.documentElement.lang=next;setValue(next)}return <Context.Provider value={{language,setLanguage}}>{children}</Context.Provider>}
export const useLanguage=()=>useContext(Context);
export function T({children}){const {language}=useLanguage();return typeof children==='string'?(translations[language]?.[children.trim()]||children):children}
