import {LocalizedDate} from '../../components/Localized';
import { T, useTranslate } from "../../context/LanguageContext";
import { useParams } from 'react-router-dom';
import { projectsApi, backendUrl } from '../../api/api';
import useResource from '../../hooks/useResource';
import { Back, Rows, MemberBadges, ResourceState, Role } from '../../components/Template';
import Publication from '../../components/Publication';
import Revenues from './revenues';
export default function ProjectDetails() {
  const tr = useTranslate();
  const {
      yearId,
      id
    } = useParams(),
    r = useResource(() => projectsApi.get(yearId, id), [yearId, id]),
    x = r.data;
  return <ResourceState {...r}>{x && <div className="container py-4" style={{
      maxWidth: 900
    }}><h1 className="mb-3"><T>{"Project Details"}</T></h1><nav className="detail-toolbar mb-4" aria-label={tr("Project actions")}><Back to={`/years/${yearId}/projects`}><T>{"Back to Projects"}</T></Back><Role roles={['HEAD', 'SUBHEAD', 'PROJECT_MANAGER']}><a className="btn btn-danger" href={backendUrl(`/pdf/years/${yearId}/projects/${id}`)}><i className="bi bi-file-earmark-pdf me-1" /><T>{"Project Report PDF"}</T></a></Role></nav><div className="card detail-hero text-center p-4 mb-4"><i className="bi bi-kanban display-4 mb-2" /><span className="display-serif" style={{
          fontSize: 36
        }}>{x.name}</span></div><div className="card p-4 mb-4"><h2 className="section-title"><T>{"Project Information"}</T></h2><Rows items={[["ID", x.id], ["Name", x.name], ["Project type", x.projectType === 'REVENUE' ? 'Revenue-generating project' : 'Standard charity project'], ["Description", x.description], ["Status", <span className={`badge rounded-pill badge-${['FINISHED', 'CANCELLED', 'ONGOING'].includes(x.status) ? x.status.toLowerCase() : 'pending'}`}><T>{x.status}</T></span>], ['Created Date', <LocalizedDate value={x.dateCreated} />], ['Cost', `${x.projectPrice} €`], ['Year', x.yearValue ?? <T>No year assigned</T>]]} /></div><div className="card p-4"><h2 className="section-title"><T>{"Project Members"}</T></h2><MemberBadges names={x.memberNames} empty="No members have been assigned to this project." /></div>{x.projectType === 'REVENUE' && <Revenues yearId={yearId} id={id} />}<Role roles={['HEAD']}><Publication yearId={yearId} id={id} type="projects" initial={x.publicImpact} /></Role></div>}</ResourceState>;
}
