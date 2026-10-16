// Адрес бэкенда. Локально: http://localhost:8080, на проде заменить адресом сервера.
const API_BASE = ['localhost', '127.0.0.1'].includes(location.hostname)
    ? 'http://localhost:8080'
    : 'https://kvarta-landing.onrender.com';
const PHONE_MSG = 'Введите белорусский номер, например +375 29 123-45-67';

const form = document.getElementById('lead-form');
const statusEl = document.getElementById('form-status');
const button = form.querySelector('button[type="submit"]');
let sending = false;

// 1. Запоминаем адрес первого захода вместе с рекламными метками
try {
  if (!sessionStorage.getItem('landing_url')) {
    sessionStorage.setItem('landing_url', location.href);
  }
} catch (e) { /* хранилище недоступно: возьмём адрес при отправке */ }

function landingUrl() {
  try { return sessionStorage.getItem('landing_url') || location.href; }
  catch (e) { return location.href; }
}

// 2. «Будим» сервер, пока человек читает страницу
fetch(API_BASE + '/actuator/health', { mode: 'no-cors' }).catch(() => {});

// 3. Кнопки в карточках выбирают товар в списке
document.querySelectorAll('[data-interest]').forEach((el) => {
  el.addEventListener('click', () => {
    form.elements['interest'].value = el.dataset.interest;
  });
});

function normalizePhone(raw) {
  let d = raw.replace(/\D/g, '');
  if (d.length === 11 && d.startsWith('80')) d = '375' + d.slice(2);
  else if (d.length === 10 && d.startsWith('0')) d = '375' + d.slice(1);
  return /^375(25|29|33|44)\d{7}$/.test(d) ? '+' + d : null;
}

function setStatus(text, kind) {
  statusEl.textContent = text;
  statusEl.className = 'status' + (kind ? ' ' + kind : '');
}

function setFieldError(name, message) {
  const field = form.elements[name];
  if (!field) return false;
  field.setAttribute('aria-invalid', 'true');
  const p = document.createElement('p');
  p.className = 'field-error';
  p.textContent = message;
  field.insertAdjacentElement('afterend', p);
  return true;
}

function clearErrors() {
  form.querySelectorAll('.field-error').forEach((p) => p.remove());
  form.querySelectorAll('[aria-invalid]').forEach((f) => f.removeAttribute('aria-invalid'));
  setStatus('', '');
}

function showServerErrors(errors) {
  let general = '';
  Object.entries(errors).forEach(([key, message]) => {
    if (!setFieldError(key, message)) general = message;
  });
  if (general) setStatus(general, 'error');
  const first = form.querySelector('[aria-invalid="true"]');
  if (first) first.focus();
}

form.addEventListener('submit', async (e) => {
  e.preventDefault();
  if (sending) return;
  clearErrors();

  const name = form.elements['name'].value.trim();
  const phone = normalizePhone(form.elements['phone'].value);
  if (!name) setFieldError('name', 'Укажите имя');
  if (!phone) setFieldError('phone', PHONE_MSG);
  const invalid = form.querySelector('[aria-invalid="true"]');
  if (invalid) { invalid.focus(); return; }

  const payload = {
    name,
    phone,
    interest: form.elements['interest'].value,
    heard_from: form.elements['heard_from'].value,
    comment: form.elements['comment'].value.trim(),
    landing_url: landingUrl()
  };

  sending = true;
  button.disabled = true;
  button.textContent = 'Отправляем…';
  const slowTimer = setTimeout(() => {
    setStatus('Сервер просыпается, это может занять до минуты. Не закрывайте страницу.', '');
  }, 4000);
  const controller = new AbortController();
  const abortTimer = setTimeout(() => controller.abort(), 75000);

  try {
    const res = await fetch(API_BASE + '/api/lead', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
      signal: controller.signal
    });
    if (res.ok) {
      const data = await res.json().catch(() => ({}));
      form.reset();
      if (data.status === 'duplicate') {
        setStatus('Мы уже получили вашу заявку, менеджер скоро перезвонит.', 'ok');
      } else {
        setStatus('Спасибо! Заявка принята, менеджер скоро перезвонит.', 'ok');
      }
    } else if (res.status === 400) {
      const data = await res.json().catch(() => ({}));
      showServerErrors(data.errors || {});
    } else {
      throw new Error('HTTP ' + res.status);
    }
  } catch (err) {
    setStatus('Не удалось отправить заявку. Проверьте интернет и нажмите «Отправить заявку» ещё раз.', 'error');
  } finally {
    clearTimeout(slowTimer);
    clearTimeout(abortTimer);
    sending = false;
    button.disabled = false;
    button.textContent = 'Отправить заявку';
  }
});
