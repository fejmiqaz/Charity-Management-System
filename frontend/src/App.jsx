import Home from './pages/home';
import {LanguageProvider} from "./context/LanguageContext";
import {BrowserRouter,Navigate,Route,Routes} from 'react-router-dom';
import {AuthProvider} from './context/AuthContext';
import {WorkspaceProvider} from './context/WorkspaceContext';
import ProtectedRoute from './components/ProtectedRoute';
import MainLayout from './layouts/MainLayout';
import Login from './pages/auth/login';
import Register from './pages/auth/register';
import Dashboard from './pages/dashboard/index';
import Years from './pages/years/list';
import YearForm from './pages/years/form';
import YearDetails from './pages/years/details';
import Members from './pages/members/list';
import MemberForm from './pages/members/form';
import MemberDetails from './pages/members/details';
import Projects from './pages/projects/list';
import ProjectForm from './pages/projects/form';
import ProjectDetails from './pages/projects/details';
import Donations from './pages/donations/list';
import DonationForm from './pages/donations/form';
import DonationDetails from './pages/donations/details';
import Events from './pages/events/list';
import EventForm from './pages/events/form';
import EventDetails from './pages/events/details';
import Budget from './pages/budgets/list';
import BudgetForm from './pages/budgets/form';
import BudgetDetails from './pages/budgets/details';
import Memberships from './pages/memberships/index';
import Profile from './pages/profile/details';
import ProfileForm from './pages/profile/form';
import Notifications from './pages/notifications/list';
import Converter from './pages/converter/index';
export default function App(){return <BrowserRouter><LanguageProvider><AuthProvider><WorkspaceProvider><Routes>
 <Route path="/" element={<Home/>}/><Route path="/login" element={<Login/>}/><Route path="/register" element={<Register/>}/>
 <Route element={<ProtectedRoute/>}><Route element={<MainLayout/>}>
 <Route path="/dashboard" element={<Dashboard/>}/>
 <Route path="/years" element={<Years/>}/><Route path="/years/new" element={<YearForm/>}/><Route path="/years/add-form" element={<YearForm/>}/><Route path="/years/:id/edit" element={<YearForm/>}/><Route path="/years/edit-form/:id" element={<YearForm/>}/><Route path="/years/:id" element={<YearDetails/>}/>
 <Route path="/members" element={<Members/>}/><Route path="/members/new" element={<MemberForm/>}/><Route path="/members/add-form" element={<MemberForm/>}/><Route path="/members/:id" element={<MemberDetails/>}/><Route path="/members/:id/edit" element={<MemberForm/>}/><Route path="/memberships" element={<Memberships/>}/>
 <Route path="/years/:yearId/projects" element={<Projects/>}/><Route path="/years/:yearId/projects/add" element={<ProjectForm/>}/><Route path="/years/:yearId/projects/add-form" element={<ProjectForm/>}/><Route path="/years/:yearId/projects/:id" element={<ProjectDetails/>}/><Route path="/years/:yearId/projects/:id/edit" element={<ProjectForm/>}/>
 <Route path="/years/:yearId/donations" element={<Donations/>}/><Route path="/years/:yearId/donations/add" element={<DonationForm/>}/><Route path="/years/:yearId/donations/:id" element={<DonationDetails/>}/><Route path="/years/:yearId/donations/:id/edit" element={<DonationForm/>}/>
 <Route path="/years/:yearId/events" element={<Events/>}/><Route path="/years/:yearId/events/add" element={<EventForm/>}/><Route path="/years/:yearId/events/add-form" element={<EventForm/>}/><Route path="/years/:yearId/events/:id" element={<EventDetails/>}/><Route path="/years/:yearId/events/:id/edit" element={<EventForm/>}/><Route path="/years/:yearId/events/:id/edit-form" element={<EventForm/>}/>
 <Route path="/years/:yearId/budget" element={<Budget/>}/><Route path="/years/:yearId/budget/add" element={<BudgetForm/>}/><Route path="/years/:yearId/budget/add-form" element={<BudgetForm/>}/><Route path="/years/:yearId/budget/edit" element={<BudgetForm/>}/><Route path="/years/:yearId/budget/edit-form/:id" element={<BudgetForm/>}/><Route path="/years/:yearId/budget/details/:id" element={<BudgetDetails/>}/>
 <Route path="/profile" element={<Profile/>}/><Route path="/profile/edit" element={<ProfileForm/>}/><Route path="/notifications" element={<Notifications/>}/><Route path="/converter" element={<Converter/>}/>
 </Route></Route><Route path="*" element={<Navigate to="/dashboard" replace/>}/>
 </Routes></WorkspaceProvider></AuthProvider></LanguageProvider></BrowserRouter>}

