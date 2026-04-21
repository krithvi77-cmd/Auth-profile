import { useMemo, useState } from 'react';
import Button from '../Common/Button';
import './AuthProfilePicker.css';

function AuthProfilePicker({ profiles = [], onCancel, onNext }) {
    const [search, setSearch] = useState('');
    const [selectedId, setSelectedId] = useState(null);

    const filtered = useMemo(() => {
        const q = search.trim().toLowerCase();
        if (!q) return profiles;
        return profiles.filter(p => (p.name || '').toLowerCase().includes(q));
    }, [profiles, search]);

    const colors = ['#303F51', '#2B4937', '#504539', '#314649', '#3F384C', '#4A3548', '#3A4A2E'];
    const bgFor = (name = '') => {
        const code = name.charCodeAt(0) || 65;
        return colors[code % colors.length];
    };

    const handleNext = () => {
        const chosen = profiles.find(p => p.id === selectedId);
        if (!chosen) return;
        onNext && onNext(chosen);
    };

    return (
        <div className="picker">
            <div className="picker_header">
                <div className="picker_logo">
                    <i className="bi bi-plug-fill"></i>
                </div>
                <div className="picker_header_content">
                    <h3>Choose an app to create connection</h3>
                </div>
                <i className="bi bi-x-lg picker_close" onClick={onCancel}></i>
            </div>

            <div className="picker_body">
                <div className="picker_search">
                    <i className="bi bi-search"></i>
                    <input
                        type="text"
                        placeholder="Search apps here"
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>

                <div className="picker_section_title">Auth Profiles</div>

                {filtered.length === 0 ? (
                    <div className="picker_empty">
                        No auth profiles found. Create one from the <b>Auth Profile</b> screen first.
                    </div>
                ) : (
                    <div className="picker_grid">
                        {filtered.map(p => {
                            const initial = (p.name || '?').charAt(0).toUpperCase();
                            const isActive = selectedId === p.id;
                            return (
                                <div
                                    key={p.id}
                                    className={isActive ? 'picker_card picker_card_active' : 'picker_card'}
                                    onClick={() => setSelectedId(p.id)}
                                >
                                    <div
                                        className="picker_card_icon"
                                        style={{ backgroundColor: bgFor(p.name) }}
                                    >
                                        {initial}
                                    </div>
                                    <div className="picker_card_name">{p.name}</div>
                                </div>
                            );
                        })}
                    </div>
                )}
            </div>

            <div className="picker_footer">
                <Button text="Cancel" color="black-light" onClick={onCancel} />
                <Button
                    text="Next"
                    color={selectedId ? 'blue' : 'grey'}
                    onClick={selectedId ? handleNext : undefined}
                />
            </div>
        </div>
    );
}

export default AuthProfilePicker;
