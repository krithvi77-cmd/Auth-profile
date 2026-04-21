import './Connection.css';
import Button from '../Common/Button';
import Display from './Display';

function Connection({ profiles = [], error = '', onCreate, onEdit, onDelete, onShare, onTest, onReconnect }) {
  return (
    <div className="connection">
      <div className="connection-header">
        <div className="connection-header-left">
          <h2>Connections</h2>
          <p>Authorize Zoho Flow to access your apps and automate tasks securely.</p>
        </div>
        <div className="connection-header-right">
          <div className="connection-search">
            <i className="bi bi-search"></i>
            <span contentEditable="true" placeholder="Search connections here" />
          </div>
          <Button text="Create Connection" onClick={onCreate} color="black" />
        </div>
      </div>

      {error && <div className="connection-error">Error: {error}</div>}

      <Display
        profiles={profiles}
        onEdit={onEdit}
        onDelete={onDelete}
        onShare={onShare}
        onTest={onTest}
        onReconnect={onReconnect}
      />
    </div>
  );
}

export default Connection;
