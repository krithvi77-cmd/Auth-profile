import './Panel.css';

function Panel({
  title = 'Auth Profiles',
  subtitle = 'Integrate with applications using HTTP methods',
  description = {
    title: 'Auth Profiles',
    text:
      "Save your app's authentication details in an Auth Profile to create " +
      'connections. Once set up, you can reuse the profile across connections ' +
      'to send webhooks to your app.',
  },
  onClose,
  children,
}) {
  return (
    <div className="panel">
      <div className="panel_header">
        <div className="panel_logo" />
        <div className="panel_header_content">
          <h3>{title}</h3>
          <p>{subtitle}</p>
        </div>
        <i className="bi bi-x-lg" onClick={onClose}></i>
      </div>

      <div className="panel_container">
        <aside className="panel_description">
          <div className="panel_desc_icon" />
          <h4 className="panel_desc_title">{description.title}</h4>
          <p className="panel_desc_text">{description.text}</p>
        </aside>

        <div className="panel_configurable">{children}</div>
      </div>
    </div>
  );
}

export default Panel;
