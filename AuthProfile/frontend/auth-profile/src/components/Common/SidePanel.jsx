import "./SidePanel.css";

function SidePanel({ activeView = "auth_profile", onSelectView }) {
  const handleSelect = (value) => {
    if (onSelectView) onSelectView(value);
  };

  const config = {
    General: {
      title: "General",
      groups: [
        { label: "Org Profile", value: "org_profile" },
        { label: "Members", value: "members" },
        { label: "Support Access", value: "support_access" },
        { label: "Notification", value: "notification" },
        { label: "Billing & Usage", value: "bill_usage" },
      ],
    },
    FlowSetup: {
      title: "Flow Setup",
      groups: [
        { label: "Connection", value: "connection" },
        { label: "Auth Profile", value: "auth_profile" },
      ],
    },
  };

  return (
    <div className="sidePanel">
      {Object.entries(config).map(([key, section]) => (
        <div className="sidePanel_group" key={key}>
          <h3 className="sidePanel_title">{section.title}</h3>

          <ul className="sidePanel_lists">
            {section.groups.map((group) => (
              <li
                key={group.value}
                className={
                  activeView === group.value
                    ? "sidePanel_list active"
                    : "sidePanel_list"
                }
                onClick={() => handleSelect(group.value)}
              >
                {group.label}
              </li>
            ))}
          </ul>
        </div>
      ))}
    </div>
  );
}

export default SidePanel;
