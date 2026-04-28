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
  const [profiles, setProfiles]       = useState([])
  const [connections, setConnections] = useState([])
  const [panelOpen, setPanelOpen] = useState(false)
  const [editing, setEditing]     = useState(null)
  const [error, setError]         = useState('')

 
  const [activeView, setActiveView] = useState('auth_profile')


  const [connectionStep, setConnectionStep]       = useState(null)
  const [selectedAuthProfile, setSelectedAuthProfile] = useState(null)

  const reloadProfiles = useCallback(async () => {
    try {
      const data = await profileApi.list()
      setProfiles(data)
      setError('')
    } catch (e) {
      setError(e.message)
    }
  }, [])

  const reloadConnections = useCallback(async () => {
    try {
      const data = await connectionApi.list()
      setConnections(data)
      setError('')
    } catch (e) {
      setError(e.message)
    }
  }, [])

  const reload = useCallback(async () => {
    await Promise.all([reloadProfiles(), reloadConnections()])
  }, [reloadProfiles, reloadConnections])

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
    setEditing(null)
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

    const body = {
      name:          payload.name,
      authProfileId: payload.authProfileId,
      values:        payload.values || {},
    }

    try {
      let saved
      if (editing && editing.id) {
        // Existing connection → this is a reconnect (UC-3), not an "edit".
        // Route through the reconnect endpoint so the server applies the
        // partial-update + status-flip semantics, not the legacy
        // create-shaped update().  Note: OAuth never reaches this code
        // path — handleReconnect short-circuits before opening the form.
        console.log('[Connection] POST /api/connection/' + editing.id + '/reconnect →', body.values)
        saved = await connectionApi.reconnect(editing.id, body.values || {})
        console.log('[Connection] reconnected:', saved)
      } else {
        console.log('[Connection] POST /api/connection →', body)
        saved = await connectionApi.create(body)
        console.log('[Connection] created:', saved)
      }


      if (saved && saved.authorizeUrl) {
        const popup = window.open(
          saved.authorizeUrl,
          'oauth_authorize',
          'width=600,height=700'
        )
        if (!popup) {
          alert('Please allow popups to complete the OAuth connection.')
          return
        }

        const onMessage = (event) => {
          const data = event.data
          if (!data || data.type !== 'oauth_result') return
          window.removeEventListener('message', onMessage)
          if (data.ok) {
            closeConnectionFlow()
            reload()
          } else {
            alert('Authorisation failed: ' + (data.message || 'unknown error'))
          }
        }
        window.addEventListener('message', onMessage)
        return
      }

      closeConnectionFlow()
      reload()
    } catch (e) {
      alert('Connection save failed: ' + e.message)
    }
  }

  const handleShare     = (p) => { alert(`Share: ${p.name}`) }
  const handleTest      = (p) => { alert(`Test connection: ${p.name}`) }

  // Reconnect flow (UC-3 in SPEC):
  //   - OAuth profiles: no form. Hit the reconnect endpoint directly. The
  //     server will either silently refresh tokens (returns {refreshed:true})
  //     or hand back an authorizeUrl that we open in a popup. Either way,
  //     the same connection_id is reused; no new connection_oauth_values row.
  //   - Non-OAuth (basic / api key): the user must retype credentials, so
  //     load the connection + profile and open the form. Form submit will
  //     route through connectionApi.reconnect() (see handleConnectionSave).
  const handleReconnect = async (p) => {
    try {
      if (!p?.id) {
        alert('Reconnect failed: missing connection id')
        return
      }

      const full = await connectionApi.get(p.id)
      if (!full) {
        alert('Reconnect failed: connection not found')
        return
      }
      const profile = await profileApi.get(full.authProfileId)
      const isOauth = (profile?.authType ?? profile?.auth_type) === 2

      if (isOauth) {
        // No form, no payload. Server decides: refresh-token vs re-authorize.
        const result = await connectionApi.reconnect(p.id, {})
        if (result?.authorizeUrl) {
          // Refresh-token unavailable / rejected → open popup to re-authorize.
          // Same connection_id is bound to the new authorize state, so the
          // callback will UPSERT onto the existing oauth row.
          const popup = window.open(
            result.authorizeUrl,
            'oauth_authorize',
            'width=600,height=700'
          )
          if (!popup) {
            alert('Please allow popups to complete the OAuth re-authorisation.')
            return
          }
          const onMessage = (event) => {
            const data = event.data
            if (!data || data.type !== 'oauth_result') return
            window.removeEventListener('message', onMessage)
            if (data.ok) {
              reload()
            } else {
              alert('Re-authorisation failed: ' + (data.message || 'unknown error'))
            }
          }
          window.addEventListener('message', onMessage)
          return
        }
        // Silent path: tokens were either still valid (no refresh
        // performed) or refresh-token grant succeeded in place.
        // Always surface feedback — without it, clicking Reconnect on
        // a STILL_VALID connection looks like the button is broken.
        if (result?.refreshed) {
          alert('Connection refreshed.')
        } else {
          alert('Connection is already active — no re-authorisation needed.')
        }
        reload()
        return
      }

      // Non-OAuth: open the form so the user can retype credentials.
      setSelectedAuthProfile(profile)
      setEditing(full)
      setConnectionStep('form')
    } catch (e) {
      alert('Reconnect failed: ' + e.message)
    }
  }

  const handleDeleteConnection = async (p) => {
    if (!window.confirm(`Delete connection "${p.name}"?`)) return
    try {
      await connectionApi.remove(p.id)
      reloadConnections()
    } catch (e) {
      alert('Delete failed: ' + e.message)
    }
  }

 
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
              connections={connections}
              error={error}
              onCreate={openCreate}
              onShare={handleShare}
              onTest={handleTest}
              onReconnect={handleReconnect}
              onDelete={handleDeleteConnection}
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
            title={editing ? 'Reconnect Connection' : 'Create Connection'}
            subtitle={`Using "${selectedAuthProfile.name}" auth profile`}
            description={{
              title: 'Connection',
              text: editing
                ? "Re-enter the credentials below to refresh this connection. The connection id stays the same."
                : "Fill in the credentials below. These values are stored securely " +
                  "and used whenever this connection sends a request to the target app.",
            }}
            onClose={closeConnectionFlow}
          >
            <ConnectionForm
              profile={selectedAuthProfile}
              editing={editing}
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
