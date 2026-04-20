import { useEffect, useState, useCallback } from 'react'
import './App.css'

import Header from './components/Common/Header'
import Wrapper from './components/Common/Wrapper'
import Panel from './components/Common/Panel'
import AuthProfileForm from './components/AuthProfile/AuthProfileForm'
import AuthProfile from './components/AuthProfile/AuthProfile'

import { profileApi } from './api'
import { toApiPayload, toUiPayload } from './payloadMapper'

function App() {
  const [profiles, setProfiles]     = useState([])
  const [panelOpen, setPanelOpen]   = useState(false)
  const [editing, setEditing]       = useState(null)   // profile being edited, or null
  const [error, setError]           = useState('')

  // ---------- load ----------
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

  // ---------- handlers ----------
  const openCreate = () => { setEditing(null);  setPanelOpen(true) }
  const openEdit   = async (p) => {
    try {
      // Always refetch so the form has the latest fields/values
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
      if (editing) await profileApi.update(editing.id, apiPayload)
      else         await profileApi.create(apiPayload)
      closePanel()
      reload()
    } catch (e) {
      alert('Save failed: ' + e.message)
    }
  }

  const handleDelete = async (id) => {
    if (!confirm('Delete this auth profile?')) return
    try {
      await profileApi.remove(id)
      reload()
    } catch (e) {
      alert('Delete failed: ' + e.message)
    }
  }

  return (
    <div className="background">
      <div className="base_panel">
        <Header />
        <Wrapper>
          <AuthProfile
            profiles={profiles}
            error={error}
            onCreate={openCreate}
            onEdit={openEdit}
            onDelete={handleDelete}
          />
        </Wrapper>
      </div>

      {panelOpen && (
        <div className="first_layer">
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
