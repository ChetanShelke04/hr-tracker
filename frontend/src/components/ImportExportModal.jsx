import React, { useState } from 'react';
import { api } from '../api';

export default function ImportExportModal({ onDataImported }) {
  const [fileContent, setFileContent] = useState(null);
  const [fileName, setFileName] = useState('');
  const [preview, setPreview] = useState(null);
  const [importStatus, setImportStatus] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Handle Export
  const handleExport = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await api.exportData();
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `hr_tracker_backup_${new Date().toISOString().split('T')[0]}.json`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
    } catch (err) {
      setError(err.message || 'Export failed');
    } finally {
      setLoading(false);
    }
  };

  // Handle File Select
  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setFileName(file.name);
    setError('');
    setImportStatus(null);

    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const json = JSON.parse(event.target.result);
        setFileContent(json);
        setPreview({
          contactsCount: json.contacts?.length || 0,
          callsCount: json.calls?.length || 0,
          schemaVersion: json.schemaVersion || 1,
          exportedAt: json.exportedAt || 'Unknown',
        });
      } catch (err) {
        setError('Invalid JSON file format. Please upload a valid JSON backup.');
        setFileContent(null);
        setPreview(null);
      }
    };
    reader.readAsText(file);
  };

  // Confirm Import
  const handleImport = async () => {
    if (!fileContent) return;
    setLoading(true);
    setError('');
    setImportStatus(null);

    try {
      const summary = await api.importData(fileContent);
      setImportStatus(summary);
      onDataImported();
    } catch (err) {
      setError(err.message || 'Failed to import backup');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div className="glass-panel" style={{ padding: '24px' }}>
        <h2 style={{ fontSize: '1.25rem', marginBottom: '8px', color: '#fff' }}>💾 Export Your Data (JSON Backup)</h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '16px' }}>
          Download a complete, offline JSON copy of all your HR contacts, call logs, and schedules anytime.
        </p>
        <button className="btn btn-primary" onClick={handleExport} disabled={loading}>
          ⬇️ Download Complete JSON Backup
        </button>
      </div>

      <div className="glass-panel" style={{ padding: '24px' }}>
        <h2 style={{ fontSize: '1.25rem', marginBottom: '8px', color: '#fff' }}>📥 Import Existing Data / HTML Backup</h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '16px' }}>
          Restore your 145 contacts or migrate your backup directly into your account. Existing duplicates by phone number will be safely preserved and skipped.
        </p>

        {error && (
          <div style={{ background: 'rgba(244,63,94,0.15)', color: 'var(--accent-rose)', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px' }}>
            {error}
          </div>
        )}

        {importStatus && (
          <div style={{ background: 'rgba(16,185,129,0.15)', color: 'var(--accent-emerald)', padding: '12px 16px', borderRadius: '8px', marginBottom: '16px' }}>
            <strong>✅ {importStatus.message}</strong>
            <div style={{ fontSize: '0.88rem', marginTop: '4px' }}>
              Imported Calls: {importStatus.importedCalls} | Total Processed: {importStatus.totalContactsInFile}
            </div>
          </div>
        )}

        <div style={{
          border: '2px dashed var(--border-color)',
          borderRadius: '12px',
          padding: '28px',
          textAlign: 'center',
          background: 'rgba(15,23,42,0.4)',
          marginBottom: '16px'
        }}>
          <input
            type="file"
            accept=".json"
            id="json-upload"
            style={{ display: 'none' }}
            onChange={handleFileChange}
          />
          <label htmlFor="json-upload" className="btn btn-secondary" style={{ cursor: 'pointer' }}>
            📁 Select JSON Backup File
          </label>
          {fileName && <div style={{ marginTop: '10px', fontSize: '0.9rem', color: 'var(--accent-cyan)' }}>Selected: {fileName}</div>}
        </div>

        {preview && (
          <div style={{
            background: 'var(--bg-surface)',
            padding: '16px',
            borderRadius: '8px',
            marginBottom: '16px',
            border: '1px solid var(--border-color)'
          }}>
            <h4 style={{ color: '#fff', marginBottom: '8px' }}>Pre-Import Inspection</h4>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', fontSize: '0.88rem', color: 'var(--text-secondary)' }}>
              <div>Contacts in file: <strong style={{ color: '#fff' }}>{preview.contactsCount}</strong></div>
              <div>Calls in file: <strong style={{ color: '#fff' }}>{preview.callsCount}</strong></div>
              <div>Schema Version: <strong style={{ color: '#fff' }}>{preview.schemaVersion}</strong></div>
              <div>Export Date: <strong style={{ color: '#fff' }}>{preview.exportedAt}</strong></div>
            </div>

            <button
              className="btn btn-primary"
              style={{ marginTop: '16px', width: '100%', justifyContent: 'center' }}
              onClick={handleImport}
              disabled={loading}
            >
              {loading ? 'Importing contacts...' : `🚀 Confirm & Import ${preview.contactsCount} Records`}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
