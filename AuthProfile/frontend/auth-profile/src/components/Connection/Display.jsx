import Card from './Card';
import './Display.css';

function Display({ connections = [], onEdit, onDelete, onShare, onTest, onReconnect }) {
  return (
    <div className="connection-display">
      <div className="connection-display-header">
        <span>Connection name</span>
        <span>Created by</span>
        <span>Last updated on</span>
        <span>Actions</span>
      </div>
      <div className="connection-display-body">
        {connections.length === 0 && (
          <div className="connection-display-empty">
            No connections yet. Click <b>Create Connection</b> to add one.
          </div>
        )}
        {connections.map(p => (
          <Card
            key={p.id}
            name={p.name}
            profileName={p.profileName}
            createdBy={p.createdBy ?? p.userId ?? '—'}
            lastUpdated={(p.createdAt || '').split(' ')[0] || '—'}
            onShare={() => onShare && onShare(p)}
            onTest={() => onTest && onTest(p)}
            onReconnect={() => onReconnect && onReconnect(p)}
            onEdit={() => onEdit && onEdit(p)}
            onDelete={() => onDelete && onDelete(p)}
          />
        ))}
      </div>
    </div>
  );
}

export default Display;
