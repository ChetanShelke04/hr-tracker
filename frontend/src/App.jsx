import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import AuthModal from './components/AuthModal';
import ContactCard from './components/ContactCard';
import ContactModal from './components/ContactModal';
import LogCallModal from './components/LogCallModal';
import ImportExportModal from './components/ImportExportModal';
import { api, getAuthToken, getCurrentUserStored } from './api';

export default function App() {
  const [user, setUser] = useState(getCurrentUserStored());
  const [currentTab, setCurrentTab] = useState('dashboard');
  const [contacts, setContacts] = useState([]);
  const [stats, setStats] = useState({ totalContacts: 0, totalCalls: 0, dueFollowupsCount: 0, dueFollowups: [] });
  const [recentCalls, setRecentCalls] = useState([]);
  const [applications, setApplications] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(false);

  // Modals state
  const [isContactModalOpen, setIsContactModalOpen] = useState(false);
  const [selectedContact, setSelectedContact] = useState(null);
  const [isLogCallModalOpen, setIsLogCallModalOpen] = useState(false);
  const [callTargetContact, setCallTargetContact] = useState(null);

  // Check auth
  useEffect(() => {
    const handleAuthChange = () => {
      setUser(getCurrentUserStored());
    };
    window.addEventListener('auth-changed', handleAuthChange);
    return () => window.removeEventListener('auth-changed', handleAuthChange);
  }, []);

  // Fetch data when logged in or tab changed
  const refreshData = async () => {
    if (!getAuthToken()) return;
    setLoading(true);
    try {
      const [contactsData, statsData, callsData, appsData] = await Promise.all([
        api.getContacts(searchQuery),
        api.getDashboardStats().catch(() => ({ totalContacts: 0, totalCalls: 0, dueFollowupsCount: 0, dueFollowups: [] })),
        api.getRecentCalls().catch(() => []),
        api.getApplications().catch(() => []),
      ]);
      setContacts(contactsData);
      setStats(statsData);
      setRecentCalls(callsData);
      setApplications(appsData);
    } catch (err) {
      console.error('Failed to load data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user) {
      refreshData();
    }
  }, [user, searchQuery]);

  const handleAuthSuccess = (res) => {
    setUser({
      userId: res.userId,
      username: res.username,
      email: res.email,
      fullName: res.fullName,
    });
  };

  const handleLogout = () => {
    api.logout();
    setUser(null);
  };

  const handleDeleteContact = async (id) => {
    if (!window.confirm('Are you sure you want to delete this contact?')) return;
    try {
      await api.deleteContact(id);
      refreshData();
    } catch (err) {
      alert(err.message || 'Failed to delete');
    }
  };

  if (!user) {
    return <AuthModal onAuthSuccess={handleAuthSuccess} />;
  }

  return (
    <div className="app-container">
      <Navbar
        currentTab={currentTab}
        setCurrentTab={setCurrentTab}
        user={user}
        onLogout={handleLogout}
      />

      <main className="main-content">
        {/* Top Header */}
        <header className="top-header">
          <div>
            <h1 className="page-title">
              {currentTab === 'dashboard' && 'Dashboard Overview'}
              {currentTab === 'contacts' && 'HR Contacts'}
              {currentTab === 'followups' && 'Due Follow-ups & Reminders'}
              {currentTab === 'applications' && 'Job Applications'}
              {currentTab === 'backup' && 'Data Management & Backup'}
            </h1>
            <p className="page-subtitle">
              Cloud synchronized across phone and PC • Data securely isolated
            </p>
          </div>

          <div className="header-actions">
            {currentTab === 'contacts' && (
              <button
                className="btn btn-primary"
                onClick={() => { setSelectedContact(null); setIsContactModalOpen(true); }}
              >
                ➕ Add Contact
              </button>
            )}
            <button className="btn btn-secondary" onClick={refreshData}>
              🔄 Refresh
            </button>
          </div>
        </header>

        {/* Tab 1: Dashboard */}
        {currentTab === 'dashboard' && (
          <div>
            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-card-title">Total HR Contacts</div>
                <div className="stat-card-value">{stats.totalContacts}</div>
              </div>
              <div className="stat-card">
                <div className="stat-card-title">Calls Logged</div>
                <div className="stat-card-value">{stats.totalCalls}</div>
              </div>
              <div className="stat-card">
                <div className="stat-card-title">Follow-ups Due Today</div>
                <div className="stat-card-value" style={{ color: stats.dueFollowupsCount > 0 ? 'var(--accent-rose)' : '#fff' }}>
                  {stats.dueFollowupsCount}
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-card-title">Applications Tracked</div>
                <div className="stat-card-value" style={{ color: 'var(--accent-emerald)' }}>
                  {applications.length}
                </div>
              </div>
            </div>

            {/* Due Followups Banner */}
            {stats.dueFollowups && stats.dueFollowups.length > 0 && (
              <div className="glass-panel" style={{ padding: '20px', marginBottom: '32px', borderColor: 'rgba(244,63,94,0.3)' }}>
                <h3 style={{ color: 'var(--accent-rose)', marginBottom: '12px' }}>🚨 Overdue &amp; Due Today Follow-ups</h3>
                <div className="cards-grid">
                  {stats.dueFollowups.slice(0, 3).map((contact) => (
                    <ContactCard
                      key={contact.id}
                      contact={contact}
                      onLogCall={(c) => { setCallTargetContact(c); setIsLogCallModalOpen(true); }}
                      onEdit={(c) => { setSelectedContact(c); setIsContactModalOpen(true); }}
                      onDelete={handleDeleteContact}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* Recent Calls Log */}
            <div className="glass-panel" style={{ padding: '24px' }}>
              <h3 style={{ color: '#fff', marginBottom: '16px' }}>📞 Recent Call Activities</h3>
              {recentCalls.length === 0 ? (
                <div style={{ color: 'var(--text-muted)' }}>No calls logged yet. Click "Log Call" on any contact to record conversations.</div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {recentCalls.slice(0, 5).map((call) => (
                    <div
                      key={call.id}
                      style={{
                        padding: '12px 16px',
                        background: 'var(--bg-surface)',
                        borderRadius: '8px',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        flexWrap: 'wrap',
                        gap: '8px'
                      }}
                    >
                      <div>
                        <strong style={{ color: '#fff' }}>{call.contactName}</strong>
                        {call.contactCompany && <span style={{ color: 'var(--accent-cyan)', marginLeft: '8px' }}>({call.contactCompany})</span>}
                        <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '2px' }}>
                          {call.notes || 'No extra notes recorded'}
                        </div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <span className="badge badge-resume">{call.outcome}</span>
                        <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                          {call.calledAt ? new Date(call.calledAt).toLocaleDateString() : ''}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}

        {/* Tab 2: HR Contacts */}
        {currentTab === 'contacts' && (
          <div>
            <div style={{ marginBottom: '24px', display: 'flex', gap: '12px' }}>
              <div className="search-wrapper">
                <span className="search-icon">🔍</span>
                <input
                  type="text"
                  className="search-input"
                  placeholder="Search contacts by name, company, phone or status..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
            </div>

            {loading ? (
              <div style={{ color: 'var(--text-muted)' }}>Loading contacts...</div>
            ) : contacts.length === 0 ? (
              <div className="glass-panel" style={{ padding: '40px', textAlign: 'center' }}>
                <h3>No contacts found</h3>
                <p style={{ color: 'var(--text-muted)', marginTop: '8px', marginBottom: '16px' }}>
                  Add your first HR contact or import your existing 145 contacts backup!
                </p>
                <button
                  className="btn btn-primary"
                  onClick={() => { setSelectedContact(null); setIsContactModalOpen(true); }}
                >
                  ➕ Add HR Contact
                </button>
              </div>
            ) : (
              <div className="cards-grid">
                {contacts.map((contact) => (
                  <ContactCard
                    key={contact.id}
                    contact={contact}
                    onLogCall={(c) => { setCallTargetContact(c); setIsLogCallModalOpen(true); }}
                    onEdit={(c) => { setSelectedContact(c); setIsContactModalOpen(true); }}
                    onDelete={handleDeleteContact}
                  />
                ))}
              </div>
            )}
          </div>
        )}

        {/* Tab 3: Due Followups */}
        {currentTab === 'followups' && (
          <div>
            {stats.dueFollowups && stats.dueFollowups.length === 0 ? (
              <div className="glass-panel" style={{ padding: '40px', textAlign: 'center' }}>
                <h3>🎉 All caught up!</h3>
                <p style={{ color: 'var(--text-muted)', marginTop: '8px' }}>
                  No overdue follow-ups for today. Great job keeping in touch!
                </p>
              </div>
            ) : (
              <div className="cards-grid">
                {(stats.dueFollowups || []).map((contact) => (
                  <ContactCard
                    key={contact.id}
                    contact={contact}
                    onLogCall={(c) => { setCallTargetContact(c); setIsLogCallModalOpen(true); }}
                    onEdit={(c) => { setSelectedContact(c); setIsContactModalOpen(true); }}
                    onDelete={handleDeleteContact}
                  />
                ))}
              </div>
            )}
          </div>
        )}

        {/* Tab 4: Applications */}
        {currentTab === 'applications' && (
          <div className="glass-panel" style={{ padding: '24px' }}>
            <h3 style={{ marginBottom: '16px', color: '#fff' }}>💼 Application Pipeline</h3>
            {applications.length === 0 ? (
              <div style={{ color: 'var(--text-muted)' }}>
                Track your active job applications, interview stages, and expected CTCs.
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {applications.map((app) => (
                  <div key={app.id} style={{ padding: '14px', background: 'var(--bg-surface)', borderRadius: '8px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <strong style={{ color: '#fff' }}>{app.role} at {app.company}</strong>
                      <span className="badge badge-interview">{app.stage}</span>
                    </div>
                    {app.salaryExpectation && (
                      <div style={{ fontSize: '0.85rem', color: 'var(--accent-emerald)', marginTop: '4px' }}>
                        Expected: {app.salaryExpectation}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Tab 5: Backup & Restore */}
        {currentTab === 'backup' && (
          <ImportExportModal onDataImported={refreshData} />
        )}
      </main>

      {/* Add / Edit Contact Modal */}
      {isContactModalOpen && (
        <ContactModal
          contact={selectedContact}
          onClose={() => setIsContactModalOpen(false)}
          onSave={refreshData}
        />
      )}

      {/* Log Call Modal */}
      {isLogCallModalOpen && callTargetContact && (
        <LogCallModal
          contact={callTargetContact}
          onClose={() => { setIsLogCallModalOpen(false); setCallTargetContact(null); }}
          onCallLogged={refreshData}
        />
      )}
    </div>
  );
}
