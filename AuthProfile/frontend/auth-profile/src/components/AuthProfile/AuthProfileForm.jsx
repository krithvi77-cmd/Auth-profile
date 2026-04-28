import { useState } from 'react';
import Button from '../Common/Button';



const AUTH_ID_TO_METHOD = { 1: 'basic_auth', 2: 'oauth_v2', 3: 'api_key' };

// Server-side mask returned for password-typed defaults on profile reads.
// Must match dao.ProfileDAO.MASK_SENTINEL. Treat as "value already set,
// keep what's stored" — never echo it back, never store it locally.
const SECRET_MASK = '********';

function pickValue(initial, key, fallback = '') {
  if (!initial || !Array.isArray(initial.fields)) return fallback;
  const f = initial.fields.find(x => x.key === key && !x.isCustom);
  if (!f) return fallback;
  const raw = f.value ?? fallback;
  // For password fields the server may have masked the real value. Surface
  // an empty input to the admin instead of pre-filling dots — empty on
  // submit means "keep existing" on the server side.
  if (f.fieldType === 'password' && raw === SECRET_MASK) return '';
  return raw;
}

function hasStoredSecret(initial, key) {
  if (!initial || !Array.isArray(initial.fields)) return false;
  const f = initial.fields.find(x => x.key === key && !x.isCustom);
  if (!f) return false;
  return f.fieldType === 'password' && f.value === SECRET_MASK;
}


function pickCustom(initial) {
  if (!initial || !Array.isArray(initial.fields)) return null;
  return initial.fields.find(x => x.isCustom) || null;
}

