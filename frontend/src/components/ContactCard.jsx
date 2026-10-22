import React from 'react';

export default function ContactCard({ contact, onLogCall, onEdit, onDelete }) {
  const getBadgeClass = (status) => {
    const s = (status || '').toLowerCase();
    if (s.includes('resume')) return 'badge-resume';
    if (s.includes('interview')) return 'badge-interview';
    if (s.includes('follow')) return 'badge-followup';
    if (s.includes('not') || s.includes('reject')) return 'badge-rejected';
    return 'badge-new';
  };

  const isOverdue = contact.followupDate && new Date(contact.followupDate) < new Date(new Date().toDateString());

  return (
    <div className="contact-card">
      <div className="card-top">
        <div>
          <h3 className="contact-name">{contact.name}</h3>
          {contact.company && <div className="contact-company">{contact.company}</div>}
        </div>
        <span className={`badge ${getBadgeClass(contact.status)}`}>
          {contact.status || 'New'}
        </span>
      </div>

      <div className="contact-details">
        {contact.phone && (
          <div className="detail-row">
            <span>📞</span>
            <a href={`tel:${contact.phone}`} className="btn-call" style={{ padding: '2px 8px', fontSize: '0.8rem' }}>
              {contact.phone} (Tap to Call)
            </a>
          </div>
        )}

        {contact.email && (
          <div className="detail-row">
            <span>✉️</span>
            <a href={`mailto:${contact.email}`} style={{ color: 'var(--text-secondary)', textDecoration: 'none' }}>
              {contact.email}
            </a>
          </div>
        )}

        {contact.followupDate && (
          <div className="detail-row" style={{ color: isOverdue ? 'var(--accent-rose)' : 'var(--text-secondary)' }}>
            <span>⏰</span>
            <span>
              Follow-up: <strong>{contact.followupDate}</strong> {isOverdue && '(Overdue!)'}
            </span>
          </div>
        )}

        {contact.interviewAt && (
          <div className="detail-row" style={{ color: 'var(--accent-emerald)' }}>
            <span>🎯</span>
            <span>Interview: {new Date(contact.interviewAt).toLocaleString()}</span>
          </div>
        )}

        {contact.nextAction && (
          <div className="detail-row" style={{ fontStyle: 'italic', color: 'var(--accent-cyan)' }}>
            <span>👉</span>
            <span>{contact.nextAction}</span>
          </div>
        )}

        {contact.notes && (
          <div style={{ marginTop: '6px', fontSize: '0.82rem', color: 'var(--text-muted)', background: 'rgba(0,0,0,0.2)', padding: '6px 10px', borderRadius: '6px' }}>
            {contact.notes}
          </div>
        )}
      </div>

      <div className="card-actions">
        <button className="btn btn-primary" style={{ flex: 1, padding: '6px 10px', fontSize: '0.82rem' }} onClick={() => onLogCall(contact)}>
          📞 Log Call
        </button>
        <button className="btn btn-secondary" style={{ padding: '6px 10px', fontSize: '0.82rem' }} onClick={() => onEdit(contact)}>
          ✏️ Edit
        </button>
        <button className="btn btn-danger" style={{ padding: '6px 10px', fontSize: '0.82rem' }} onClick={() => onDelete(contact.id)}>
          🗑️
        </button>
      </div>
    </div>
  );
}
