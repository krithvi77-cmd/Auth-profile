import './SideBar.css'

function SideBar() {
  return (
    <div className="sidebar">
      <div className="sidebar-item"><i class="bi bi-speedometer2"></i>Dashboard</div>
      <div className="sidebar-item"><i class="bi bi-list-task"></i>My Flows</div>
      <div className="sidebar-item active"><i class="bi bi-gear"></i>Settings</div>
    </div>
  )
}

export default SideBar