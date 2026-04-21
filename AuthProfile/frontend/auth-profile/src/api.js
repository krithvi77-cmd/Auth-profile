const BASE = import.meta.env.VITE_API_BASE || '';

async function request(path, options = {}) {
  const res = await fetch(BASE + path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!res.ok) {
    let msg = `Request failed: ${res.status}`;
    try { msg = (await res.json()).error || msg; } catch (_) {}
    throw new Error(msg);
  }
  if (res.status === 204) return null;
  return res.json();
}

export const profileApi = {
  list:   ()          => request('/api/profiles'),
  get:    (id)        => request(`/api/profiles/${id}`),
  create: (profile)   => request('/api/profiles',        { method: 'POST',   body: JSON.stringify(profile) }),
  update: (id, data)  => request(`/api/profiles/${id}`,  { method: 'PUT',    body: JSON.stringify(data) }),
  remove: (id)        => request(`/api/profiles/${id}`,  { method: 'DELETE' }),
};
