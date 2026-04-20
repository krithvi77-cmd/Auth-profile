import { useState } from 'react';
import Button from '../Common/Button';

// maps backend auth_type id -> radio value used in this form
const AUTH_ID_TO_METHOD = { 1: 'basic_auth', 2: 'oauth_v2', 3: 'api_key', 4: 'jwt' };

function AuthProfileForm({ onSave, onCancel, initial = null }) {

  const isEdit = !!initial;

  // Pick the right radio based on auth_type + a heuristic for v1 vs v2
  function detectMethod(ini) {
    if (!ini) return 'basic_auth';
    const t = ini.auth_type;
    if (t === 2 && ini.values && ini.values.request_token_url) return 'oauth_v1';
    return AUTH_ID_TO_METHOD[t] || 'basic_auth';
  }

  const [authMethod, setAuthMethod] = useState(detectMethod(initial));

  // two checkboxes
  const [onPremises, setOnPremises]       = useState(false);
  const [enableTestUrl, setEnableTestUrl] = useState(false);

  // common field (shown for every method)
  const [profileName, setProfileName] = useState(initial?.name || '');

  // prefill source (single object so every section can read from it)
  const v = initial?.values || {};

  // basic auth fields
  const [username, setUsername] = useState(v.username || '');
  const [password, setPassword] = useState(v.password || '');

  // oauth v1 fields
  const [v1ClientId, setV1ClientId]         = useState(v.client_id || '');
  const [v1ClientSecret, setV1ClientSecret] = useState(v.client_secret || '');
  const [v1RequestUrl, setV1RequestUrl]     = useState(v.request_token_url || '');
  const [v1AuthUrl, setV1AuthUrl]           = useState(v.authorization_url || '');
  const [v1AccessUrl, setV1AccessUrl]       = useState(v.access_token_url || '');
  const [v1Scopes, setV1Scopes]             = useState(v.scopes || '');

  // oauth v2 fields
  const [v2ClientId, setV2ClientId]         = useState(v.client_id || '');
  const [v2ClientSecret, setV2ClientSecret] = useState(v.client_secret || '');
  const [v2AuthUrl, setV2AuthUrl]           = useState(v.authorization_url || '');
  const [v2AccessUrl, setV2AccessUrl]       = useState(v.access_token_url || '');
  const [v2Scopes, setV2Scopes]             = useState(v.scopes || '');
  const [v2Placement, setV2Placement]       = useState(v.token_placement || '');

  // jwt fields
  const [jwtIssuer, setJwtIssuer]       = useState(v.issuer || '');
  const [jwtSubject, setJwtSubject]     = useState(v.subject || '');
  const [jwtAudience, setJwtAudience]   = useState(v.audience || '');
  const [jwtSecret, setJwtSecret]       = useState(v.secret_key || '');
  const [jwtAlgorithm, setJwtAlgorithm] = useState(v.algorithm || '');

  // api key fields
  const [apiFieldLabel, setApiFieldLabel]   = useState(v.field_label || '');
  const [apiFieldType, setApiFieldType]     = useState(v.field_type || '');
  const [apiParamName, setApiParamName]     = useState(v.parameter_name || '');
  const [apiPlacement, setApiPlacement]     = useState(v.api_key_placement || '');

  // one errors object for all fields
  const [errors, setErrors] = useState({});

  // clear the error of a single field when user types
  function clearError(key) {
    if (errors[key]) {
      const next = { ...errors };
      delete next[key];
      setErrors(next);
    }
  }

  // ------- SAVE -------
  function handleSave() {
    const next = {};

    if (!profileName.trim()) next.profileName = 'Auth Profile Name is required';

    if (authMethod === 'basic_auth') {
      if (!username.trim()) next.username = 'Username is required';
      if (!password.trim()) next.password = 'Password is required';
    }

    if (authMethod === 'oauth_v1') {
      if (!v1ClientId.trim())     next.v1ClientId     = 'Client ID is required';
      if (!v1ClientSecret.trim()) next.v1ClientSecret = 'Client Secret is required';
      if (!v1RequestUrl.trim())   next.v1RequestUrl   = 'Request Token URL is required';
      if (!v1AuthUrl.trim())      next.v1AuthUrl      = 'Authorization URL is required';
      if (!v1AccessUrl.trim())    next.v1AccessUrl    = 'Access Token URL is required';
    }

    if (authMethod === 'oauth_v2') {
      if (!v2ClientId.trim())     next.v2ClientId     = 'Client ID is required';
      if (!v2ClientSecret.trim()) next.v2ClientSecret = 'Client Secret is required';
      if (!v2AuthUrl.trim())      next.v2AuthUrl      = 'Authorization URL is required';
      if (!v2AccessUrl.trim())    next.v2AccessUrl    = 'Access Token URL is required';
      if (!v2Placement.trim())    next.v2Placement    = 'Access Token Placement is required';
    }

    if (authMethod === 'jwt') {
      if (!jwtIssuer.trim())    next.jwtIssuer    = 'Issuer is required';
      if (!jwtSecret.trim())    next.jwtSecret    = 'Secret Key is required';
      if (!jwtAlgorithm.trim()) next.jwtAlgorithm = 'Algorithm is required';
    }

    if (authMethod === 'api_key') {
      if (!apiFieldLabel.trim()) next.apiFieldLabel = 'Field Label is required';
      if (!apiFieldType.trim())  next.apiFieldType  = 'Field Type is required';
      if (!apiParamName.trim())  next.apiParamName  = 'Parameter Name is required';
      if (!apiPlacement.trim())  next.apiPlacement  = 'API Key Placement is required';
    }

    setErrors(next);
    if (Object.keys(next).length > 0) return;

    // build payload
    let authCode = 1;
    if (authMethod === 'oauth_v1') authCode = 2;
    if (authMethod === 'oauth_v2') authCode = 2;
    if (authMethod === 'api_key')  authCode = 3;
    if (authMethod === 'jwt')      authCode = 4;

    let values = {};
    if (authMethod === 'basic_auth') {
      values = { username, password };
    } else if (authMethod === 'oauth_v1') {
      values = {
        client_id: v1ClientId,
        client_secret: v1ClientSecret,
        request_token_url: v1RequestUrl,
        authorization_url: v1AuthUrl,
        access_token_url: v1AccessUrl,
        scopes: v1Scopes,
      };
    } else if (authMethod === 'oauth_v2') {
      values = {
        client_id: v2ClientId,
        client_secret: v2ClientSecret,
        authorization_url: v2AuthUrl,
        access_token_url: v2AccessUrl,
        scopes: v2Scopes,
        token_placement: v2Placement,
      };
    } else if (authMethod === 'jwt') {
      values = {
        issuer: jwtIssuer,
        subject: jwtSubject,
        audience: jwtAudience,
        secret_key: jwtSecret,
        algorithm: jwtAlgorithm,
      };
    } else if (authMethod === 'api_key') {
      values = {
        field_label: apiFieldLabel,
        field_type: apiFieldType,
        parameter_name: apiParamName,
        api_key_placement: apiPlacement,
      };
    }

    if (onSave) {
      onSave({
        name: profileName,
        auth_type: authCode,
        on_premises: onPremises,
        enable_test_url: enableTestUrl,
        values: values,
      });
    }
  }

  // ------- RENDER -------
  return (
    <form onSubmit={(e) => e.preventDefault()}>

      {/* ===== Authentication Method ===== */}
      <div className="panel_section_title">Authentication Method</div>

      <div className="panel_radio_group">
        <label className="panel_radio_item">
          <input
            type="radio" name="auth_method" value="basic_auth"
            checked={authMethod === 'basic_auth'}
            onChange={() => { setAuthMethod('basic_auth'); setErrors({}); }}
          />
          <span>Basic Auth</span>
        </label>

        <label className="panel_radio_item">
          <input
            type="radio" name="auth_method" value="oauth_v1"
            checked={authMethod === 'oauth_v1'}
            onChange={() => { setAuthMethod('oauth_v1'); setErrors({}); }}
          />
          <span>OAuth v1.0a</span>
        </label>

        <label className="panel_radio_item">
          <input
            type="radio" name="auth_method" value="oauth_v2"
            checked={authMethod === 'oauth_v2'}
            onChange={() => { setAuthMethod('oauth_v2'); setErrors({}); }}
          />
          <span>OAuth v2</span>
        </label>

        <label className="panel_radio_item">
          <input
            type="radio" name="auth_method" value="jwt"
            checked={authMethod === 'jwt'}
            onChange={() => { setAuthMethod('jwt'); setErrors({}); }}
          />
          <span>JWT Auth</span>
        </label>

        <label className="panel_radio_item">
          <input
            type="radio" name="auth_method" value="api_key"
            checked={authMethod === 'api_key'}
            onChange={() => { setAuthMethod('api_key'); setErrors({}); }}
          />
          <span>API Key</span>
        </label>
      </div>

      {/* ===== On-premises checkbox ===== */}
      <label className="panel_checkbox_item">
        <input
          type="checkbox"
          checked={onPremises}
          onChange={(e) => setOnPremises(e.target.checked)}
        />
        <span>This app is installed on-premises</span>
        <i className="bi bi-info-circle"></i>
      </label>

      {/* ===== Auth Profile Name (always shown) ===== */}
      <div className="panel_field">
        <label className="panel_field_label">
          Auth Profile Name<span className="panel_required">*</span>
        </label>
        <input
          type="text"
          className={errors.profileName ? 'panel_input panel_input_error' : 'panel_input'}
          value={profileName}
          onChange={(e) => { setProfileName(e.target.value); clearError('profileName'); }}
        />
        {errors.profileName && <span className="panel_error_tooltip">{errors.profileName}</span>}
      </div>

      {/* ===================== BASIC AUTH ===================== */}
      {authMethod === 'basic_auth' && (
        <>
          <div className="panel_section_title panel_section_title_sub">
            Basic Auth Configuration
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Username<span className="panel_required">*</span>
            </label>
            <input
              type="text"
              className={errors.username ? 'panel_input panel_input_error' : 'panel_input'}
              value={username}
              onChange={(e) => { setUsername(e.target.value); clearError('username'); }}
            />
            {errors.username && <span className="panel_error_tooltip">{errors.username}</span>}
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Password<span className="panel_required">*</span>
            </label>
            <input
              type="password"
              className={errors.password ? 'panel_input panel_input_error' : 'panel_input'}
              value={password}
              onChange={(e) => { setPassword(e.target.value); clearError('password'); }}
            />
            {errors.password && <span className="panel_error_tooltip">{errors.password}</span>}
          </div>
        </>
      )}

      {/* ===================== OAUTH V1 ===================== */}
      {authMethod === 'oauth_v1' && (
        <>
          <div className="panel_section_title panel_section_title_sub">
            OAuth v1.0a Configuration
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Client ID<span className="panel_required">*</span>
            </label>
            <input
              type="text"
              className={errors.v1ClientId ? 'panel_input panel_input_error' : 'panel_input'}
              value={v1ClientId}
              onChange={(e) => { setV1ClientId(e.target.value); clearError('v1ClientId'); }}
            />
            {errors.v1ClientId && <span className="panel_error_tooltip">{errors.v1ClientId}</span>}
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Client Secret<span className="panel_required">*</span>
            </label>
            <input
              type="password"
              className={errors.v1ClientSecret ? 'panel_input panel_input_error' : 'panel_input'}
              value={v1ClientSecret}
              onChange={(e) => { setV1ClientSecret(e.target.value); clearError('v1ClientSecret'); }}
            />
            {errors.v1ClientSecret && <span className="panel_error_tooltip">{errors.v1ClientSecret}</span>}
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Request Token URL<span className="panel_required">*</span>
            </label>
            <input
              type="text"
              className={errors.v1RequestUrl ? 'panel_input panel_input_error' : 'panel_input'}
              value={v1RequestUrl}
              onChange={(e) => { setV1RequestUrl(e.target.value); clearError('v1RequestUrl'); }}
            />
            {errors.v1RequestUrl && <span className="panel_error_tooltip">{errors.v1RequestUrl}</span>}
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Authorization URL<span className="panel_required">*</span>
            </label>
            <input
              type="text"
              className={errors.v1AuthUrl ? 'panel_input panel_input_error' : 'panel_input'}
              value={v1AuthUrl}
              onChange={(e) => { setV1AuthUrl(e.target.value); clearError('v1AuthUrl'); }}
            />
            {errors.v1AuthUrl && <span className="panel_error_tooltip">{errors.v1AuthUrl}</span>}
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Access Token URL<span className="panel_required">*</span>
            </label>
            <input
              type="text"
              className={errors.v1AccessUrl ? 'panel_input panel_input_error' : 'panel_input'}
              value={v1AccessUrl}
              onChange={(e) => { setV1AccessUrl(e.target.value); clearError('v1AccessUrl'); }}
            />
            {errors.v1AccessUrl && <span className="panel_error_tooltip">{errors.v1AccessUrl}</span>}
          </div>

          <div className="panel_field">
            <label className="panel_field_label">Scopes</label>
            <textarea
              className="panel_input panel_textarea"
              value={v1Scopes}
              onChange={(e) => setV1Scopes(e.target.value)}
            />
          </div>
        </>
      )}

      {/* ===================== OAUTH V2 ===================== */}
      {authMethod === 'oauth_v2' && (
        <>
          <div className="panel_section_title panel_section_title_sub">
            OAuth v2 Configuration
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Client ID<span className="panel_required">*</span>
            </label>
            <input
              type="text"
              className={errors.v2ClientId ? 'panel_input panel_input_error' : 'panel_input'}
              value={v2ClientId}
              onChange={(e) => { setV2ClientId(e.target.value); clearError('v2ClientId'); }}
            />
            {errors.v2ClientId && <span className="panel_error_tooltip">{errors.v2ClientId}</span>}
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Client Secret<span className="panel_required">*</span>
            </label>
            <input
              type="password"
              className={errors.v2ClientSecret ? 'panel_input panel_input_error' : 'panel_input'}
              value={v2ClientSecret}
              onChange={(e) => { setV2ClientSecret(e.target.value); clearError('v2ClientSecret'); }}
            />
            {errors.v2ClientSecret && <span className="panel_error_tooltip">{errors.v2ClientSecret}</span>}
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Authorization URL<span className="panel_required">*</span>
            </label>
            <input
              type="text"
              className={errors.v2AuthUrl ? 'panel_input panel_input_error' : 'panel_input'}
              value={v2AuthUrl}
              onChange={(e) => { setV2AuthUrl(e.target.value); clearError('v2AuthUrl'); }}
            />
            {errors.v2AuthUrl && <span className="panel_error_tooltip">{errors.v2AuthUrl}</span>}
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Access Token URL<span className="panel_required">*</span>
            </label>
            <input
              type="text"
              className={errors.v2AccessUrl ? 'panel_input panel_input_error' : 'panel_input'}
              value={v2AccessUrl}
              onChange={(e) => { setV2AccessUrl(e.target.value); clearError('v2AccessUrl'); }}
            />
            {errors.v2AccessUrl && <span className="panel_error_tooltip">{errors.v2AccessUrl}</span>}
          </div>

          <div className="panel_field">
            <label className="panel_field_label">Scopes</label>
            <textarea
              className="panel_input panel_textarea"
              value={v2Scopes}
              onChange={(e) => setV2Scopes(e.target.value)}
            />
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Access Token Placement<span className="panel_required">*</span>
            </label>
            <select
              className={errors.v2Placement ? 'panel_input panel_input_error' : 'panel_input'}
              value={v2Placement}
              onChange={(e) => { setV2Placement(e.target.value); clearError('v2Placement'); }}
            >
              <option value="" disabled>Select</option>
              <option value="header">Token in Header</option>
              <option value="query">Token in Query Param</option>
            </select>
            {errors.v2Placement && <span className="panel_error_tooltip">{errors.v2Placement}</span>}
          </div>
        </>
      )}

      {/* ===================== JWT ===================== */}
      {authMethod === 'jwt' && (
        <>
          <div className="panel_section_title panel_section_title_sub">
            JWT Configuration
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Issuer<span className="panel_required">*</span>
            </label>
            <input
              type="text"
              className={errors.jwtIssuer ? 'panel_input panel_input_error' : 'panel_input'}
              value={jwtIssuer}
              onChange={(e) => { setJwtIssuer(e.target.value); clearError('jwtIssuer'); }}
            />
            {errors.jwtIssuer && <span className="panel_error_tooltip">{errors.jwtIssuer}</span>}
          </div>

          <div className="panel_field">
            <label className="panel_field_label">Subject</label>
            <input
              type="text"
              className="panel_input"
              value={jwtSubject}
              onChange={(e) => setJwtSubject(e.target.value)}
            />
          </div>

          <div className="panel_field">
            <label className="panel_field_label">Audience</label>
            <input
              type="text"
              className="panel_input"
              value={jwtAudience}
              onChange={(e) => setJwtAudience(e.target.value)}
            />
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Secret Key<span className="panel_required">*</span>
            </label>
            <input
              type="password"
              className={errors.jwtSecret ? 'panel_input panel_input_error' : 'panel_input'}
              value={jwtSecret}
              onChange={(e) => { setJwtSecret(e.target.value); clearError('jwtSecret'); }}
            />
            {errors.jwtSecret && <span className="panel_error_tooltip">{errors.jwtSecret}</span>}
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Algorithm<span className="panel_required">*</span>
            </label>
            <select
              className={errors.jwtAlgorithm ? 'panel_input panel_input_error' : 'panel_input'}
              value={jwtAlgorithm}
              onChange={(e) => { setJwtAlgorithm(e.target.value); clearError('jwtAlgorithm'); }}
            >
              <option value="" disabled>Select</option>
              <option value="HS256">HS256</option>
              <option value="HS512">HS512</option>
              <option value="RS256">RS256</option>
            </select>
            {errors.jwtAlgorithm && <span className="panel_error_tooltip">{errors.jwtAlgorithm}</span>}
          </div>
        </>
      )}

      {/* ===================== API KEY ===================== */}
      {authMethod === 'api_key' && (
        <>
          <div className="panel_section_title panel_section_title_sub">
            API Key Parameter Configuration
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Field Label<span className="panel_required">*</span>
            </label>
            <p className="panel_field_desc">
              Label for the field that will store the API key when creating a connection
            </p>
            <input
              type="text"
              className={errors.apiFieldLabel ? 'panel_input panel_input_error' : 'panel_input'}
              value={apiFieldLabel}
              onChange={(e) => { setApiFieldLabel(e.target.value); clearError('apiFieldLabel'); }}
            />
            {errors.apiFieldLabel && <span className="panel_error_tooltip">{errors.apiFieldLabel}</span>}
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Field Type<span className="panel_required">*</span>
            </label>
            <p className="panel_field_desc">
              Choose whether the API key should be handled as plain text or as a password
            </p>
            <select
              className={errors.apiFieldType ? 'panel_input panel_input_error' : 'panel_input'}
              value={apiFieldType}
              onChange={(e) => { setApiFieldType(e.target.value); clearError('apiFieldType'); }}
            >
              <option value="" disabled>Choose Field Type</option>
              <option value="text">Plain Text</option>
              <option value="password">Password</option>
            </select>
            {errors.apiFieldType && <span className="panel_error_tooltip">{errors.apiFieldType}</span>}
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Parameter Name<span className="panel_required">*</span>
            </label>
            <p className="panel_field_desc">
              The key name that will be added to the request when sending the API key
            </p>
            <input
              type="text"
              className={errors.apiParamName ? 'panel_input panel_input_error' : 'panel_input'}
              value={apiParamName}
              onChange={(e) => { setApiParamName(e.target.value); clearError('apiParamName'); }}
            />
            {errors.apiParamName && <span className="panel_error_tooltip">{errors.apiParamName}</span>}
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              API Key Placement<span className="panel_required">*</span>
            </label>
            <p className="panel_field_desc">
              Select where the API key should be added in the request
            </p>
            <select
              className={errors.apiPlacement ? 'panel_input panel_input_error' : 'panel_input'}
              value={apiPlacement}
              onChange={(e) => { setApiPlacement(e.target.value); clearError('apiPlacement'); }}
            >
              <option value="" disabled>Choose API Key Placement</option>
              <option value="header">Header</option>
              <option value="query">Query Param</option>
            </select>
            {errors.apiPlacement && <span className="panel_error_tooltip">{errors.apiPlacement}</span>}
          </div>
        </>
      )}

      {/* ===== Enable test URL ===== */}
      <label className="panel_checkbox_item panel_test_url">
        <input
          type="checkbox"
          checked={enableTestUrl}
          onChange={(e) => setEnableTestUrl(e.target.checked)}
        />
        <span>Enable test URL</span>
      </label>
      <p className="panel_field_desc panel_test_url_desc">
        Configure a test URL to check if connections created for your app are working correctly
      </p>

      {/* ===== Footer buttons ===== */}
      <div className="panel_footer">
        <Button text="Cancel" color="black-light" onClick={onCancel} />
        <Button text={isEdit ? 'Update' : 'Save'} color="blue" onClick={handleSave} />
      </div>

    </form>
  );
}

export default AuthProfileForm;
