// =========================================================
// Charity Organization – shared front-end behaviour
// =========================================================

document.addEventListener("DOMContentLoaded", function () {
  initThemeAndLanguage();
  initDeleteConfirmation();
  initFadeIn();
});

const charityTranslations = {
  sq: {
    "Charity Management":"Menaxhimi i Bamirësisë","Overview":"Përmbledhje","Members":"Anëtarët","Yearly records":"Regjistrimet vjetore","Memberships":"Anëtarësimet","Projects":"Projektet","Donations":"Donacionet","Events":"Ngjarjet","Budget":"Buxheti","WORKSPACE":"HAPËSIRA E PUNËS","WORKING YEAR":"VITI AKTIV","View profile":"Shiko profilin","Sign out":"Dil","Find a page":"Gjej një faqe","Close":"Mbyll","Confirm Deletion":"Konfirmo fshirjen","Cancel":"Anulo","Delete":"Fshi","Good work starts here.":"Puna e mirë fillon këtu.","Organization at a glance":"Organizata në një vështrim","Across all years":"Në të gjitha vitet","Members":"Anëtarët","Projects":"Projektet","Donations":"Donacionet","Yearly records":"Regjistrimet vjetore","Financial overview":"Përmbledhje financiare","Remaining budget":"Buxheti i mbetur","Total donations":"Donacionet totale","Membership income":"Të ardhurat nga anëtarësimi","Project costs":"Shpenzimet e projekteve","Pick up from here":"Vazhdo nga këtu","Manage projects":"Menaxho projektet","View donations":"Shiko donacionet","Explore events":"Shfleto ngjarjet","Find a member":"Gjej një anëtar","Actions":"Veprimet","Amount":"Shuma","Description":"Përshkrimi","Year":"Viti","Status":"Statusi","Name":"Emri","First Name":"Emri","Last Name":"Mbiemri","Email":"Email","Country":"Shteti","City":"Qyteti","Phone Number":"Numri i telefonit","Role":"Roli","Save":"Ruaj","Edit":"Ndrysho","Details":"Detajet","Search":"Kërko","Export Excel":"Eksporto Excel","No content available.":"Nuk ka përmbajtje.","My profile":"Profili im","Edit my information":"Ndrysho të dhënat e mia","Access Denied":"Qasja u refuzua","Back to overview":"Kthehu te përmbledhja","Sign In":"Hyr","Password":"Fjalëkalimi","Create an account":"Krijo llogari","Explore our charity":"Shfleto organizatën tonë"
  },
  fr: {
    "Charity Management":"Gestion de l'association","Overview":"Vue d'ensemble","Members":"Membres","Yearly records":"Dossiers annuels","Memberships":"Adhésions","Projects":"Projets","Donations":"Dons","Events":"Événements","Budget":"Budget","WORKSPACE":"ESPACE DE TRAVAIL","WORKING YEAR":"ANNÉE ACTIVE","View profile":"Voir le profil","Sign out":"Se déconnecter","Find a page":"Rechercher une page","Close":"Fermer","Confirm Deletion":"Confirmer la suppression","Cancel":"Annuler","Delete":"Supprimer","Good work starts here.":"Le bon travail commence ici.","Organization at a glance":"L'organisation en un coup d'œil","Across all years":"Toutes années confondues","Financial overview":"Aperçu financier","Remaining budget":"Budget restant","Total donations":"Total des dons","Membership income":"Revenus des adhésions","Project costs":"Coûts des projets","Pick up from here":"Continuer ici","Manage projects":"Gérer les projets","View donations":"Voir les dons","Explore events":"Explorer les événements","Find a member":"Trouver un membre","Actions":"Actions","Amount":"Montant","Description":"Description","Year":"Année","Status":"Statut","Name":"Nom","First Name":"Prénom","Last Name":"Nom de famille","Email":"E-mail","Country":"Pays","City":"Ville","Phone Number":"Téléphone","Role":"Rôle","Save":"Enregistrer","Edit":"Modifier","Details":"Détails","Search":"Rechercher","Export Excel":"Exporter vers Excel","No content available.":"Aucun contenu disponible.","My profile":"Mon profil","Edit my information":"Modifier mes informations","Access Denied":"Accès refusé","Back to overview":"Retour à la vue d'ensemble","Sign In":"Se connecter","Password":"Mot de passe","Create an account":"Créer un compte","Explore our charity":"Découvrir notre association"
  },
  de: {
    "Charity Management":"Wohltätigkeitsverwaltung","Overview":"Übersicht","Members":"Mitglieder","Yearly records":"Jahresdaten","Memberships":"Mitgliedschaften","Projects":"Projekte","Donations":"Spenden","Events":"Veranstaltungen","Budget":"Budget","WORKSPACE":"ARBEITSBEREICH","WORKING YEAR":"AKTIVES JAHR","View profile":"Profil ansehen","Sign out":"Abmelden","Find a page":"Seite suchen","Close":"Schließen","Confirm Deletion":"Löschen bestätigen","Cancel":"Abbrechen","Delete":"Löschen","Good work starts here.":"Gute Arbeit beginnt hier.","Organization at a glance":"Organisation im Überblick","Across all years":"Über alle Jahre","Financial overview":"Finanzübersicht","Remaining budget":"Verbleibendes Budget","Total donations":"Spenden insgesamt","Membership income":"Mitgliedsbeiträge","Project costs":"Projektkosten","Pick up from here":"Hier weitermachen","Manage projects":"Projekte verwalten","View donations":"Spenden anzeigen","Explore events":"Veranstaltungen ansehen","Find a member":"Mitglied finden","Actions":"Aktionen","Amount":"Betrag","Description":"Beschreibung","Year":"Jahr","Status":"Status","Name":"Name","First Name":"Vorname","Last Name":"Nachname","Email":"E-Mail","Country":"Land","City":"Stadt","Phone Number":"Telefonnummer","Role":"Rolle","Save":"Speichern","Edit":"Bearbeiten","Details":"Details","Search":"Suchen","Export Excel":"Excel exportieren","No content available.":"Kein Inhalt verfügbar.","My profile":"Mein Profil","Edit my information":"Meine Daten bearbeiten","Access Denied":"Zugriff verweigert","Back to overview":"Zurück zur Übersicht","Sign In":"Anmelden","Password":"Passwort","Create an account":"Konto erstellen","Explore our charity":"Unsere Organisation entdecken"
  }
};

