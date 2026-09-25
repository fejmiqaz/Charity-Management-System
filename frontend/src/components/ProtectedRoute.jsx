import { T } from "../context/LanguageContext";
import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
export default function ProtectedRoute() {
  const {
    user,
    loading
  } = useAuth();
  if (loading) return <div className="d-flex min-vh-100 align-items-center justify-content-center"><T>{"Loading…"}</T></div>;
  return user ? <Outlet /> : <Navigate to="/login" replace />;
}
