// Read-only synthetic API for manual localization QA. No production data or writes.
import http from 'node:http';
const year={id:1,yearValue:2026},member={id:1,name:'Ada',surname:'Example',email:'ada@example.test',role:'MEMBER',country:'Example country',city:'Example city',phone:'+38970000000'};
const project={id:1,name:'Community garden',description:'User-entered project description',status:'ONGOING',projectType:'REVENUE',dateCreated:'2026-09-01',projectPrice:1250,yearValue:2026,memberNames:['Ada Example'],memberIds:[1]};
const event={id:1,purpose:'Community gathering',date:'2026-10-10T12:00:00',eventType:'TASK_BASED',yearValue:2026,memberNames:['Ada Example'],memberIds:[1]};
const payment={id:1,memberId:1,memberName:'Ada Example',membershipYear:2026,amount:25,currency:'EUR',paidOn:'2026-09-01',paid:true,recordedBy:'Demo treasurer'};
const notification={id:1,title:'Task assigned: Prepare venue',message:'You have been assigned to "Prepare venue" for Community gathering.\nEvent: 10 Oct 2026, 12:00 (Europe/Skopje)\nTask details: User-entered task description',createdAt:'2026-09-25T10:00:00Z',unread:true,eventPath:'/years/1/events/1'};
const page=content=>({content,page:0,totalPages:2,totalElements:content.length+1});
const fixtures={
 '/auth/me':{...member,role:'HEAD'},'/options/years':[year],'/options/members':[member],
 '/options/enums':{roles:['HEAD','SUBHEAD','TREASURER','PROJECT_MANAGER','EVENT_MANAGER','VOLUNTEER','MEMBER'],projectStatuses:['PLANNED','ONGOING','FINISHED','CANCELLED'],projectTypes:['STANDARD','REVENUE'],eventTypes:['NORMAL','TASK_BASED'],eventStatuses:['UPCOMING','PAST','UNSCHEDULED']},
 '/dashboard':{totalMembers:2,totalProjects:3,totalDonationsCount:2,totalYears:2,remainingBudget:5000,donationTotals:{EUR:125},membershipIncome:25,projectIncome:500,totalProjectCost:1250,budgetWarnings:[{year:2026,level:'warning',percentage:80,remaining:250}]},
 '/notifications/summary':{unread:1,items:[notification]},'/notifications':page([notification]),
 '/years':page([year]),'/years/1':year,
 '/years/1/projects':[project],'/years/1/projects/1':project,'/years/1/projects/summary':{totalProjectCost:1250,budgetAmount:5000},
 '/years/1/events':[event],'/years/1/events/1':event,
 '/years/1/events/1/tasks':[{id:1,title:'Prepare venue',description:'User-entered task description',price:200,completed:false,members:[member],paymentTotals:{EUR:25},payments:[payment]}],
 '/years/1/projects/1/revenues':[{id:1,revenueMonth:'2026-09',customer:'Example customer',amount:150,currency:'EUR',note:'User note'}],
 '/years/1/donations':[{id:1,donationAmount:50,currency:'EUR',memberNames:['Ada Example']}],
 '/members':page([member]),'/members/1':member,
 '/memberships/overview':{fee:25,totals:{EUR:25},payments:[payment],members:[member]},
 '/memberships/status':{fee:25,paidMemberships:{1:payment}},
 '/public/home':{completedProjects:1,impactYears:1,projects:[{...project,status:'FINISHED'}],events:[event],publicZone:'Europe/Skopje',currentYear:2026,projectChart:[{label:'2026',width:100,count:1}],eventChart:[{label:'Oct 2026',width:100,count:1}]}
};
http.createServer((req,res)=>{
 res.setHeader('Access-Control-Allow-Origin','http://127.0.0.1:5174');res.setHeader('Access-Control-Allow-Credentials','true');res.setHeader('Content-Type','application/json');
 if(req.method!=='GET'){res.writeHead(405);res.end(JSON.stringify({message:'Read-only localization fixture'}));return;}
 const key=new URL(req.url,'http://localhost').pathname.replace(/^\/api/,'');
 if(!Object.hasOwn(fixtures,key)){res.writeHead(404);res.end(JSON.stringify({message:'Resource not found.'}));return;}
 res.end(JSON.stringify(fixtures[key]));
}).listen(18080,'127.0.0.1',()=>console.log('Read-only i18n fixtures: http://127.0.0.1:18080'));
