import React, { useState } from 'react';
import { api } from '../api';

export default function LogCallModal({ contact, onClose, onCallLogged }) {
  const [outcome, setOutcome] = useState('Resume Requested');
  const [notes, setNotes] = useState('');
  const [nextFollowupDate, setNextFollowupDate] = useState(() => {
    // Default to 3 days in the future
    const d = new Date();
    d.setDate(d.getDate() + 3);
    return d.toISOString().split('T')[0];
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await api.logCall({
        contactId: contact.id,
        outcome,
        notes,
        nextFollowupDate: nextFollowupDate || null,
      });
      onCallLogged();
      onClose();
    } catch (err) {
      setError(err.message || 'Failed to log call');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-dialog">
        <div className="modal-header">
          <h2 className="modal-title">📞 Log Call with {contact.name}</h2>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            {error && (
              <div style={{ background: 'rgba(244,63,94,0.15)', color: 'var(--accent-rose)', padding: '8px 12px', borderRadius: '6px' }}>
                {error}
              </div>
            )}

            <div style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>
              Company: <strong style={{ color: '#fff' }}>{contact.company || 'N/A'}</strong> | Phone: <strong style={{ color: '#fff' }}>{contact.phone || 'N/A'}</strong>
            </div>

            <div className="form-group">
              <label className="form-label">Call Outcome / Status</label>
              <select className="form-select" value={outcome} onChange={(e) => setOutcome(e.target.value)}>
                <option value="Resume Requested">📄 Resume Requested</option>
                <option value="Interview Scheduled">🎯 Interview Scheduled</option>
                <option value="Follow Up Needed">⏰ Follow Up Needed</option>
                <option value="Waiting for HR Response">⏳ Waiting for HR Response</option>
                <option value="Call Back Later / Busy">📴 Call Back Later / Busy</option>
                <option value="Not Hiring / Rejected">❌ Not Hiring / Rejected</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Call Discussion &amp; Notes</label>
              <textarea
                className="form-textarea"
                placeholder="What did the HR discuss? What role or package was mentioned?"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Next Follow-up Date (Auto-syncs to Contact)</label>
              <input
                type="date"
                className="form-input"
                value={nextFollowupDate}
                onChange={(e) => setNextFollowupDate(e.target.value)}
              />
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving...' : '💾 Save Call & Update Status'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
