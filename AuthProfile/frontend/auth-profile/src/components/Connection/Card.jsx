import './Card.css';

function Card({ name, createdBy, lastUpdated, onShare, onTest, onReconnect }) {
  const colors = ["#303F51", "#2B4937", "#504539", "#314649", "#3F384C"];
  const idx = (name || '').charCodeAt(0) % colors.length;
  const bg = colors[isNaN(idx) ? 0 : idx];
  const initial = (name || '?').charAt(0).toUpperCase();

  return (
    <div className="connection-card">
      <div className="connection-card-title">
        <span className="connection-card-icon" style={{ backgroundColor: bg }}>
          {initial}
        </span>
        <div className="connection-card-name">
          {name}
          <span className="connection-card-tag">Auth Profile</span>
        </div>
      </div>
      <div>{createdBy}</div>
      <div>{lastUpdated}</div>
      <div className="connection-card-action">
        <span className="connection-link-btn" onClick={onShare}>Share</span>
        <span className="connection-link-sep">|</span>
        <span className="connection-link-btn" onClick={onTest}>Test</span>
        <span className="connection-link-sep">|</span>
        <span className="connection-link-btn" onClick={onReconnect || onShare}>Reconnect</span>
      </div>
    </div>
  );
}

export default Card;
