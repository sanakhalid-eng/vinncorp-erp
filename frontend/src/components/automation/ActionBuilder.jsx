import {
  Box,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Chip,
} from "@mui/material";
const ACTION_DETAILS = {
  UPDATE_STATUS: {
    label: "Update Status",
    desc: "Change task status to a target value",
  },
  ASSIGN_USER: {
    label: "Assign User",
    desc: "Assign task to a specific user or role",
  },
  ADD_COMMENT: { label: "Add Comment", desc: "Post a comment on the task" },
  SET_PRIORITY: { label: "Set Priority", desc: "Change task priority level" },
  SET_DUE_DATE: {
    label: "Set Due Date",
    desc: "Set a due date relative to now",
  },
  CREATE_SUBTASK: { label: "Create Subtask", desc: "Create a new subtask" },
  SEND_NOTIFICATION: {
    label: "Send Notification",
    desc: "Send a notification",
  },
  MOVE_TO_SPRINT: { label: "Move to Sprint", desc: "Move task to a sprint" },
  ADD_LABEL: { label: "Add Label", desc: "Add a label to the task" },
  ESCALATE_TASK: {
    label: "Escalate Task",
    desc: "Escalate task priority to CRITICAL",
  },
};
export default function ActionBuilder({
  actionType,
  config,
  onChange,
  onActionTypeChange,
  actionOptions,
}) {
  const updateConfig = (key, value) => {
    onChange({ ...config, [key]: value });
  };
  const selectedAction = ACTION_DETAILS[actionType];
  const renderConfigFields = () => {
    switch (actionType) {
      case "UPDATE_STATUS":
        return (
          <TextField
            fullWidth
            label="Target Status"
            value={config.targetStatus || "DONE"}
            onChange={(e) => updateConfig("targetStatus", e.target.value)}
            helperText="Enter the workflow status name to set (e.g., DONE, IN_PROGRESS, QA_REVIEW)"
          />
        );
      case "ASSIGN_USER":
        return (
          <>
             
            <TextField
              fullWidth
              label="Assignee Email"
              value={config.assigneeEmail || ""}
              onChange={(e) => updateConfig("assigneeEmail", e.target.value)}
              sx={{ mb: 2 }}
              helperText="Email of user to assign. Leave blank to use role-based assignment."
            /> 
            <TextField
              fullWidth
              label="Assignee Role"
              value={config.assigneeRole || ""}
              onChange={(e) => updateConfig("assigneeRole", e.target.value)}
              helperText="Project role to assign to (e.g., QA_REVIEWER)"
            /> 
          </>
        );
      case "ADD_COMMENT":
        return (
          <TextField
            fullWidth
            label="Comment Text"
            value={config.commentText || ""}
            onChange={(e) => updateConfig("commentText", e.target.value)}
            multiline
            rows={3}
            helperText="The comment that will be auto-added to the task"
          />
        );
      case "SET_PRIORITY":
        return (
          <FormControl fullWidth>
             
            <InputLabel>Priority</InputLabel> 
            <Select
              value={config.priority || "MEDIUM"}
              label="Priority"
              onChange={(e) => updateConfig("priority", e.target.value)}
            >
               
              <MenuItem value="LOW">Low</MenuItem> 
              <MenuItem value="MEDIUM">Medium</MenuItem> 
              <MenuItem value="HIGH">High</MenuItem> 
              <MenuItem value="CRITICAL">Critical</MenuItem> 
            </Select> 
          </FormControl>
        );
      case "SET_DUE_DATE":
        return (
          <TextField
            fullWidth
            label="Days From Now"
            type="number"
            value={config.daysFromNow || "7"}
            onChange={(e) => updateConfig("daysFromNow", e.target.value)}
            helperText="Number of days from now for the due date"
          />
        );
      case "CREATE_SUBTASK":
        return (
          <TextField
            fullWidth
            label="Subtask Title"
            value={config.subtaskTitle || ""}
            onChange={(e) => updateConfig("subtaskTitle", e.target.value)}
            helperText="Title for the auto-created subtask"
          />
        );
      case "SEND_NOTIFICATION":
        return (
          <TextField
            fullWidth
            label="Notification Message"
            value={config.message || ""}
            onChange={(e) => updateConfig("message", e.target.value)}
            multiline
            rows={2}
            helperText="Message to include in the notification"
          />
        );
      case "ADD_LABEL":
        return (
          <TextField
            fullWidth
            label="Label Name"
            value={config.label || "automated"}
            onChange={(e) => updateConfig("label", e.target.value)}
            helperText="Label to add to the task"
          />
        );
      default:
        return (
          <Typography variant="body2" color="text.secondary">
             
            No additional configuration needed for this action. 
          </Typography>
        );
    }
  };
  return (
    <Box py={2}>
       
      <Typography variant="subtitle1" gutterBottom fontWeight={600}>
        Action
      </Typography> 
      <FormControl fullWidth sx={{ mb: 2 }}>
         
        <InputLabel>Action Type</InputLabel> 
        <Select
          value={actionType}
          label="Action Type"
          onChange={(e) => onActionTypeChange(e.target.value)}
        >
           
          {actionOptions.map((key) => {
            const detail = ACTION_DETAILS[key] || { label: key, desc: "" };
            return (
              <MenuItem key={key} value={key}>
                {detail.label}
              </MenuItem>
            );
          })} 
        </Select> 
      </FormControl> 
      {selectedAction && (
        <Box mb={1}>
           
          <Typography variant="caption" color="text.secondary">
            {selectedAction.desc}
          </Typography> 
          {actionType && (
            <Chip
              label={actionType}
              size="small"
              color="primary"
              sx={{ ml: 1 }}
            />
          )} 
        </Box>
      )} 
      {actionType && (
        <Box mt={2} p={2} bgcolor="grey.50" borderRadius={1}>
           
          {renderConfigFields()} 
        </Box>
      )} 
    </Box>
  );
}
