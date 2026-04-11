import Button from './Button';
import './Header.css';




function Header() {
  return (
    <div className="header">
      <div className="header-left">
           <div className="logo">
             <div className="logo-img"></div>
           </div>
           Flow
      </div>
          <div className="header-right">
            <Button text="Explore Gallery" onClick={() => redirect('/gallery')} color="blue" />
            <div className="org">newBegin <span><i class="bi bi-chevron-down"></i></span></div>
            <div className="subscription">Trail expires in 10 days <span>Upgrade</span></div>
            <i className="bi bi-bell"></i>
            <div className="profile-image">
        </div>
      </div>
    </div>
  )
}

export default Header