import './App.css'
import Header from './components/Common/Header'
import Wrapper from './components/Common/Wrapper'
import Panel from  './components/Common/Panel'

function App() {
  return (
    <div className="background">
      <div className ="base_panel">
      <Header/>
      <Wrapper/>
      </div>
      <div className = "first_layer">
        <Panel/>
      </div>
     </div>
  )
}

export default App