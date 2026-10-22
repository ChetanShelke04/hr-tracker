import React, { useState } from 'react';
import { api } from '../api';

export default function ContactModal({ contact, onClose, onSave }) {
  const [name, setName] = useState(contact?.name || '');
  const [company, setCompany] = useState(contact?.company || '');
  const [phone, setPhone] = useState(contact?.phone || '');
  const [email, setEmail] = useState(contact?.email || '');
  const [status, setStatus] = useState(contact?.status || 'New');
  const [followupDate, setFollowupDate] = useState(contact?.followupDate || '');
  const [nextAction, setNextAction] = useState(contact?.nextAction || '');
  const [notes, setNotes] = useState(contact?.notes || '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    const payload = {
      name,
      company: company || null,
      phone: phone || null,
      email: email || null,
      status: status || 'New',
      followupDate: followupDate || null,
      nextAction: nextAction || null,
      notes: notes || null,
    };

    try {
      if (contact?.id) {
        await api.updateContact(contact.id, payload);
      } else {
        await api.createContact(payload);
      }
      onSave();
      onClose();
    } catch (err) {
      setError(err.message || 'Failed to save contact');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-dialog">
        <div className="modal-header">
          <h2 className="modal-title">{contact ? '✏️ Edit Contact' : '➕ Add HR Contact'}</h2>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            {error && (
              <div style={{ background: 'rgba(244,63,94,0.15)', color: 'var(--accent-rose)', padding: '8px 12px', borderRadius: '6px' }}>
                {error}
              </div>
            )}

            <div className="form-group">
              <label className="form-label">HR / Contact Name *</label>
              <input
                type="text"
                required
                className="form-input"
                placeholder="e.g. Priya Sharma"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div className="form-group">
                <label className="form-label">Company</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Infosys, TCS"
                  value={company}
                  onChange={(e) => setCompany(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Phone Number</label>
                <input
                  type="tel"
                  className="form-input"
                  placeholder="+919800000000"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                />
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div className="form-group">
                <label className="form-label">Email</label>
                <input
                  type="email"
                  className="form-input"
                  placeholder="hr@company.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Status</label>
                <select className="form-select" value={status} onChange={(e) => setStatus(e.target.value)}>
                  <option value="New">New</option>
                  <option value="Resume Requested">Resume Requested</option>
                  <option value="Follow Up Needed">Follow Up Needed</option>
                  <option value="Interview Scheduled">Interview Scheduled</option>
                  <option value="Waiting for HR Response">Waiting for HR Response</option>
                  <option value="Not Hiring / Closed">Not Hiring / Closed</option>
                </select>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div className="form-group">
                <label className="form-label">Follow-up Date</label>
                <input
                  type="date"
                  className="form-input"
                  value={followupDate}
                  onChange={(e) => setFollowupDate(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Next Action</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Send updated CV"
                  value={nextAction}
                  onChange={(e) => setNextAction(e.target.value)}
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Notes</label>
              <textarea
                className="form-textarea"
                placeholder="Tech stack, job ID, location, comments..."
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
              />
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving...' : '💾 Save Contact'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
