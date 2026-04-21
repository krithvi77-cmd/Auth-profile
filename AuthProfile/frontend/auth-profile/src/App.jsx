import { useEffect, useState, useCallback } from 'react'
import './App.css'
import './components/Common/Panel.css'

import Header from './components/Common/Header'
import Wrapper from './components/Common/Wrapper'
import Panel from './components/Common/Panel'
import AuthProfile from './components/AuthProfile/AuthProfile'
import AuthProfileForm from './components/AuthProfile/AuthProfileForm'
import Connection from './components/Connection/Connection'
import AuthProfilePicker from './components/Connection/AuthProfilePicker'
import ConnectionForm from './components/Connection/ConnectionForm'

import { profileApi, connectionApi } from './api'
import { toApiPayload, toUiPayload } from './payloadMapper'

function App() {
  const [profiles, setProfiles]   = useState([])
  const [panelOpen, setPanelOpen] = useState(false)
  const [editing, setEditing]     = useState(null)
  const [error, setError]         = useState('')

 
  const [activeView, setActiveView] = useState('auth_profile')


  const [connectionStep, setConnectionStep]       = useState(null)
  const [selectedAuthProfile, setSelectedAuthProfile] = useState(null)

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

  const openCreate = () => {
    if (activeView === 'connection') {
    
      setConnectionStep('pick')
      return
    }
    setEditing(null)
    setPanelOpen(true)
  }

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


  const closeConnectionFlow = () => {
    setConnectionStep(null)
    setSelectedAuthProfile(null)
  }

  const handlePickNext = async (profile) => {

    try {
      const fresh = await profileApi.get(profile.id)
      setSelectedAuthProfile(fresh)
      setConnectionStep('form')
    } catch (e) {
      alert('Could not load auth profile: ' + e.message)
    }
  }

  const handleConnectionSave = async (payload) => {
    // Build the final JSON body for POST /api/connection
    const body = {
      name:          payload.name,
      authProfileId: payload.authProfileId,
      fields: Object.entries(payload.values || {}).map(([key, value]) => ({
        key,
        value,
      })),
    }

    try {
      console.log('[Connection] POST /api/connection →', body)
      const created = await connectionApi.create(body)
      console.log('[Connection] created:', created)
      closeConnectionFlow()
      reload()
    } catch (e) {
      alert('Connection save failed: ' + e.message)
    }
  }

  const handleShare     = (p) => { alert(`Share: ${p.name}`) }
  const handleTest      = (p) => { alert(`Test connection: ${p.name}`) }
  const handleReconnect = (p) => { openEdit(p) }

 
  const handleSelectView = (value) => {
    if (value === 'auth_profile' || value === 'connection') {
      setActiveView(value)
     
      closePanel()
      closeConnectionFlow()
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

      
      {connectionStep === 'pick' && (
        <div className="first_layer">
          <div className="panel-tint" />
          <AuthProfilePicker
            profiles={profiles}
            onCancel={closeConnectionFlow}
            onNext={handlePickNext}
          />
        </div>
      )}

      
      {connectionStep === 'form' && selectedAuthProfile && (
        <div className="first_layer">
          <div className="panel-tint" />
          <Panel
            title="Create Connection"
            subtitle={`Using "${selectedAuthProfile.name}" auth profile`}
            description={{
              title: 'Connection',
              text:
                "Fill in the credentials below. These values are stored securely " +
                "and used whenever this connection sends a request to the target app.",
            }}
            onClose={closeConnectionFlow}
          >
            <ConnectionForm
              profile={selectedAuthProfile}
              onSave={handleConnectionSave}
              onCancel={closeConnectionFlow}
            />
          </Panel>
        </div>
      )}
    </div>
  )
}

export default App
