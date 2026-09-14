document.addEventListener('DOMContentLoaded', () => {
    const header = document.getElementById('siteHeader');
    const toggle = document.querySelector('.menu-toggle');
    const links = document.getElementById('publicLinks');
    const updateHeader = () => header.classList.toggle('scrolled', window.scrollY > 10);
    updateHeader();
    window.addEventListener('scroll', updateHeader, {passive: true});
    const closeMenu = () => { links.classList.remove('is-open'); toggle.setAttribute('aria-expanded', 'false'); };
    toggle.addEventListener('click', () => {
        const open = links.classList.toggle('is-open');
        toggle.setAttribute('aria-expanded', String(open));
    });
    links.addEventListener('click', event => { if (event.target.closest('a')) closeMenu(); });
    document.addEventListener('keydown', event => { if (event.key === 'Escape') { closeMenu(); toggle.focus(); } });
    window.matchMedia('(min-width: 921px)').addEventListener('change', closeMenu);
});
