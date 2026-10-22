import React from 'react';

export default function Navbar({ currentTab, setCurrentTab, user, onLogout }) {
  const navItems = [
    { id: 'dashboard', label: '📊 Dashboard' },
    { id: 'contacts', label: '👥 HR Contacts' },
    { id: 'followups', label: '⏰ Due Follow-ups' },
    { id: 'applications', label: '💼 Applications' },
    { id: 'backup', label: '💾 Backup & Restore' },
  ];

  return (
    <aside className="sidebar">
      <div className="brand-badge">
        <div className="brand-logo">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
          </svg>
        </div>
        <div>
          <div className="brand-title">HR Tracker</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Multi-User Cloud Sync</div>
        </div>
      </div>

      <nav className="nav-links">
        {navItems.map((item) => (
          <button
            key={item.id}
            className={`nav-item ${currentTab === item.id ? 'active' : ''}`}
            onClick={() => setCurrentTab(item.id)}
          >
            {item.label}
          </button>
        ))}
      </nav>

      {user && (
        <div style={{ marginTop: 'auto', paddingTop: '16px', borderTop: '1px solid var(--border-color)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '12px' }}>
            <div style={{
              width: '36px', height: '36px', borderRadius: '50%', background: 'var(--accent-gradient)',
              display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold'
            }}>
              {user.username ? user.username.charAt(0).toUpperCase() : 'U'}
            </div>
            <div style={{ overflow: 'hidden' }}>
              <div style={{ fontSize: '0.9rem', fontWeight: 600, color: '#fff', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {user.fullName || user.username}
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                @{user.username}
              </div>
            </div>
          </div>
          <button
            onClick={onLogout}
            className="btn btn-secondary"
            style={{ width: '100%', justifyContent: 'center', fontSize: '0.85rem', padding: '8px' }}
          >
            Log Out
          </button>
        </div>
      )}
    </aside>
  );
}
