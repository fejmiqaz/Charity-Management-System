(() => {
    const currencies = ['MKD', 'EUR', 'CHF'];
    const cache = new Map();
    async function rates(base) {
        if (!cache.has(base)) {
            cache.set(base, fetch('https://api.frankfurter.dev/v2/rates?base=' + base.toLowerCase() + '&quotes=' + currencies.filter(c => c !== base).join(',').toLowerCase())
                .then(response => { if (!response.ok) throw new Error('Rates unavailable'); return response.json(); }));
        }
        return cache.get(base);
    }
    async function update(form) {
        const amount = form.querySelector('[data-currency-amount]');
        const currency = form.querySelector('[data-currency-code]');
        const preview = form.querySelector('[data-currency-preview]');
        if (!amount || !currency || !preview) return;
        const value = Number(amount.value);
        if (!amount.value || !Number.isFinite(value) || value < 0) { preview.textContent = ''; return; }
        const base = currency.value;
        preview.textContent = 'Loading conversion estimate…';
        try {
            const items = await rates(base);
            if (base !== currency.value || value !== Number(amount.value)) return;
            const converted = currencies.filter(c => c !== base).map(quote => {
                const item = items.find(r => r.base?.toUpperCase() === base && r.quote?.toUpperCase() === quote);
                return item ? new Intl.NumberFormat(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2}).format(value * item.rate) + ' ' + quote : quote + ' unavailable';
            });
            preview.textContent = 'Estimate: ' + converted.join(' · ') + ' · rates ' + (items[0]?.date || 'latest');
        } catch (_) { preview.textContent = 'Conversion estimate unavailable.'; }
    }
    document.querySelectorAll('[data-currency-form]').forEach(form => {
        form.addEventListener('input', () => update(form));
        form.addEventListener('change', () => update(form));
        update(form);
    });
})();
