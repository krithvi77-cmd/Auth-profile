import { useEffect, useState, useCallback } from 'react';
import { connectionApi } from '../../api';
import './CheckConnectionDialog.css';

function formatExpiry(iso) {
  if (!iso) return '—';
  try {
    return new Date(iso).toLocaleString();
  } catch (_) {
    return iso;
  }
}

function CheckConnectionDialog({ connection, onClose, onReconnectDone }) {
  const [info, setInfo] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async () => {
    if (!connection?.id) return;
    setLoading(true);
    try {
      const data = await connectionApi.check(connection.id);
      setInfo(data);
      setError('');
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [connection]);

  useEffect(() => { load(); }, [load]);

  const isOauth = info?.authType === 2;
  const isActive = isOauth ? (info?.hasToken && !info?.isExpired) : true;

  const handleReconnect = async () => {
    if (!connection?.id) return;
    setRefreshing(true);
    setError('');
    try {
      const result = await connectionApi.reconnect(connection.id, {});
      if (result?.authorizeUrl) {
        const popup = window.open(result.authorizeUrl, 'oauth_authorize', 'width=600,height=700');
        if (!popup) {
          setError('Please allow popups to complete re-authorisation.');
          setRefreshing(false);
          return;
        }
        const onMessage = (event) => {
          const data = event.data;
          if (!data || data.type !== 'oauth_result') return;
          window.removeEventListener('message', onMessage);
          setRefreshing(false);
          if (data.ok) {
            load();
            onReconnectDone && onReconnectDone();
          } else {
            setError('Re-authorisation failed: ' + (data.message || 'unknown error'));
          }
        };
        window.addEventListener('message', onMessage);
        return;
      }
      await load();
      onReconnectDone && onReconnectDone();
      setRefreshing(false);
    } catch (e) {
      setError(e.message);
      setRefreshing(false);
    }
  };

  return (
    <div className="check-dialog-backdrop" onClick={onClose}>
      <div className="check-dialog" onClick={(e) => e.stopPropagation()}>
        <div className="check-dialog-header">
          <h3>Connection Status</h3>
          <button className="check-dialog-close" onClick={onClose}>×</button>
        </div>

        <div className="check-dialog-body">
          {loading && <div className="check-loading">Loading…</div>}

          {!loading && info && (
            <>
              <div className="check-row">
                <span className="check-label">Expiry</span>
                <span className="check-value">
                  {isOauth ? formatExpiry(info.expiresAt) : 'No expiry'}
                </span>
              </div>
              <div className="check-row">
                <span className="check-label">Status</span>
                <span className={`check-badge ${isActive ? 'check-status-ok' : 'check-status-bad'}`}>
                  {isActive ? 'Active' : 'Expired'}
                </span>
              </div>
            </>
          )}

          {error && <div className="check-error">{error}</div>}
        </div>

        <div className="check-dialog-footer">
          <button className="check-btn-secondary" onClick={onClose}>Close</button>
          <button
            className="check-btn-primary"
            onClick={handleReconnect}
            disabled={refreshing || loading}
          >
            {refreshing ? 'Reconnecting…' : 'Reconnect'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default CheckConnectionDialog;
