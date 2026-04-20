import './Wrapper.css'
import SideBar from './SideBar';

function Wrapper({ children }) {
  return (
    <div className="main">
      <SideBar />
      {children}
    </div>
  )
}

export default Wrapper
