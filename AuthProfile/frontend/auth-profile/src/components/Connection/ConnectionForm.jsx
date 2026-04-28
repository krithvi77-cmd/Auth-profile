import { useMemo, useState } from 'react';
import Button from '../Common/Button';
import { authTypeName } from '../../payloadMapper';

const AUTH_ID_TO_METHOD = { 1: 'basic_auth', 2: 'oauth_v2', 3: 'api_key' };


function ConnectionForm({ profile, editing, onSave, onCancel }) {
    const method = AUTH_ID_TO_METHOD[profile.authType ?? profile.auth_type] || 'basic_auth';
    const isEdit = !!(editing && editing.id);

    const [connectionName, setConnectionName] = useState(
        isEdit
            ? (editing.name || '')
            : (profile.name ? `${profile.name} - connection` : '')
    );

    const userFields = useMemo(() => {
        const all = profile.fields || [];

        if (method === 'basic_auth') {
            return [
                { key: 'username', label: 'Username', fieldType: 'text',     required: true },
                { key: 'password', label: 'Password', fieldType: 'password', required: true },
            ];
        }

        if (method === 'api_key') {
            const customs = all.filter(f => f.isCustom);
            if (customs.length === 0) {
                return [
                    { key: 'api_key_value', label: 'API Key', fieldType: 'password', required: true },
                ];
            }
            return customs.map(f => ({
                key:       f.key,
                label:     f.label || f.key,
                fieldType: f.fieldType || 'text',
                required:  true,
            }));
        }

        return [];
    }, [profile, method]);

    const [values, setValues] = useState(() => {
        const init = {};
        userFields.forEach(f => { init[f.key] = ''; });
        return init;
    });
    const [errors, setErrors] = useState({});

    const setField = (key, val) => {
        setValues(prev => ({ ...prev, [key]: val }));
        if (errors[key]) {
            const next = { ...errors };
            delete next[key];
            setErrors(next);
        }
    };

    const handleSave = () => {
        const next = {};
        if (!connectionName.trim()) next.connectionName = 'Connection name is required';

        userFields.forEach(f => {
            if (f.required && !(values[f.key] || '').trim()) {
                next[f.key] = `${f.label} is required`;
            }
        });

        setErrors(next);
        if (Object.keys(next).length > 0) return;

        onSave && onSave({
            name:          connectionName.trim(),
            authProfileId: profile.id,
            values,
        });
    };

    return (
        <form onSubmit={(e) => e.preventDefault()}>

            <div className="panel_section_title">
                Connection Details
                <span style={{ float: 'right', fontWeight: 400, fontSize: 12, color: '#8A8A8A' }}>
                    Using auth profile: <b style={{ color: '#CECECE' }}>{profile.name}</b>
                    {' '}({authTypeName(profile.authType ?? profile.auth_type)})
                </span>
            </div>

            <div className="panel_field">
                <label className="panel_field_label">
                    Connection Name<span className="panel_required">*</span>
                </label>
                <input
                    type="text"
                    className={errors.connectionName ? 'panel_input panel_input_error' : 'panel_input'}
                    value={connectionName}
                    onChange={(e) => {
                        setConnectionName(e.target.value);
                        if (errors.connectionName) {
                            const n = { ...errors }; delete n.connectionName; setErrors(n);
                        }
                    }}
                />
                {errors.connectionName && (
                    <span className="panel_error_tooltip">{errors.connectionName}</span>
                )}
            </div>

            {userFields.length > 0 && (
                <div className="panel_section_title panel_section_title_sub">
                    Credentials
                </div>
            )}

            {userFields.map(f => (
                <div className="panel_field" key={f.key}>
                    <label className="panel_field_label">
                        {f.label}
                        {f.required && <span className="panel_required">*</span>}
                    </label>
                    <input
                        type={f.fieldType === 'password' ? 'password' : 'text'}
                        className={errors[f.key] ? 'panel_input panel_input_error' : 'panel_input'}
                        value={values[f.key] || ''}
                        onChange={(e) => setField(f.key, e.target.value)}
                        autoComplete="off"
                    />
                    {errors[f.key] && (
                        <span className="panel_error_tooltip">{errors[f.key]}</span>
                    )}
                </div>
            ))}

            {method === 'oauth_v2' && (
                <div className="panel_field">
                    <p className="panel_field_desc">
                        {isEdit
                            ? "You'll be redirected to the provider again to refresh authorisation. The existing connection will be reused — no new connection is created."
                            : "You'll be redirected to the provider to sign in and authorise access. Tokens are fetched automatically — no need to enter them here."}
                    </p>
                </div>
            )}

            <div className="panel_footer">
                <Button text="Cancel" color="black-light" onClick={onCancel} />
                <Button
                    text={
                        method === 'oauth_v2'
                            ? (isEdit ? 'Reconnect' : 'Connect')
                            : (isEdit ? 'Update' : 'Save')
                    }
                    color="blue"
                    onClick={handleSave}
                />
            </div>
        </form>
    );
}

export default ConnectionForm;
