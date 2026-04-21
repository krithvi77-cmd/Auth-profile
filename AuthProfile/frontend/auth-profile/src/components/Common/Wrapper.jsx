import './Wrapper.css'
import SideBar from './SideBar';
import SidePanel from './SidePanel';

function Wrapper({ children, activeView, onSelectView }) {
  return (
    <div className="main">
      <SideBar />
      <SidePanel activeView={activeView} onSelectView={onSelectView} />
      {children}
    </div>
  )
}

export default Wrapper
