import { useState } from 'react';
import './TestConnectionDialog.css';
import Button from '../Common/Button';
import { connectionApi } from '../../api';

function TestConnectionDialog({ connection, onClose, onReconnect }) {
  const [url, setUrl] = useState('');
  const [method, setMethod] = useState('GET');
  const [params, setParams] = useState([{ key: '', value: '' }]);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const updateParam = (index, field, value) => {
    setParams(prev => prev.map((p, i) => (i === index ? { ...p, [field]: value } : p)));
  };

  const addParam = () => setParams(prev => [...prev, { key: '', value: '' }]);

  const removeParam = (index) => {
    setParams(prev => prev.length === 1
      ? [{ key: '', value: '' }]
      : prev.filter((_, i) => i !== index));
  };

  const runTest = async () => {
    setError('');
    setResult(null);
    if (!url.trim()) {
      setError('Please enter a URL to test.');
      return;
    }
    setLoading(true);
    try {
      const cleaned = params
        .map(p => ({ key: (p.key || '').trim(), value: p.value || '' }))
        .filter(p => p.key.length > 0);
      const payload = { url: url.trim(), method, params: cleaned };
      const res = await connectionApi.test(connection.id, payload);
      setResult(res);
    } catch (e) {
      setError(e.message || 'Test failed');
    } finally {
      setLoading(false);
    }
  };

  const handleReconnectAndRetry = async () => {
    if (typeof onReconnect === 'function') {
      await onReconnect(connection);
    }
    setResult(null);
  };

  const isSuccess = result && result.ok === true;
  const isFailure = result && result.ok === false;
  const needsReconnect = isFailure && result.needsReconnect === true;

  return (
    <div className="test-dialog-backdrop" onClick={onClose}>
      <div className="test-dialog" onClick={(e) => e.stopPropagation()}>
        <div className="test-dialog-header">
          <div>
            <h3>Test Connection</h3>
            <p>{connection?.name}</p>
          </div>
          <i className="bi bi-x-lg" onClick={onClose}></i>
        </div>

        <div className="test-dialog-body">
          <div className="test-dialog-row">
            <label>Method</label>
            <select
              className="test-dialog-input test-dialog-method"
              value={method}
              onChange={(e) => setMethod(e.target.value)}
              disabled={loading}
            >
              <option value="GET">GET</option>
              <option value="POST">POST</option>
              <option value="PUT">PUT</option>
              <option value="DELETE">DELETE</option>
              <option value="PATCH">PATCH</option>
            </select>
          </div>

          <div className="test-dialog-row">
            <label>URL</label>
            <input
              className="test-dialog-input"
              type="text"
              placeholder="https://flow.zoho.in/api"
              value={url}
              onChange={(e) => setUrl(e.target.value)}
              disabled={loading}
            />
          </div>

          <div className="test-dialog-params">
            <div className="test-dialog-params-header">
              <label>Parameters</label>
              <button
                type="button"
                className="test-dialog-add-btn"
                onClick={addParam}
                disabled={loading}
                title="Add parameter"
              >
                <i className="bi bi-plus-lg"></i>
              </button>
            </div>

            {params.map((p, i) => (
              <div className="test-dialog-param-row" key={i}>
                <input
                  className="test-dialog-input"
                  type="text"
                  placeholder="key"
                  value={p.key}
                  onChange={(e) => updateParam(i, 'key', e.target.value)}
                  disabled={loading}
                />
                <span className="test-dialog-pipe">|</span>
                <input
                  className="test-dialog-input"
                  type="text"
                  placeholder="value"
                  value={p.value}
                  onChange={(e) => updateParam(i, 'value', e.target.value)}
                  disabled={loading}
                />
                <button
                  type="button"
                  className="test-dialog-remove-btn"
                  onClick={() => removeParam(i)}
                  disabled={loading}
                  title="Remove"
                >
                  <i className="bi bi-trash"></i>
                </button>
              </div>
            ))}
          </div>

          {error && <div className="test-dialog-error">{error}</div>}

          {isSuccess && (
            <div className="test-dialog-result test-dialog-result-success">
              <div className="test-dialog-result-icon">
                <i className="bi bi-check-circle-fill"></i>
              </div>
              <div className="test-dialog-result-content">
                <h4>Connection successful</h4>
                <p>{result.message}</p>
                <div className="test-dialog-meta">
                  <span>HTTP {result.status}</span>
                  <span>{result.latencyMs} ms</span>
                  {result.authType && <span>{result.authType}</span>}
                </div>
                {result.body && (
                  <pre className="test-dialog-body-preview">
                    {String(result.body).slice(0, 600)}
                    {String(result.body).length > 600 ? '\n…' : ''}
                  </pre>
                )}
              </div>
            </div>
          )}

          {isFailure && (
            <div className="test-dialog-result test-dialog-result-failure">
              <div className="test-dialog-result-icon">
                <i className="bi bi-x-circle-fill"></i>
              </div>
              <div className="test-dialog-result-content">
                <h4>Connection failed</h4>
                <p>{result.message}</p>
                <div className="test-dialog-meta">
                  {result.status > 0 && <span>HTTP {result.status}</span>}
                  {result.latencyMs >= 0 && <span>{result.latencyMs} ms</span>}
                  {result.authType && <span>{result.authType}</span>}
                </div>
                {result.body && (
                  <pre className="test-dialog-body-preview">
                    {String(result.body).slice(0, 600)}
                    {String(result.body).length > 600 ? '\n…' : ''}
                  </pre>
                )}
              </div>
            </div>
          )}
        </div>

        <div className="test-dialog-footer">
          <Button text="Close" onClick={onClose} color="grey" />
          {needsReconnect && (
            <Button
              text="Reconnect & Retry"
              onClick={handleReconnectAndRetry}
              color="black"
            />
          )}
          <Button
            text={loading ? 'Testing…' : (isFailure ? 'Retry Test' : 'Run Test')}
            onClick={runTest}
            color="black"
          />
        </div>
      </div>
    </div>
  );
}

export default TestConnectionDialog;
