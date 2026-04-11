import Card from './Card'
import './Display.css'

function Display(){
    return (
        <div className="display">
           <div className="display-header">
              <span className="display-title">Auth Profile Name</span>
              <span className="display-actions">Auth type</span>
              <span className="display-actions">Created by</span>
              <span className="display-actions">Last updated on</span>
              <span className="display-actions">Action</span>
           </div>
           <div className="display-body">
                <Card icon={<i class="bi bi-github"></i>} name="Zoho projects Profile" type="OAuth" createdBy="krithvi" lastUpdated="12-04-2026" action="Edit"/>
                <Card icon={<i class="bi bi-slack"></i>} name="Exodus" type="API Key" createdBy="shai" lastUpdated="12-04-2026" action="Edit"/>
                <Card icon={<i class="bi bi-google"></i>} name="Flow " type="OAuth" createdBy="krithvi shai" lastUpdated="15-04-2026" action="Edit"/>
            </div>
        </div>
    )
}

export default Display