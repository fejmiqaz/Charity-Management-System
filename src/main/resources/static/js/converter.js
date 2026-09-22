(() => {
    const amount = document.getElementById('convertAmount');
    const from = document.getElementById('convertFrom');
    const results = document.getElementById('conversionResults');
    const source = document.getElementById('conversionSource');
    let rates = [];
    const currencies = ['MKD', 'EUR', 'CHF'];
    const render = () => {
        const value = Number(amount.value);
        if (!Number.isFinite(value) || value < 0) { results.textContent = 'Enter a valid amount.'; return; }
        results.replaceChildren();
        for (const quote of currencies) {
            const rate = quote === from.value ? 1 : rates.find(item => item.base?.toUpperCase() === from.value && item.quote?.toUpperCase() === quote)?.rate;
            const row = document.createElement('div');
            row.className = 'summary-row';
            const label = document.createElement('span');
            label.textContent = quote;
            const total = document.createElement('strong');
            total.textContent = rate == null ? 'Rate unavailable' : new Intl.NumberFormat(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value * rate) + ' ' + quote;
            row.append(label, total); results.append(row);
        }
    };
    const load = async () => {
        results.textContent = 'Loading rates…'; source.textContent = '';
        try {
            const response = await fetch('https://api.frankfurter.dev/v2/rates?base=' + encodeURIComponent(from.value) + '&quotes=' + currencies.filter(c => c !== from.value).join(',').toLowerCase());
            if (!response.ok) throw new Error('Rates unavailable');
            rates = await response.json();
            if (!Array.isArray(rates)) throw new Error('Rates unavailable');
            render();
            source.textContent = 'Source: Frankfurter reference rates · ' + (rates[0]?.date || 'latest available') + '. Estimates only.';
        } catch (_) { results.textContent = 'Exchange rates are unavailable right now. Try again later.'; }
    };
    from.addEventListener('change', load);
    amount.addEventListener('input', () => { if (rates.length) render(); });
    load();
})();
