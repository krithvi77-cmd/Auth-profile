import Card from './Card'
import './Display.css'
import { authTypeName } from '../../payloadMapper'

function Display({ profiles = [], onEdit, onDelete }) {
  return (
    <div className="display">
      <div className="display-header">
        <span className="display-title">Auth Profile Name</span>
        <span className="display-actions">Auth type</span>
        <span className="display-actions">Created by</span>
        <span className="display-actions">Last updated on</span>
        <span className="display-actions">Action</span>
      </div>
      <div className="display-body">
        {profiles.length === 0 && (
          <div className="display-empty">No auth profiles yet. Click <b>Create</b> to add one.</div>
        )}
        {profiles.map(p => (
          <Card
            key={p.id}
            icon={<i className="bi bi-shield-lock"></i>}
            name={p.name}
            type={authTypeName(p.authType)}
            createdBy={p.createdBy ?? '—'}
            lastUpdated={(p.createdAt || '').split(' ')[0] || '—'}
            action="Edit"
            onEdit={() => onEdit && onEdit(p)}
            onDelete={() => onDelete && onDelete(p.id)}
          />
        ))}
      </div>
    </div>
  )
}

export default Display
