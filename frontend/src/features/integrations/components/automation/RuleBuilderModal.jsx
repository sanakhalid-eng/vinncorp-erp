import { useState, useEffect } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Box,
  Typography,
  Chip,
  Alert,
  Snackbar,
  Stepper,
  Step,
  StepLabel,
} from "@mui/material";
import { createRule, updateRule } from "../../api/automationApi";
import TriggerSelector from "./TriggerSelector";
import ConditionBuilder from "./ConditionBuilder";
import ActionBuilder from "./ActionBuilder";
const TRIGGER_OPTIONS = [
  "TASK_CREATED",
  "TASK_UPDATED",
  "TASK_COMPLETED",
  "TASK_ASSIGNED",
  "TASK_OVERDUE",
  "SPRINT_STARTED",
  "SPRINT_COMPLETED",
  "DEPENDENCY_BLOCKED",
  "DEPENDENCY_UNBLOCKED",
  "COMMENT_ADDED",
  "TIMELOG_CREATED",
];
const ACTION_OPTIONS = [
  "UPDATE_STATUS",
  "ASSIGN_USER",
  "ADD_COMMENT",
  "SET_PRIORITY",
  "SET_DUE_DATE",
  "CREATE_SUBTASK",
  "SEND_NOTIFICATION",
  "MOVE_TO_SPRINT",
  "ADD_LABEL",
  "ESCALATE_TASK",
];
export default function RuleBuilderModal({
  open,
  onClose,
  onSave,
  workspaceId,
  editRule,
}) {
  const [activeStep, setActiveStep] = useState(0);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [triggerType, setTriggerType] = useState("");
  const [actionType, setActionType] = useState("");
  const [executionOrder, setExecutionOrder] = useState(0);
  const [cooldownSeconds, setCooldownSeconds] = useState(0);
  const [conditions, setConditions] = useState([]);
  const [actionConfig, setActionConfig] = useState({});
  const [saving, setSaving] = useState(false);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success",
  });
  const isEditing = !!editRule;
  useEffect(() => {
    if (editRule) {
      setName(editRule.name || "");
      setDescription(editRule.description || "");
      setTriggerType(editRule.triggerType || "");
      setActionType(editRule.actionType || "");
      setExecutionOrder(editRule.executionOrder ?? 0);
      setCooldownSeconds(editRule.cooldownSeconds ?? 0);
      setConditions(editRule.conditions || []);
      try {
        setActionConfig(
          editRule.actionConfig ? JSON.parse(editRule.actionConfig) : {},
        );
      } catch {
        setActionConfig({});
      }
    } else {
      resetForm();
    }
  }, [editRule, open]);
  const resetForm = () => {
    setName("");
    setDescription("");
    setTriggerType("");
    setActionType("");
    setExecutionOrder(0);
    setCooldownSeconds(0);
    setConditions([]);
    setActionConfig({});
    setActiveStep(0);
  };
  const handleSave = async () => {
    if (!name || !triggerType || !actionType) {
      setSnackbar({
        open: true,
        message: "Name, trigger, and action are required",
        severity: "error",
      });
      return;
    }
    setSaving(true);
    try {
      const payload = {
        workspaceId,
        name,
        description,
        triggerType,
        actionType,
        executionOrder,
        cooldownSeconds,
        conditions: conditions.map((c) => ({
          fieldName: c.fieldName,
          operator: c.operator,
          comparisonValue: c.comparisonValue,
        })),
        actionConfig: JSON.stringify(actionConfig),
      };
      if (isEditing) {
        await updateRule(editRule.id, payload);
      } else {
        await createRule(payload);
      }
      onSave();
      resetForm();
    } catch (err) {
      setSnackbar({
        open: true,
        message: "Failed to save rule",
        severity: "error",
      });
    } finally {
      setSaving(false);
    }
  };
  const steps = ["Basic Info", "Trigger", "Conditions", "Action"];
  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
       
      <DialogTitle>
        {isEditing ? "Edit Rule" : "Create Automation Rule"}
      </DialogTitle> 
      <DialogContent>
         
        <Stepper activeStep={activeStep} sx={{ my: 2 }}>
           
          {steps.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))} 
        </Stepper> 
        {activeStep === 0 && (
          <Box py={2}>
             
            <TextField
              fullWidth
              label="Rule Name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              sx={{ mb: 2 }}
              required
            /> 
            <TextField
              fullWidth
              label="Description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              multiline
              rows={2}
              sx={{ mb: 2 }}
            /> 
            <TextField
              fullWidth
              label="Execution Order"
              type="number"
              value={executionOrder}
              onChange={(e) => setExecutionOrder(Number(e.target.value))}
              sx={{ mb: 2 }}
            /> 
            <TextField
              fullWidth
              label="Cooldown (seconds)"
              type="number"
              value={cooldownSeconds}
              onChange={(e) => setCooldownSeconds(Number(e.target.value))}
              helperText="Minimum time between rule executions. 0 = no cooldown."
            /> 
          </Box>
        )} 
        {activeStep === 1 && (
          <TriggerSelector
            value={triggerType}
            onChange={setTriggerType}
            options={TRIGGER_OPTIONS}
          />
        )} 
        {activeStep === 2 && (
          <ConditionBuilder conditions={conditions} onChange={setConditions} />
        )} 
        {activeStep === 3 && (
          <ActionBuilder
            actionType={actionType}
            config={actionConfig}
            onChange={setActionConfig}
            onActionTypeChange={setActionType}
            actionOptions={ACTION_OPTIONS}
          />
        )} 
      </DialogContent> 
      <DialogActions>
         
        <Button onClick={onClose}>Cancel</Button> 
        <Button
          onClick={() => setActiveStep((prev) => Math.max(0, prev - 1))}
          disabled={activeStep === 0}
        >
          Back
        </Button> 
        {activeStep < steps.length - 1 ? (
          <Button
            variant="contained"
            onClick={() => setActiveStep((prev) => prev + 1)}
          >
            Next
          </Button>
        ) : (
          <Button variant="contained" onClick={handleSave} disabled={saving}>
             
            {saving ? "Saving..." : isEditing ? "Update" : "Create"} 
          </Button>
        )} 
      </DialogActions> 
      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}
      >
         
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert> 
      </Snackbar> 
    </Dialog>
  );
}
