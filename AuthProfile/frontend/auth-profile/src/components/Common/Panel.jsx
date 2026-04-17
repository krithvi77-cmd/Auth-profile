 import './Panel.css';

function Panel() {
  return (
    <div className="panel">
      <div className="panel_header">
        <div className="panel_logo"/>
          <div className="panel_header_content">
            <h3>Auth Profiles</h3>
            <p>Integrate with applications using HTTP methods</p>
          </div>
             <i class="bi bi-x-lg"></i>
      </div>
      <div className="panel_container">
             <div className="panel_description">
             </div>
             <form className="panel_configurable">
                 <p>Authentication Method</p>
                 <input type="radio" name="group1" value="basic_auth"/>
                 <label> Basic Auth</label>
                 <input type="radio" name="group1"  value="oauth_v1.0"/>
                 <label>OAuth v1.0a</label>
                 <input type="radio" name="group1"  value="oauth_v2.0"/>
                 <label>OAuth v2</label>
                 <input type="radio" name="group1"  value="jwt"/>
                 <label>JWT Auth</label>
                 <input type="radio" name="group1"  value="api_key"/>
                 <label>API key</label>
                 <br/>
                 <div>
                 <label>Auth Profile Name</label><br/>
                 <input type="input" name="auth_profile_name"/>
                 </div>
                 <br/>
                 <div>
                 <label>Auth Profile Name</label><br/>
                 <input type="input" name="auth_profile_name"/>
                 </div>
             </form>
         </div>
    </div>
  )
}

export default Panel