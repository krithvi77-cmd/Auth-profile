import './AuthProfile.css';
import Button from '../Common/Button';
import Display from './Display';

function AuthProfile() {
  return (
    <div className="auth-profile">
      <div className="auth-profile-header">
        <div className="auth-profile-header-left">
            <h2>Auth Profiles</h2>
            <p>Save the authentication details of apps to create connections for outgoing webhooks.</p>
        </div>
        <div className="auth-profile-header-right">
            <div className="auth-profile-search">
                <i class="bi bi-search"></i>
                <span contenteditable="true" placeholder="Search"/>
            </div>
            <Button text="Create" onClick={() => redirect('/create-auth-profile')} color="black"/>
        </div>
      </div>
      <Display/>
    </div>
  );
}

export default AuthProfile;