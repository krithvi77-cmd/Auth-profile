
const BASIC_AUTH = 1;

export function toApiPayload(ui) {
  const sourceFields = [...(ui.fields || [])];

  if (ui.auth_type === BASIC_AUTH) {
    const hasKey = (k) => sourceFields.some(
      f => (f.key || '').toLowerCase() === k
    );
    if (!hasKey('username')) {
      sourceFields.push({
        key: 'username',
        label: 'Username',
        fieldType: 'text',
        value: '',
        isCustom: false,
      });
    }
    if (!hasKey('password')) {
      sourceFields.push({
        key: 'password',
        label: 'Password',
        fieldType: 'password',
        value: '',
        isCustom: false,
      });
    }
  }

  const fields = sourceFields.map((f, i) => {
    const out = {
      key:          f.key,
      fieldType:    f.fieldType || 'text',
      defaultValue: f.value ?? '',
      isCustom:     !!f.isCustom,
      position:     i + 1,
    };
   
    if (f.isCustom && f.label) {
      out.label = f.label;
    }
    
    if (f.placement) {
      out.placement = f.placement;
    }
    return out;
  });

  return {
    name:     ui.name,
    authType: ui.auth_type,
    fields,
  };
}


export function toUiPayload(api) {
  const fields = (api.fields || []).map(f => ({
    key:       f.key,
    value:     f.defaultValue ?? '',
    fieldType: f.fieldType || 'text',
    isCustom:  !!f.isCustom,
    label:     f.label || null,        
    placement: f.placement || null,
  }));

  return {
    id:        api.id,
    name:      api.name,
    auth_type: api.authType,
    fields,
  };
}

export function authTypeName(id) {
  return {
    1: 'Basic Auth',
    2: 'OAuth v2.0',
    3: 'API Key',
    4: 'JWT',
    5: 'No Auth',
  }[id] || 'Unknown';
}