Object.assign(charityTranslations.sq, {"Profile": "Profili"});
Object.assign(charityTranslations.fr, {"Profile": "Profil"});
Object.assign(charityTranslations.de, {"Profile": "Profil"});

Object.assign(charityTranslations.sq, {
  "THE BIG PICTURE":"PAMJA E PËRGJITHSHME","Your people, projects, and purpose. All in one place.":"Njerëzit, projektet dhe qëllimi juaj. Të gjitha në një vend.","People in your community":"Njerëzit në komunitetin tuaj","Ideas put into action":"Idetë e kthyer në veprim","Contributions recorded":"Kontributet e regjistruara","Your impact over time":"Ndikimi juaj ndër vite","Yearly budget alerts":"Njoftimet e buxhetit vjetor","Project costs against allocated budget":"Shpenzimet e projekteve kundrejt buxhetit","Manage this year's events and participants":"Menaxho ngjarjet dhe pjesëmarrësit e këtij viti","Manage the year's budget":"Menaxho buxhetin e vitit","No events found for this year.":"Nuk u gjetën ngjarje për këtë vit.","No donations were found for this year.":"Nuk u gjetën donacione për këtë vit.","No budget was found for this year.":"Nuk u gjet buxhet për këtë vit.","Member Details":"Detajet e anëtarit","Donation Details":"Detajet e donacionit","Event Details":"Detajet e ngjarjes","Project Details":"Detajet e projektit","Budget Details":"Detajet e buxhetit","Personal Information":"Të dhënat personale","Contact Information":"Të dhënat e kontaktit","Select a role":"Zgjidh një rol","All Members":"Të gjithë anëtarët","All Roles":"Të gjitha rolet","Membership":"Anëtarësimi","Payment history":"Historia e pagesave","Purpose":"Qëllimi","Date":"Data","Date and Time":"Data dhe ora","Total Donation Amount":"Shuma totale e donacioneve","Save publication setting":"Ruaj cilësimin e publikimit"
});
Object.assign(charityTranslations.fr, {
  "THE BIG PICTURE":"VUE D'ENSEMBLE","Your people, projects, and purpose. All in one place.":"Vos équipes, vos projets et votre mission. Tout au même endroit.","People in your community":"Personnes dans votre communauté","Ideas put into action":"Idées mises en action","Contributions recorded":"Contributions enregistrées","Your impact over time":"Votre impact au fil du temps","Yearly budget alerts":"Alertes budgétaires annuelles","Project costs against allocated budget":"Coûts des projets par rapport au budget alloué","Manage this year's events and participants":"Gérer les événements et participants de cette année","Manage the year's budget":"Gérer le budget de l'année","No events found for this year.":"Aucun événement trouvé pour cette année.","No donations were found for this year.":"Aucun don trouvé pour cette année.","No budget was found for this year.":"Aucun budget trouvé pour cette année.","Member Details":"Détails du membre","Donation Details":"Détails du don","Event Details":"Détails de l'événement","Project Details":"Détails du projet","Budget Details":"Détails du budget","Personal Information":"Informations personnelles","Contact Information":"Coordonnées","Select a role":"Sélectionner un rôle","All Members":"Tous les membres","All Roles":"Tous les rôles","Membership":"Adhésion","Payment history":"Historique des paiements","Purpose":"Objet","Date":"Date","Date and Time":"Date et heure","Total Donation Amount":"Montant total des dons","Save publication setting":"Enregistrer le paramètre de publication"
});
Object.assign(charityTranslations.de, {
  "THE BIG PICTURE":"DAS GESAMTBILD","Your people, projects, and purpose. All in one place.":"Ihre Menschen, Projekte und Ziele. Alles an einem Ort.","People in your community":"Menschen in Ihrer Gemeinschaft","Ideas put into action":"Umgesetzte Ideen","Contributions recorded":"Erfasste Beiträge","Your impact over time":"Ihre Wirkung im Zeitverlauf","Yearly budget alerts":"Jährliche Budgetwarnungen","Project costs against allocated budget":"Projektkosten im Verhältnis zum Budget","Manage this year's events and participants":"Veranstaltungen und Teilnehmer dieses Jahres verwalten","Manage the year's budget":"Jahresbudget verwalten","No events found for this year.":"Für dieses Jahr wurden keine Veranstaltungen gefunden.","No donations were found for this year.":"Für dieses Jahr wurden keine Spenden gefunden.","No budget was found for this year.":"Für dieses Jahr wurde kein Budget gefunden.","Member Details":"Mitgliedsdetails","Donation Details":"Spendendetails","Event Details":"Veranstaltungsdetails","Project Details":"Projektdetails","Budget Details":"Budgetdetails","Personal Information":"Persönliche Angaben","Contact Information":"Kontaktinformationen","Select a role":"Rolle auswählen","All Members":"Alle Mitglieder","All Roles":"Alle Rollen","Membership":"Mitgliedschaft","Payment history":"Zahlungsverlauf","Purpose":"Zweck","Date":"Datum","Date and Time":"Datum und Uhrzeit","Total Donation Amount":"Gesamtsumme der Spenden","Save publication setting":"Veröffentlichungseinstellung speichern"
});

