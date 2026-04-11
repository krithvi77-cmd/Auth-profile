import './Wrapper.css'
import SideBar from './SideBar';
import AuthProfile from '../AuthProfile/AuthProfile';


function Wrapper(){
  return (
    <div className="main">
      <SideBar/>
      {/* <SidePanel/> */}
      <AuthProfile/>
    </div>
  )
}

export default Wrapper