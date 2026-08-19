// Small fetch wrapper: sends JSON, keeps the session cookie,
// and turns backend error responses into readable Error objects.
async function request(path, options = {}) {
  const res = await fetch(`/api${path}`, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  if (res.status === 204) return null;

  let data = null;
  try {
    data = await res.json();
  } catch {
    data = null;
  }

  if (!res.ok) {
    const error = new Error((data && data.message) || `Request failed (${res.status})`);
    error.status = res.status;
    throw error;
  }

  return data;
}

// Builds a query string, skipping empty values
export function qs(params) {
  const sp = new URLSearchParams();
  for (const [key, value] of Object.entries(params || {})) {
    if (value !== undefined && value !== null && value !== '') {
      sp.append(key, value);
    }
  }
  return sp.toString();
}

export const api = {
  get: (path) => request(path),
  post: (path, body) => request(path, { method: 'POST', body: JSON.stringify(body) }),
  put: (path, body) => request(path, { method: 'PUT', body: body === undefined ? undefined : JSON.stringify(body) }),
  del: (path) => request(path, { method: 'DELETE' }),
};