const actionTranslations = {
  sq:{"Back to Years":"Kthehu te vitet","Year Details":"Detajet e vitit","Export All Projects PDF":"Eksporto projektet në PDF","Export All Donations PDF":"Eksporto donacionet në PDF","Export All Events PDF":"Eksporto ngjarjet në PDF","Add Project":"Shto projekt","Add Donation":"Shto donacion","Add Event":"Shto ngjarje","Projects for Year":"Projektet për vitin","Donations for Year":"Donacionet për vitin","Events for Year":"Ngjarjet për vitin","Budget for Year":"Buxheti për vitin","Event type":"Lloji i ngjarjes","Normal event":"Ngjarje normale","Event with tasks":"Ngjarje me detyra","Project type":"Lloji i projektit","Standard charity project":"Projekt standard bamirësie","Revenue-generating project":"Projekt që gjeneron të ardhura","Monthly project income":"Të ardhurat mujore të projektit","Event tasks":"Detyrat e ngjarjes","Task title":"Titulli i detyrës","Assigned members":"Anëtarët e caktuar","Record a payment":"Regjistro pagesë","Record income":"Regjistro të ardhura"},
  fr:{"Back to Years":"Retour aux années","Year Details":"Détails de l'année","Export All Projects PDF":"Exporter les projets en PDF","Export All Donations PDF":"Exporter les dons en PDF","Export All Events PDF":"Exporter les événements en PDF","Add Project":"Ajouter un projet","Add Donation":"Ajouter un don","Add Event":"Ajouter un événement","Projects for Year":"Projets de l'année","Donations for Year":"Dons de l'année","Events for Year":"Événements de l'année","Budget for Year":"Budget de l'année","Event type":"Type d'événement","Normal event":"Événement normal","Event with tasks":"Événement avec tâches","Project type":"Type de projet","Standard charity project":"Projet caritatif standard","Revenue-generating project":"Projet générateur de revenus","Monthly project income":"Revenus mensuels du projet","Event tasks":"Tâches de l'événement","Task title":"Titre de la tâche","Assigned members":"Membres assignés","Record a payment":"Enregistrer un paiement","Record income":"Enregistrer un revenu"},
  de:{"Back to Years":"Zurück zu Jahren","Year Details":"Jahresdetails","Export All Projects PDF":"Alle Projekte als PDF exportieren","Export All Donations PDF":"Alle Spenden als PDF exportieren","Export All Events PDF":"Alle Veranstaltungen als PDF exportieren","Add Project":"Projekt hinzufügen","Add Donation":"Spende hinzufügen","Add Event":"Veranstaltung hinzufügen","Projects for Year":"Projekte für das Jahr","Donations for Year":"Spenden für das Jahr","Events for Year":"Veranstaltungen für das Jahr","Budget for Year":"Budget für das Jahr","Event type":"Veranstaltungstyp","Normal event":"Normale Veranstaltung","Event with tasks":"Veranstaltung mit Aufgaben","Project type":"Projekttyp","Standard charity project":"Standard-Wohltätigkeitsprojekt","Revenue-generating project":"Einnahmenprojekt","Monthly project income":"Monatliche Projekteinnahmen","Event tasks":"Veranstaltungsaufgaben","Task title":"Aufgabentitel","Assigned members":"Zugewiesene Mitglieder","Record a payment":"Zahlung erfassen","Record income":"Einnahme erfassen"}
};
Object.keys(actionTranslations).forEach(language => Object.assign(charityTranslations[language], actionTranslations[language]));

