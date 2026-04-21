import { useEffect, useState, useCallback } from 'react'
import './App.css'
import './components/Common/Panel.css'

import Header from './components/Common/Header'
import Wrapper from './components/Common/Wrapper'
import Panel from './components/Common/Panel'
import AuthProfile from './components/AuthProfile/AuthProfile'
import AuthProfileForm from './components/AuthProfile/AuthProfileForm'
import Connection from './components/Connection/Connection'

import { profileApi } from './api'
import { toApiPayload, toUiPayload } from './payloadMapper'

function App() {
  const [profiles, setProfiles]   = useState([])
  const [panelOpen, setPanelOpen] = useState(false)
  const [editing, setEditing]     = useState(null)
  const [error, setError]         = useState('')

  /* Which sidebar item is active: 'auth_profile' | 'connection' */
  const [activeView, setActiveView] = useState('auth_profile')

  const reload = useCallback(async () => {
    try {
      const data = await profileApi.list()
      setProfiles(data)
      setError('')
    } catch (e) {
      setError(e.message)
    }
  }, [])

  useEffect(() => { reload() }, [reload])

  const openCreate = () => { setEditing(null); setPanelOpen(true) }

  const openEdit = async (p) => {
    try {
      const fresh = await profileApi.get(p.id)
      setEditing(toUiPayload(fresh))
      setPanelOpen(true)
    } catch (e) {
      alert('Could not load profile: ' + e.message)
    }
  }

  const closePanel = () => { setPanelOpen(false); setEditing(null) }

  const handleSave = async (payload) => {
    try {
      const apiPayload = toApiPayload(payload)
      if (editing) {
        await profileApi.update(editing.id, apiPayload)
      } else {
        await profileApi.create(apiPayload)
      }
      closePanel()
      reload()
    } catch (e) {
      alert('Save failed: ' + e.message)
    }
  }

  const handleDelete = async (p) => {
    if (!window.confirm(`Delete "${p.name}"?`)) return
    try {
      await profileApi.remove(p.id)
      reload()
    } catch (e) {
      alert('Delete failed: ' + e.message)
    }
  }

  /* Connection-specific actions */
  const handleShare = (p) => {
    alert(`Share: ${p.name}`)
  }
  const handleTest = (p) => {
    alert(`Test connection: ${p.name}`)
  }
  const handleReconnect = (p) => {
    openEdit(p)
  }

  /* Handle sidebar switch.
     Only two views are currently implemented: 'auth_profile' and 'connection'.
     Any other values are ignored (stay on current view). */
  const handleSelectView = (value) => {
    if (value === 'auth_profile' || value === 'connection') {
      setActiveView(value)
    }
  }

  return (
    <div className="background">
      <div className="base_panel">
        <Header />
        <Wrapper activeView={activeView} onSelectView={handleSelectView}>
          {activeView === 'connection' ? (
            <Connection
              profiles={profiles}
              error={error}
              onCreate={openCreate}
              onShare={handleShare}
              onTest={handleTest}
              onReconnect={handleReconnect}
            />
          ) : (
            <AuthProfile
              profiles={profiles}
              error={error}
              onCreate={openCreate}
              onEdit={openEdit}
              onDelete={handleDelete}
            />
          )}
        </Wrapper>
      </div>

      {panelOpen && (
        <div className="first_layer">
          <div className="panel-tint" />
          <Panel onClose={closePanel}>
            <AuthProfileForm
              initial={editing}
              onSave={handleSave}
              onCancel={closePanel}
            />
          </Panel>
        </div>
      )}
    </div>
  )
}

export default App