function AuthProfileForm({ onSave, onCancel, initial = null }) {

  const isEdit = !!initial;

  function detectMethod(ini) {
    if (!ini) return 'basic_auth';
    return AUTH_ID_TO_METHOD[ini.auth_type] || 'basic_auth';
  }

  const [authMethod, setAuthMethod] = useState(detectMethod(initial));
  const [onPremises, setOnPremises] = useState(false);
  const [enableTestUrl, setEnableTestUrl] = useState(false);

  const [profileName, setProfileName] = useState(initial?.name || '');



  const [v2ClientId, setV2ClientId] = useState(pickValue(initial, 'client_id'));
  const [v2ClientSecret, setV2ClientSecret] = useState(pickValue(initial, 'client_secret'));
  // True when editing a profile whose client_secret is already stored.
  // Shown as a hint and also relaxes the "required" check on edit.
  const secretStored = hasStoredSecret(initial, 'client_secret');
  const [v2AuthUrl, setV2AuthUrl] = useState(pickValue(initial, 'authorization_url'));
  const [v2AccessUrl, setV2AccessUrl] = useState(pickValue(initial, 'access_token_url'));
  const [v2Scopes, setV2Scopes] = useState(pickValue(initial, 'scopes'));
  const [v2Placement, setV2Placement] = useState(pickValue(initial, 'token_placement'));

  const initialCustom = pickCustom(initial);
  const [apiFieldLabel, setApiFieldLabel] = useState(initialCustom?.label || '');
  const [apiFieldType, setApiFieldType] = useState(initialCustom?.fieldType || '');
  const [apiParamName, setApiParamName] = useState(initialCustom?.key || '');
  const [apiValue] = useState(initialCustom?.value || '');
  const [apiPlacement, setApiPlacement] = useState(initialCustom?.placement || '');

  const [errors, setErrors] = useState({});

  function clearError(key) {
    if (errors[key]) {
      const next = { ...errors };
      delete next[key];
      setErrors(next);
    }
  }

  function handleSave() {
    const next = {};

    if (!profileName.trim()) next.profileName = 'Auth Profile Name is required';


    if (authMethod === 'oauth_v2') {
      if (!v2ClientId.trim()) next.v2ClientId = 'Client ID is required';
      // On edit, an empty Client Secret means "keep the stored value" — the
      // server merges it correctly. Only enforce required-ness on create.
      if (!v2ClientSecret.trim() && !secretStored) {
        next.v2ClientSecret = 'Client Secret is required';
      }
      if (!v2AuthUrl.trim()) next.v2AuthUrl = 'Authorization URL is required';
      if (!v2AccessUrl.trim()) next.v2AccessUrl = 'Access Token URL is required';
      if (!v2Placement.trim()) next.v2Placement = 'Access Token Placement is required';
    }

    if (authMethod === 'api_key') {
      if (!apiFieldLabel.trim()) next.apiFieldLabel = 'Field Label is required';
      if (!apiFieldType.trim()) next.apiFieldType = 'Field Type is required';
      if (!apiParamName.trim()) next.apiParamName = 'Parameter Name is required';
      if (!apiPlacement.trim()) next.apiPlacement = 'API Key Placement is required';
    }

    setErrors(next);
    if (Object.keys(next).length > 0) return;

    let authCode = 1;
    if (authMethod === 'oauth_v2') authCode = 2;
    if (authMethod === 'api_key') authCode = 3;

    let fields = [];

    if (authMethod === 'basic_auth') {
      fields = [
        {
          key: 'username',
          value: '',
          fieldType: 'text',
          isCustom: false,
          label: 'Username',
        },
        {
          key: 'password',
          value: '',
          fieldType: 'password',
          isCustom: false,
          label: 'Password',
        },
      ];
    } else if (authMethod === 'oauth_v2') {
      fields = [
        {
          key: 'client_id',
          value: v2ClientId,
          fieldType: 'text', isCustom: false
        },
        {
          key: 'client_secret',
          value: v2ClientSecret, fieldType: 'password', isCustom: false
        },
        {
          key: 'authorization_url',
          value: v2AuthUrl, fieldType: 'text', isCustom: false
        },
        { key: 'access_token_url', value: v2AccessUrl, fieldType: 'text', isCustom: false },
        { key: 'scopes', value: v2Scopes, fieldType: 'text', isCustom: false },
        {
          key: 'token_placement', value: v2Placement,
          fieldType: 'text', isCustom: false, placement: v2Placement,
        },
      ];
    } else if (authMethod === 'api_key') {
      fields = [
        {
          key: apiParamName,
          value: apiValue,
          fieldType: apiFieldType,
          isCustom: true,
          label: apiFieldLabel,
          placement: apiPlacement,
        },
      ];
    }

    if (onSave) {
      onSave({
        name: profileName,
        auth_type: authCode,
        on_premises: onPremises,
        enable_test_url: enableTestUrl,
        fields,
      });
    }
  }

  return (
    <form onSubmit={(e) => e.preventDefault()}>

      <div className="panel_section_title">Authentication Method</div>

      <div className="panel_radio_group">
        <label className="panel_radio_item">
          <input type="radio" name="auth_method" value="basic_auth"
            checked={authMethod === 'basic_auth'}
            onChange={() => { setAuthMethod('basic_auth'); setErrors({}); }}
          />
          <span>Basic Auth</span>
        </label>
        
        <label className="panel_radio_item">
          <input type="radio" name="auth_method" value="oauth_v2"
            checked={authMethod === 'oauth_v2'}
            onChange={() => { setAuthMethod('oauth_v2'); setErrors({}); }}
          />
          <span>OAuth v2</span>
        </label>

        <label className="panel_radio_item">
          <input type="radio" name="auth_method" value="api_key"
            checked={authMethod === 'api_key'}
            onChange={() => { setAuthMethod('api_key'); setErrors({}); }}
          />
          <span>API Key</span>
        </label>
      </div>

      <label className="panel_checkbox_item">
        <input type="checkbox" checked={onPremises}
          onChange={(e) => setOnPremises(e.target.checked)} />
        <span>This app is installed on-premises</span>
        <i className="bi bi-info-circle"></i>
      </label>

      <div className="panel_field">
        <label className="panel_field_label">
          Auth Profile Name<span className="panel_required">*</span>
        </label>
        <input type="text"
          className={errors.profileName ? 'panel_input panel_input_error' : 'panel_input'}
          value={profileName}
          onChange={(e) => { setProfileName(e.target.value); clearError('profileName'); }}
        />
        {errors.profileName && <span className="panel_error_tooltip">{errors.profileName}</span>}
      </div>

      { }
      {authMethod === 'oauth_v2' && (
        <>
          <div className="panel_section_title panel_section_title_sub">
            OAuth v2 Configuration
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Client ID<span className="panel_required">*</span>
            </label>
            <input type="text"
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
            {secretStored && (
              <p className="panel_field_desc">
                A secret is already saved. Leave this empty to keep it, or
                type a new value to replace it.
              </p>
            )}
            <input type="password"
              className={errors.v2ClientSecret ? 'panel_input panel_input_error' : 'panel_input'}
              value={v2ClientSecret}
              placeholder={secretStored ? '••••••••' : ''}
              onChange={(e) => { setV2ClientSecret(e.target.value); clearError('v2ClientSecret'); }}
            />
            {errors.v2ClientSecret && <span className="panel_error_tooltip">{errors.v2ClientSecret}</span>}
          </div>

          <div className="panel_field">
            <label className="panel_field_label">
              Authorization URL<span className="panel_required">*</span>
            </label>
            <input type="text"
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
            <input type="text"
              className={errors.v2AccessUrl ? 'panel_input panel_input_error' : 'panel_input'}
              value={v2AccessUrl}
              onChange={(e) => { setV2AccessUrl(e.target.value); clearError('v2AccessUrl'); }}
            />
            {errors.v2AccessUrl && <span className="panel_error_tooltip">{errors.v2AccessUrl}</span>}
          </div>

          <div className="panel_field">
            <label className="panel_field_label">Scopes</label>
            <textarea className="panel_input panel_textarea"
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

      { }
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
              Label shown to the user in the Connection screen
            </p>
            <input type="text"
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
              Controls the input type rendered in the Connection screen
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
              Key sent in the request (e.g. "x-api-key", "api_key")
            </p>
            <input type="text"
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
              Where the API key is attached on outgoing requests
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

      <label className="panel_checkbox_item panel_test_url">
        <input type="checkbox" checked={enableTestUrl}
          onChange={(e) => setEnableTestUrl(e.target.checked)} />
        <span>Enable test URL</span>
      </label>
      <p className="panel_field_desc panel_test_url_desc">
        Configure a test URL to check if connections created for your app are working correctly
      </p>

      <div className="panel_footer">
        <Button text="Cancel" color="black-light" onClick={onCancel} />
        <Button text={isEdit ? 'Update' : 'Save'} color="blue" onClick={handleSave} />
      </div>

    </form>
  );
}

export default AuthProfileForm;
