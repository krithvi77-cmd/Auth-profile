import './AuthProfile.css';
import Button from '../Common/Button';
import Display from './Display';

function AuthProfile({ profiles = [], error = '', onCreate, onEdit, onDelete }) {
  return (
    <div className="auth-profile">
      <div className="auth-profile-header">
        <div className="auth-profile-header-left">
          <h2>Auth Profiles</h2>
          <p>Save the authentication details of apps to create connections for outgoing webhooks.</p>
        </div>
        <div className="auth-profile-header-right">
          <div className="auth-profile-search">
            <i className="bi bi-search"></i>
            <span contentEditable="true" placeholder="Search" />
          </div>
          <Button text="Create" onClick={onCreate} color="black" />
        </div>
      </div>

      {error && <div className="auth-profile-error">Error: {error}</div>}

      <Display profiles={profiles} onEdit={onEdit} onDelete={onDelete} />
    </div>
  );
}

export default AuthProfile;
