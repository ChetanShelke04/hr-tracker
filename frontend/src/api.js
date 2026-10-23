const API_BASE = import.meta.env.VITE_API_URL || (typeof window !== 'undefined' && window.location.hostname === 'localhost' ? '/api/v1' : 'https://hr-tracker-998u.onrender.com/api/v1');

export const getAuthToken = () => localStorage.getItem('token');
export const setAuthToken = (token) => {
  if (token) {
    localStorage.setItem('token', token);
  } else {
    localStorage.removeItem('token');
  }
};

export const getCurrentUserStored = () => {
  try {
    return JSON.parse(localStorage.getItem('user'));
  } catch {
    return null;
  }
};

export const setCurrentUserStored = (user) => {
  if (user) {
    localStorage.setItem('user', JSON.stringify(user));
  } else {
    localStorage.removeItem('user');
  }
};

async function request(endpoint, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  const token = getAuthToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  if (response.status === 401 && endpoint !== '/auth/login') {
    setAuthToken(null);
    setCurrentUserStored(null);
    window.dispatchEvent(new Event('auth-changed'));
    throw new Error('Session expired. Please log in again.');
  }

  if (response.status === 403 || (response.status === 401 && endpoint === '/auth/login')) {
    throw new Error('Invalid username or password. If you have not created an account yet on this database, please click "Sign Up" above.');
  }

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({ message: 'Request failed' }));
    throw new Error(errorBody.message || errorBody.error || `HTTP ${response.status}`);
  }

  return response.json();
}

export const api = {
  // Auth
  async login(username, password) {
    const data = await request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
    setAuthToken(data.token);
    setCurrentUserStored({
      userId: data.userId,
      username: data.username,
      email: data.email,
      fullName: data.fullName,
    });
    return data;
  },

  async register(username, email, password, fullName) {
    const data = await request('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, email, password, fullName }),
    });
    setAuthToken(data.token);
    setCurrentUserStored({
      userId: data.userId,
      username: data.username,
      email: data.email,
      fullName: data.fullName,
    });
    return data;
  },

  logout() {
    setAuthToken(null);
    setCurrentUserStored(null);
    window.dispatchEvent(new Event('auth-changed'));
  },

  // Contacts
  getContacts(query) {
    const q = query ? `?q=${encodeURIComponent(query)}` : '';
    return request(`/contacts${q}`);
  },

  getContact(id) {
    return request(`/contacts/${id}`);
  },

  createContact(contact) {
    return request('/contacts', {
      method: 'POST',
      body: JSON.stringify(contact),
    });
  },

  updateContact(id, contact) {
    return request(`/contacts/${id}`, {
      method: 'PUT',
      body: JSON.stringify(contact),
    });
  },

  deleteContact(id) {
    return request(`/contacts/${id}`, {
      method: 'DELETE',
    });
  },

  getDueFollowups() {
    return request('/contacts/followups/due');
  },

  getDashboardStats() {
    return request('/contacts/dashboard/stats');
  },

  // Calls
  logCall(callData) {
    return request('/calls', {
      method: 'POST',
      body: JSON.stringify(callData),
    });
  },

  getRecentCalls() {
    return request('/calls/recent');
  },

  getCallsForContact(contactId) {
    return request(`/calls/contact/${contactId}`);
  },

  // Applications
  getApplications() {
    return request('/applications');
  },

  createApplication(appData) {
    return request('/applications', {
      method: 'POST',
      body: JSON.stringify(appData),
    });
  },

  updateApplication(id, appData) {
    return request(`/applications/${id}`, {
      method: 'PUT',
      body: JSON.stringify(appData),
    });
  },

  deleteApplication(id) {
    return request(`/applications/${id}`, {
      method: 'DELETE',
    });
  },

  // Import / Export
  exportData() {
    return request('/data/export');
  },

  importData(payload) {
    return request('/data/import', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
};
