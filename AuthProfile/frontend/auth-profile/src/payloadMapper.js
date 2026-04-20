// ----------------------------------------------------------------
// Converts the flat payload produced by AuthProfileForm into the
// shape the Java backend expects (AuthProfile + child fields[]).
//
// UI payload  :  { name, auth_type, values: { key: value, ... } }
// API payload :  { name, authType, fields: [ { key, label, fieldType,
//                                              defaultValue, position,
//                                              isSecret, isRequired } ] }
// ----------------------------------------------------------------

// ----- label map: maps snake_case keys back to nice labels -----
const LABELS = {
  username:           'Username',
  password:           'Password',
  client_id:          'Client ID',
  client_secret:      'Client Secret',
  authorization_url:  'Authorization URL',
  access_token_url:   'Access Token URL',
  request_token_url:  'Request Token URL',
  scopes:             'Scopes',
  token_placement:    'Access Token Placement',
  issuer:             'Issuer',
  subject:            'Subject',
  audience:           'Audience',
  secret_key:         'Secret Key',
  algorithm:          'Algorithm',
  field_label:        'Field Label',
  field_type:         'Field Type',
  parameter_name:     'Parameter Name',
  api_key_placement:  'API Key Placement',
};

// ----- which keys should be stored as secret / password in DB -----
const SECRETS = new Set([
  'password', 'client_secret', 'secret_key',
]);

export function toApiPayload(ui) {
  const fields = Object.entries(ui.values || {}).map(([key, value], i) => ({
    key,
    label:        LABELS[key] || key,
    fieldType:    SECRETS.has(key) ? 'password' : 'text',
    defaultValue: value,
    position:     i + 1,
    required:     true,
    secret:       SECRETS.has(key),
  }));

  return {
    name:     ui.name,
    authType: ui.auth_type,
    fields,
  };
}

// ----- reverse direction (API -> UI) for edit mode -----
export function toUiPayload(api) {
  const values = {};
  (api.fields || []).forEach(f => { values[f.key] = f.defaultValue || ''; });
  return {
    id:        api.id,
    name:      api.name,
    auth_type: api.authType,
    values,
  };
}

// ----- friendly name for the auth type id -----
export function authTypeName(id) {
  return {
    1: 'Basic Auth',
    2: 'OAuth v2.0',
    3: 'API Key',
    4: 'JWT',
    5: 'No Auth',
  }[id] || 'Unknown';
}
