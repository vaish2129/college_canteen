(() => {
  const toggle = document.querySelector('.nav-toggle');
  const nav = document.querySelector('.main-nav');
  if (toggle && nav) toggle.addEventListener('click', () => {
    const open = nav.classList.toggle('open');
    toggle.setAttribute('aria-expanded', String(open));
  });

  const chips = [...document.querySelectorAll('.filter-chip')];
  const cards = [...document.querySelectorAll('.food-card')];
  const search = document.getElementById('menuSearch');
  let category = 'all';
  const filter = () => {
    const query = search ? search.value.trim().toLowerCase() : '';
    cards.forEach(card => {
      const matchesCategory = category === 'all' || card.dataset.category === category;
      const matchesSearch = !query || card.dataset.search.toLowerCase().includes(query);
      card.hidden = !(matchesCategory && matchesSearch);
    });
  };
  chips.forEach(chip => chip.addEventListener('click', () => {
    category = chip.dataset.category;
    chips.forEach(item => item.classList.toggle('active', item === chip));
    filter();
  }));
  if (search) search.addEventListener('input', filter);

  document.querySelectorAll('[data-confirm]').forEach(button => button.addEventListener('click', event => {
    if (!window.confirm(button.dataset.confirm)) event.preventDefault();
  }));

  const chart = document.getElementById('salesChart');
  if (chart && Array.isArray(window.canteenSales) && window.canteenSales.length) {
    const values = window.canteenSales.map(day => Number(day.revenue) || 0);
    const max = Math.max(...values, 1);
    const formatter = new Intl.NumberFormat('en-IN', { maximumFractionDigits: 0 });
    window.canteenSales.forEach((day, index) => {
      const date = new Date(`${day.date}T00:00:00`);
      const column = document.createElement('div'); column.className = 'chart-column';
      const amount = Number(day.revenue) || 0;
      const value = document.createElement('span'); value.className = 'chart-value'; value.textContent = `₹${formatter.format(amount)}`;
      const bar = document.createElement('div'); bar.className = `chart-bar${index === values.length - 1 ? ' latest' : ''}`;
      bar.style.height = `${Math.max(3, amount / max * 100)}%`;
      const label = document.createElement('span'); label.className = 'chart-label'; label.textContent = date.toLocaleDateString('en-IN', { weekday: 'short' });
      column.append(value, bar, label); chart.append(column);
    });
  }
})();
