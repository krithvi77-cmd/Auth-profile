import Card from './Card';
import './Display.css';

function Display({ profiles = [], onEdit, onShare, onTest, onReconnect }) {
  return (
    <div className="connection-display">
      <div className="connection-display-header">
        <span>Connection name</span>
        <span>Created by</span>
        <span>Last updated on</span>
        <span>Actions</span>
      </div>
      <div className="connection-display-body">
        {profiles.length === 0 && (
          <div className="connection-display-empty">
            No connections yet. Click <b>Create Connection</b> to add one.
          </div>
        )}
        {profiles.map(p => (
          <Card
            key={p.id}
            name={p.name}
            createdBy={p.createdBy ?? '—'}
            lastUpdated={(p.createdAt || '').split(' ')[0] || '—'}
            onShare={() => onShare && onShare(p)}
            onTest={() => onTest && onTest(p)}
            onReconnect={() => onReconnect && onReconnect(p)}
            onEdit={() => onEdit && onEdit(p)}
          />
        ))}
      </div>
    </div>
  );
}

export default Display;
