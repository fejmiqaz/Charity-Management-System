import {createContext, useCallback, useContext, useEffect, useState} from 'react';
import {translate, locales, normalizeLanguage} from '../i18n/index.js';
const Context = createContext({language: 'en', setLanguage: () => {}});
export function LanguageProvider({children}) {
  const [language, setValue] = useState(() => normalizeLanguage(localStorage.getItem('charity-language')));
  function setLanguage(next) {
    const value = normalizeLanguage(next);
    localStorage.setItem('charity-language', value);
    document.documentElement.lang = value;
    setValue(value);
  }
  useEffect(() => { document.documentElement.lang = language; }, [language]);
  return <Context.Provider value={{language, setLanguage, locale: locales[language]}}>{children}</Context.Provider>;
}
export const useLanguage = () => useContext(Context);
export function useTranslate() {
  const {language} = useLanguage();
  return useCallback((text, values) => translate(language, text, values), [language]);
}
export function T({children, values}) {
  const tr = useTranslate();
  return typeof children === 'string' ? tr(children, values) : children;
}
