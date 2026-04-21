import Button from '../Common/Button';
import './Card.css';

function Card({ icon, name, type, createdBy, lastUpdated, action, onEdit, onDelete }) {
  const colors = ["#303F51", "#2B4937", "#504539", "#314649", "#3F384C"];
  const randomColor = colors[Math.floor(Math.random() * colors.length)];

  return (
    <div className="card-container">
      <div className="card-title">
        <span className="card-icon" style={{ backgroundColor: randomColor }}>
          {name.split('')[0]}
        </span>
        <div className="card-name">{name}</div>
      </div>
      <div className="card-type">{type}</div>
      <div className="card-createdBy">{createdBy}</div>
      <div className="card-lastUpdated">{lastUpdated}</div>
      <div className="card-action">
        <Button text={action} color="black-light" onClick={onEdit} />
        <i className="bi bi-trash delete-icon" onClick={onDelete}></i>
      </div>
    </div>
  );
}

export default Card;
