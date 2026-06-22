import { Box, Typography, Card, CardContent, Chip } from "@mui/material";
const TRIGGER_DETAILS = {
  TASK_CREATED: {
    label: "Task Created",
    description: "When a new task is created",
    icon: "≡ƒô¥",
    color: "primary",
  },
  TASK_UPDATED: {
    label: "Task Updated",
    description: "When a task is modified",
    icon: "Γ£Å∩╕Å",
    color: "info",
  },
  TASK_COMPLETED: {
    label: "Task Completed",
    description: "When a task is marked done",
    icon: "Γ£à",
    color: "success",
  },
  TASK_ASSIGNED: {
    label: "Task Assigned",
    description: "When a task is assigned to a user",
    icon: "≡ƒæñ",
    color: "secondary",
  },
  TASK_OVERDUE: {
    label: "Task Overdue",
    description: "When a task passes its due date",
    icon: "ΓÜá∩╕Å",
    color: "warning",
  },
  SPRINT_STARTED: {
    label: "Sprint Started",
    description: "When a sprint begins",
    icon: "≡ƒÅâ",
    color: "primary",
  },
  SPRINT_COMPLETED: {
    label: "Sprint Completed",
    description: "When a sprint ends",
    icon: "≡ƒÅü",
    color: "success",
  },
  DEPENDENCY_BLOCKED: {
    label: "Dependency Blocked",
    description: "When a task is blocked by a dependency",
    icon: "≡ƒöÆ",
    color: "error",
  },
  DEPENDENCY_UNBLOCKED: {
    label: "Dependency Unblocked",
    description: "When a dependency is resolved",
    icon: "≡ƒöô",
    color: "success",
  },
  COMMENT_ADDED: {
    label: "Comment Added",
    description: "When a comment is posted on a task",
    icon: "≡ƒÆ¼",
    color: "info",
  },
  TIMELOG_CREATED: {
    label: "Time Log Created",
    description: "When time is logged against a task",
    icon: "ΓÅ▒∩╕Å",
    color: "info",
  },
};
export default function TriggerSelector({ value, onChange, options }) {
  return (
    <Box py={2}>
       
      <Typography variant="subtitle1" gutterBottom fontWeight={600}>
        Select Trigger
      </Typography> 
      <Typography variant="body2" color="text.secondary" mb={2}>
         
        Choose what event starts this automation. 
      </Typography> 
      <Box display="flex" flexWrap="wrap" gap={1}>
         
        {options.map((key) => {
          const detail = TRIGGER_DETAILS[key] || {
            label: key,
            description: "",
            icon: "ΓÜí",
            color: "default",
          };
          const selected = value === key;
          return (
            <Card
              key={key}
              variant="outlined"
              sx={{
                cursor: "pointer",
                minWidth: 180,
                border: selected ? 2 : 1,
                borderColor: selected ? "primary.main" : "divider",
                bgcolor: selected ? "action.selected" : "background.paper",
                "&:hover": { borderColor: "primary.light" },
              }}
              onClick={() => onChange(key)}
            >
               
              <CardContent sx={{ py: 1.5, px: 2, "&:last-child": { pb: 1.5 } }}>
                 
                <Typography variant="body2" fontWeight={600}>
                   
                  {detail.icon} {detail.label} 
                </Typography> 
                <Typography variant="caption" color="text.secondary">
                   
                  {detail.description} 
                </Typography> 
                {selected && (
                  <Chip
                    label="Selected"
                    size="small"
                    color="primary"
                    sx={{ mt: 0.5 }}
                  />
                )} 
              </CardContent> 
            </Card>
          );
        })} 
      </Box> 
    </Box>
  );
}