function initThemeAndLanguage() {
  const root = document.documentElement;
  const themeButton = document.getElementById('themeToggle');
  const syncThemeIcon = () => {
    const dark = root.dataset.theme === 'dark';
    const icon = themeButton?.querySelector('i');
    if (icon) icon.className = dark ? 'bi bi-sun' : 'bi bi-moon-stars';
    themeButton?.setAttribute('aria-label', dark ? 'Switch to light mode' : 'Switch to dark mode');
  };
  themeButton?.addEventListener('click', () => {
    root.dataset.theme = root.dataset.theme === 'dark' ? 'light' : 'dark';
    localStorage.setItem('charity-theme', root.dataset.theme);
    syncThemeIcon();
  });
  syncThemeIcon();

  const selector = document.getElementById('appLanguage');
  const language = localStorage.getItem('charity-language') || 'en';
  if (selector) selector.value = language;
  applyLanguage(language);
  selector?.addEventListener('change', () => {
    localStorage.setItem('charity-language', selector.value);
    window.location.reload();
  });
}

function applyLanguage(language) {
  document.documentElement.lang = language;
  if (language === 'en') return;
  const translations = charityTranslations[language] || {};
  document.querySelectorAll('[data-i18n]').forEach(element => {
    const key = element.dataset.i18n;
    if (translations[key]) element.textContent = translations[key];
  });
  const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, {
    acceptNode(node) {
      const parent = node.parentElement;
      if (!node.nodeValue.trim() || ['SCRIPT','STYLE'].includes(parent?.tagName)) return NodeFilter.FILTER_REJECT;
      if (parent?.tagName === 'OPTION' && parent.closest('#appLanguage')) return NodeFilter.FILTER_REJECT;
      return NodeFilter.FILTER_ACCEPT;
    }
  });
  const nodes = [];
  while (walker.nextNode()) nodes.push(walker.currentNode);
  nodes.forEach(node => {
    const key = node.nodeValue.trim();
    if (!translations[key]) return;
    const prefix = node.nodeValue.match(/^\s*/)[0];
    const suffix = node.nodeValue.match(/\s*$/)[0];
    node.nodeValue = prefix + translations[key] + suffix;
  });
  document.querySelectorAll('[placeholder],[title],[aria-label]').forEach(element => {
    ['placeholder','title','aria-label'].forEach(attribute => {
      const key = element.getAttribute(attribute);
      if (key && translations[key]) element.setAttribute(attribute, translations[key]);
    });
  });
}

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
  const actions = document.getElementById('mobileActions');
  const actionsToggle = document.querySelector('.mobile-actions-toggle');
  const actionsClose = document.querySelector('.mobile-actions-close');
  const actionsBackdrop = document.querySelector('.mobile-actions-backdrop');
  const closeActions = () => {
    actions?.classList.remove('open');
    actionsToggle?.setAttribute('aria-expanded', 'false');
    if (actionsBackdrop) actionsBackdrop.hidden = true;
    if (actions) actions.inert = mobile.matches;
  };
  const syncActions = () => { if (actions) actions.inert = mobile.matches && !actions.classList.contains('open'); };
  mobile.addEventListener('change', () => { closeActions(); syncActions(); });
  syncActions();
  actionsToggle?.addEventListener('click', () => {
    closeMenu();
    actions?.classList.add('open');
    actionsToggle.setAttribute('aria-expanded', 'true');
    if (actionsBackdrop) actionsBackdrop.hidden = false;
    syncActions();
    actionsClose?.focus();
  });
  actionsClose?.addEventListener('click', () => { closeActions(); actionsToggle?.focus(); });
  actionsBackdrop?.addEventListener('click', () => { closeActions(); actionsToggle?.focus(); });
  actions?.addEventListener('keydown', event => {
    if (event.key !== 'Tab' || !mobile.matches || !actions.classList.contains('open')) return;
    const controls = Array.from(actions.querySelectorAll('a,button,select,summary'))
      .filter(element => !element.disabled && element.getClientRects().length > 0);
    if (event.shiftKey && document.activeElement === controls[0]) { event.preventDefault(); controls.at(-1)?.focus(); }
    else if (!event.shiftKey && document.activeElement === controls.at(-1)) { event.preventDefault(); controls[0]?.focus(); }
  });
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && actions?.classList.contains('open')) { closeActions(); actionsToggle?.focus(); }
  });
  const accountMenu = document.querySelector('.account-menu');
  document.addEventListener('click', event => {
    if (accountMenu?.open && !accountMenu.contains(event.target)) accountMenu.open = false;
  });
  accountMenu?.addEventListener('keydown', event => {
    if (event.key === 'Escape') { accountMenu.open = false; accountMenu.querySelector('summary')?.focus(); }
  });
  const syncSidebar = () => { sidebar.inert = mobile.matches && !document.body.classList.contains('nav-open'); };
  const closeMenu = () => {document.body.classList.remove('nav-open'); menu?.setAttribute('aria-expanded','false');syncSidebar();};
  mobile.addEventListener('change', closeMenu);
  syncSidebar();
  menu?.addEventListener('click', () => {closeActions(); const open = document.body.classList.toggle('nav-open'); menu.setAttribute('aria-expanded',String(open));syncSidebar(); if(open) sidebar.querySelector('a')?.focus();});
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
    const open = () => {closeActions();closeMenu();if(!dialog.open)dialog.showModal();input.focus();};
    document.querySelectorAll('.jump-trigger, .mobile-search-trigger').forEach(trigger => trigger.addEventListener('click', open));
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
