import { T } from "../context/LanguageContext";
import { Outlet } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Header from "../components/Header";
import useBodyClass from "../hooks/useBodyClass";
export default function MainLayout() {
  useBodyClass('workspace');
  return <><Sidebar /><a className="skip-link" href="#mainContent"><T>{"Skip to content"}</T></a><div className="workspace-shell"><Header /><main id="mainContent" tabIndex="-1"><Outlet /></main><footer className="workspace-footer"><span><T>{"Charity Management"}</T></span><span><T>{"Built around your community."}</T></span></footer></div></>;
}
