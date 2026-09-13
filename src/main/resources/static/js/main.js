// =========================================================
// Charity Organization – shared front-end behaviour
// =========================================================

document.addEventListener("DOMContentLoaded", function () {
  initDeleteConfirmation();
  initFadeIn();
});

/**
 * Every delete <form> has a trigger button with class "co-delete-trigger"
 * (instead of submitting straight away). Clicking it opens the shared
 * confirmation modal (#coConfirmDeleteModal); confirming submits the
 * original form, so every existing th:action target keeps working
 * completely unchanged.
 *
 * Optional: put a data-item-label="..." on the trigger button to show
 * the item's name in the confirmation text.
 */
function initDeleteConfirmation() {
  const modalEl = document.getElementById("coConfirmDeleteModal");
  if (!modalEl || typeof bootstrap === "undefined") return;

  const modal = new bootstrap.Modal(modalEl);
  const confirmBtn = document.getElementById("coConfirmDeleteBtn");
  const modalText = document.getElementById("coConfirmDeleteText");
  let pendingForm = null;

  document.querySelectorAll(".co-delete-trigger").forEach(function (btn) {
    btn.addEventListener("click", function (e) {
      e.preventDefault();
      pendingForm = btn.closest("form");
      const label = btn.getAttribute("data-item-label");
      modalText.textContent = label
        ? 'Are you sure you want to delete "' + label + '"?'
        : "Are you sure you want to delete this item?";
      modal.show();
    });
  });

  confirmBtn.addEventListener("click", function () {
    if (pendingForm) {
      pendingForm.submit();
    }
    modal.hide();
  });
}

/** Small entrance animation for cards / tables as they load. */
function initFadeIn() {
  document.querySelectorAll(".card, .table-branded").forEach(function (el) {
    el.classList.add("fade-in");
  });
}

// Navigation keeps the selected year when moving between sections.
document.addEventListener('DOMContentLoaded', () => {
  const sidebar = document.getElementById('appSidebar');
  if (!sidebar) return;
  const path = window.location.pathname;
  const section = ['projects','donations','events','budget'].find(name => path.includes('/' + name));
  const active = section || (path.includes('/profile') ? 'profile' : path.includes('/memberships') ? 'memberships' : path.includes('/members') ? 'members' : path.includes('/years') ? 'years' : 'dashboard');
  sidebar.querySelectorAll('[data-nav]').forEach(link => {
    if (link.dataset.nav === active) { link.classList.add('active'); link.setAttribute('aria-current','page'); }
  });
  const year = document.getElementById('workspaceYear');
  if (year) year.addEventListener('change', () => {
    const fallback = sidebar.querySelector('[data-nav="projects"], [data-nav="donations"], [data-nav="events"], [data-nav="budget"]');
    const targetSection = section || (sidebar.querySelector('[data-nav="years"]') ? '' : fallback?.dataset.nav);
    window.location.assign(year.value + (targetSection ? '/' + targetSection : ''));
  });
  const menu = document.querySelector('.mobile-menu');
  const mobile = window.matchMedia('(max-width: 767px)');
  const syncSidebar = () => { sidebar.inert = mobile.matches && !document.body.classList.contains('nav-open'); };
  const closeMenu = () => {document.body.classList.remove('nav-open'); menu?.setAttribute('aria-expanded','false');syncSidebar();};
  mobile.addEventListener('change', closeMenu);
  syncSidebar();
  menu?.addEventListener('click', () => {const open = document.body.classList.toggle('nav-open'); menu.setAttribute('aria-expanded',String(open));syncSidebar(); if(open) sidebar.querySelector('a')?.focus();});
  sidebar.addEventListener('keydown', event => {
    if(event.key !== 'Tab' || !mobile.matches || !document.body.classList.contains('nav-open')) return;
    const controls = Array.from(sidebar.querySelectorAll('a,button,select')).filter(element => !element.disabled);
    if(event.shiftKey && document.activeElement === controls[0]) {event.preventDefault();controls.at(-1)?.focus();}
    else if(!event.shiftKey && document.activeElement === controls.at(-1)) {event.preventDefault();controls[0]?.focus();}
  });
  document.addEventListener('click', event => {if(!sidebar.contains(event.target) && !menu?.contains(event.target)) closeMenu();});
  document.addEventListener('keydown', event => {if(event.key === 'Escape' && document.body.classList.contains('nav-open')) {closeMenu();menu?.focus();}});
  const dialog = document.getElementById('pageFinder');
  const input = document.getElementById('finderInput');
  const results = document.getElementById('finderResults');
  if(dialog && input && results) {
    sidebar.querySelectorAll('[data-nav]').forEach(link => {const copy = link.cloneNode(true);copy.removeAttribute('aria-current');copy.className='';results.appendChild(copy);});
    input.addEventListener('input', () => {let count=0;results.querySelectorAll('a').forEach(link=>{link.hidden=!link.textContent.toLowerCase().includes(input.value.toLowerCase());if(!link.hidden)count++;});document.getElementById('finderEmpty').hidden=count>0;});
    const open = () => {if(!dialog.open)dialog.showModal();input.focus();};
    document.querySelector('.jump-trigger')?.addEventListener('click',open);
    document.getElementById('closeFinder')?.addEventListener('click',()=>dialog.close());
    document.addEventListener('keydown',event=>{if((event.ctrlKey||event.metaKey)&&event.key.toLowerCase()==='k'){event.preventDefault();open();}});
    input.addEventListener('keydown',event=>{if(event.key==='ArrowDown'){event.preventDefault();results.querySelector('a:not([hidden])')?.focus();}if(event.key==='Enter'){event.preventDefault();results.querySelector('a:not([hidden])')?.click();}});
  }
  // Keep the original select as the submitted source of truth.
  document.querySelectorAll('select[multiple]').forEach(select => {
    const wrapper=document.createElement('div');wrapper.className='member-picker';
    const search=document.createElement('input');search.type='search';search.className='form-control';search.placeholder='Find a member...';search.setAttribute('aria-label','Filter members');
    const list=document.createElement('div');list.className='member-picker-list';
    const count=document.createElement('div');count.className='member-picker-count';count.setAttribute('aria-live','polite');
    const update=()=>{count.textContent=Array.from(select.selectedOptions).length+' selected';};
    Array.from(select.options).forEach(option=>{const label=document.createElement('label');const checkbox=document.createElement('input');checkbox.type='checkbox';checkbox.checked=option.selected;checkbox.disabled=option.disabled||select.disabled;checkbox.addEventListener('change',()=>{option.selected=checkbox.checked;select.dispatchEvent(new Event('change',{bubbles:true}));update();});label.append(checkbox,document.createTextNode(option.textContent));list.appendChild(label);});
    search.addEventListener('input',()=>list.querySelectorAll('label').forEach(label=>label.hidden=!label.textContent.toLowerCase().includes(search.value.toLowerCase())));
    wrapper.append(search,list,count);select.after(wrapper);select.hidden=true;
    const label=document.querySelector('label[for="'+select.id+'"]');search.id=select.id+'-search';if(label)label.htmlFor=search.id;
    const help=wrapper.nextElementSibling;if(help?.classList.contains('form-text'))help.textContent='Select each member you want to include.';
    update();
  });
});
