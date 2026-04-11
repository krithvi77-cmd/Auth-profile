import './Button.css';

function Button({ text, onClick, color }) {
  return (
    <button className= {`custom-button ${color}`} onClick={onClick}>
      {text}
    </button>
  );
}

export default Button;