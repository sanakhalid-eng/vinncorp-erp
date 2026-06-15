import {
  Box,
  Typography,
  Button,
  IconButton,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
} from "@mui/material";
import { Add as AddIcon, Delete as DeleteIcon } from "@mui/icons-material";
const FIELD_OPTIONS = [
  { value: "status", label: "Status" },
  { value: "priority", label: "Priority" },
  { value: "assignee", label: "Assignee" },
  { value: "dueDate", label: "Due Date" },
  { value: "storyPoints", label: "Story Points" },
  { value: "tags", label: "Tags" },
  { value: "blocked", label: "Blocked" },
  { value: "sprint", label: "Sprint" },
];
const OPERATOR_OPTIONS = [
  { value: "EQUALS", label: "Equals" },
  { value: "NOT_EQUALS", label: "Not Equals" },
  { value: "GREATER_THAN", label: "Greater Than" },
  { value: "LESS_THAN", label: "Less Than" },
  { value: "CONTAINS", label: "Contains" },
  { value: "IN", label: "In" },
  { value: "IS_EMPTY", label: "Is Empty" },
  { value: "IS_NOT_EMPTY", label: "Is Not Empty" },
];
export default function ConditionBuilder({ conditions, onChange }) {
  const addCondition = () => {
    onChange([
      ...conditions,
      { fieldName: "status", operator: "EQUALS", comparisonValue: "" },
    ]);
  };
  const updateCondition = (index, field, value) => {
    const updated = conditions.map((c, i) =>
      i === index ? { ...c, [field]: value } : c,
    );
    onChange(updated);
  };
  const removeCondition = (index) => {
    onChange(conditions.filter((_, i) => i !== index));
  };
  return (
    <Box py={2}>
       
      <Box
        display="flex"
        justifyContent="space-between"
        alignItems="center"
        mb={2}
      >
         
        <Typography variant="subtitle1" fontWeight={600}>
          Conditions
        </Typography> 
        <Button size="small" startIcon={<AddIcon />} onClick={addCondition}>
          Add Condition
        </Button> 
      </Box> 
      <Typography variant="body2" color="text.secondary" mb={2}>
         
        All conditions must be met for the action to execute. 
      </Typography> 
      {conditions.length === 0 && (
        <Typography
          variant="body2"
          color="text.secondary"
          fontStyle="italic"
          mb={2}
        >
           
          No conditions — rule will run on every trigger match. 
        </Typography>
      )} 
      {conditions.map((cond, index) => (
        <Box key={index} display="flex" alignItems="center" gap={1} mb={1.5}>
           
          <FormControl size="small" sx={{ minWidth: 140 }}>
             
            <InputLabel>Field</InputLabel> 
            <Select
              value={cond.fieldName}
              label="Field"
              onChange={(e) =>
                updateCondition(index, "fieldName", e.target.value)
              }
            >
               
              {FIELD_OPTIONS.map((o) => (
                <MenuItem key={o.value} value={o.value}>
                  {o.label}
                </MenuItem>
              ))} 
            </Select> 
          </FormControl> 
          <FormControl size="small" sx={{ minWidth: 130 }}>
             
            <InputLabel>Operator</InputLabel> 
            <Select
              value={cond.operator}
              label="Operator"
              onChange={(e) =>
                updateCondition(index, "operator", e.target.value)
              }
            >
               
              {OPERATOR_OPTIONS.map((o) => (
                <MenuItem key={o.value} value={o.value}>
                  {o.label}
                </MenuItem>
              ))} 
            </Select> 
          </FormControl> 
          {cond.operator !== "IS_EMPTY" && cond.operator !== "IS_NOT_EMPTY" && (
            <TextField
              size="small"
              label="Value"
              value={cond.comparisonValue}
              onChange={(e) =>
                updateCondition(index, "comparisonValue", e.target.value)
              }
              sx={{ minWidth: 150 }}
            />
          )} 
          <IconButton
            size="small"
            color="error"
            onClick={() => removeCondition(index)}
          >
             
            <DeleteIcon /> 
          </IconButton> 
        </Box>
      ))} 
      {conditions.length > 0 && (
        <Box mt={1}>
           
          <Chip
            label={`${conditions.length} condition${conditions.length > 1 ? "s" : ""}`}
            size="small"
            color="info"
          /> 
        </Box>
      )} 
    </Box>
  );
}
