import { useState, useEffect, useCallback } from "react";
import {
  Box,
  Typography,
  Button,
  Switch,
  IconButton,
  Tooltip,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Alert,
  Snackbar,
  Card,
  CardContent,
  Grid,
  Tabs,
  Tab,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  TextField,
} from "@mui/material";
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  PlayArrow as PlayIcon,
  History as HistoryIcon,
  ToggleOn as ToggleOnIcon,
  AutoAwesome as TemplateIcon,
} from "@mui/icons-material";
import {
  getWorkspaceRules,
  deleteRule,
  toggleRule,
  getTemplates,
  applyTemplate,
} from "../api/automationApi";
import RuleBuilderModal from "../components/automation/RuleBuilderModal";
import AutomationExecutionHistory from "../components/automation/AutomationExecutionHistory";
import EmptyState from "../../../components/EmptyState";
import ErrorState from "../../../components/ErrorState";
import { TableRowSkeleton } from "../../../components/LoadingSkeleton";
import ConfirmationDialog from "../../projects/components/members/ConfirmationDialog";
const TRIGGER_LABELS = {
  TASK_CREATED: "Task Created",
  TASK_UPDATED: "Task Updated",
  TASK_COMPLETED: "Task Completed",
  TASK_ASSIGNED: "Task Assigned",
  TASK_OVERDUE: "Task Overdue",
  SPRINT_STARTED: "Sprint Started",
  SPRINT_COMPLETED: "Sprint Completed",
  DEPENDENCY_BLOCKED: "Dependency Blocked",
  DEPENDENCY_UNBLOCKED: "Dependency Unblocked",
  COMMENT_ADDED: "Comment Added",
  TIMELOG_CREATED: "Time Log Created",
};
const ACTION_LABELS = {
  UPDATE_STATUS: "Update Status",
  ASSIGN_USER: "Assign User",
  ADD_COMMENT: "Add Comment",
  SET_PRIORITY: "Set Priority",
  SET_DUE_DATE: "Set Due Date",
  CREATE_SUBTASK: "Create Subtask",
  SEND_NOTIFICATION: "Send Notification",
  START_SPRINT: "Start Sprint",
  MOVE_TO_SPRINT: "Move to Sprint",
  ADD_LABEL: "Add Label",
  ESCALATE_TASK: "Escalate Task",
};
const ACTION_COLORS = {
  UPDATE_STATUS: "primary",
  ASSIGN_USER: "secondary",
  ADD_COMMENT: "info",
  SET_PRIORITY: "warning",
  ESCALATE_TASK: "error",
  SEND_NOTIFICATION: "success",
};
export default function AutomationRules({ workspace }) {
  const [rules, setRules] = useState([]);
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [tabValue, setTabValue] = useState(0);
  const [modalOpen, setModalOpen] = useState(false);
  const [editRule, setEditRule] = useState(null);
  const [historyOpen, setHistoryOpen] = useState(false);
  const [templateDialogOpen, setTemplateDialogOpen] = useState(false);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [ruleToDelete, setRuleToDelete] = useState(null);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success",
  });
  const workspaceId = workspace?.id;
  const fetchRules = useCallback(async () => {
    if (!workspaceId) return;
    setLoading(true);
    setError(null);
    try {
      const data = await getWorkspaceRules(workspaceId);
      setRules(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [workspaceId]);
  const fetchTemplates = useCallback(async () => {
    try {
      const data = await getTemplates();
      setTemplates(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error("Failed to load templates");
    }
  }, []);
  useEffect(() => {
    fetchRules();
  }, [fetchRules]);
  useEffect(() => {
    if (tabValue === 1) fetchTemplates();
  }, [tabValue, fetchTemplates]);
  const handleToggle = async (ruleId, currentEnabled) => {
    try {
      await toggleRule(ruleId, !currentEnabled);
      setRules((prev) =>
        prev.map((r) =>
          r.id === ruleId ? { ...r, enabled: !currentEnabled } : r,
        ),
      );
      setSnackbar({
        open: true,
        message: `Rule ${currentEnabled ? "disabled" : "enabled"}`,
        severity: "success",
      });
    } catch {
      setSnackbar({
        open: true,
        message: "Failed to toggle rule",
        severity: "error",
      });
    }
  };
  const handleDelete = (ruleId) => {
    setRuleToDelete(ruleId);
    setShowConfirmDialog(true);
  };

  const confirmDelete = async () => {
    if (!ruleToDelete) return;
    try {
      await deleteRule(ruleToDelete);
      setRules((prev) => prev.filter((r) => r.id !== ruleToDelete));
      setSnackbar({ open: true, message: "Rule deleted", severity: "success" });
    } catch {
      setSnackbar({
        open: true,
        message: "Failed to delete rule",
        severity: "error",
      });
    } finally {
      setShowConfirmDialog(false);
      setRuleToDelete(null);
    }
  };

  const openCreateRule = () => {
    setEditRule(null);
    setModalOpen(true);
  };
  const handleApplyTemplate = async (template) => {
    try {
      await applyTemplate(template.name, workspaceId, null);
      setTemplateDialogOpen(false);
      fetchRules();
      setSnackbar({
        open: true,
        message: `Template "${template.name}" applied`,
        severity: "success",
      });
    } catch {
      setSnackbar({
        open: true,
        message: "Failed to apply template",
        severity: "error",
      });
    }
  };
  const handleSaveRule = () => {
    setModalOpen(false);
    setEditRule(null);
    fetchRules();
  };
  return (
    <Box p={3}>
       
      <Box
        display="flex"
        justifyContent="space-between"
        alignItems="center"
        mb={3}
      >
         
        <Typography variant="h4">Automation Rules</Typography> 
        <Box>
           
          <Button
            variant="outlined"
            startIcon={<TemplateIcon />}
            sx={{ mr: 1 }}
            onClick={() => {
              setTemplateDialogOpen(true);
              fetchTemplates();
            }}
          >
             
            Templates 
          </Button> 
          <Button
            variant="outlined"
            startIcon={<HistoryIcon />}
            sx={{ mr: 1 }}
            onClick={() => setHistoryOpen(true)}
          >
             
            Logs 
          </Button> 
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={openCreateRule}
          >
             
            Create Rule 
          </Button> 
        </Box> 
      </Box> 
      <Tabs value={tabValue} onChange={(e, v) => setTabValue(v)} sx={{ mb: 2 }}>
         
        <Tab label={`Active Rules (${rules.length})`} /> 
        <Tab label={`Templates (${templates.length})`} /> 
      </Tabs> 
      {tabValue === 0 &&
        (loading ? (
          <Paper>
            <table className="w-full text-sm">
              <tbody>
                {[1, 2, 3, 4, 5].map((i) => <TableRowSkeleton key={i} columns={7} />)}
              </tbody>
            </table>
          </Paper>
        ) : error ? (
          <ErrorState error={error} onRetry={fetchRules} />
        ) : rules.length === 0 ? (
          <EmptyState
            icon={TemplateIcon}
            title="No automation rules yet"
            description="Automate repetitive tasks like status updates, assignments, and notifications."
            action={{ label: "Create Rule", icon: AddIcon, onClick: openCreateRule }}
          />
        ) : (
          <TableContainer component={Paper}>
             
            <Table>
               
              <TableHead>
                 
                <TableRow>
                   
                  <TableCell>Name</TableCell> <TableCell>Trigger</TableCell> 
                  <TableCell>Action</TableCell> <TableCell>Order</TableCell> 
                  <TableCell>Last Run</TableCell> <TableCell>Enabled</TableCell> 
                  <TableCell>Actions</TableCell> 
                </TableRow> 
              </TableHead> 
              <TableBody>
                 
                {rules.map((rule) => (
                  <TableRow key={rule.id}>
                     
                    <TableCell>
                       
                      <Typography variant="body2" fontWeight={600}>
                        {rule.name}
                      </Typography> 
                      {rule.description && (
                        <Typography variant="caption" color="text.secondary">
                          {rule.description}
                        </Typography>
                      )} 
                    </TableCell> 
                    <TableCell>
                       
                      <Chip
                        label={
                          TRIGGER_LABELS[rule.triggerType] || rule.triggerType
                        }
                        size="small"
                      /> 
                    </TableCell> 
                    <TableCell>
                       
                      <Chip
                        label={
                          ACTION_LABELS[rule.actionType] || rule.actionType
                        }
                        size="small"
                        color={ACTION_COLORS[rule.actionType] || "default"}
                      /> 
                    </TableCell> 
                    <TableCell>{rule.executionOrder}</TableCell> 
                    <TableCell>
                       
                      <Typography variant="caption">
                        {rule.lastExecutedAt || "Never"}
                      </Typography> 
                    </TableCell> 
                    <TableCell>
                       
                      <Switch
                        checked={rule.enabled}
                        onChange={() => handleToggle(rule.id, rule.enabled)}
                      /> 
                    </TableCell> 
                    <TableCell>
                       
                      <Tooltip title="Edit">
                        <IconButton
                          size="small"
                          onClick={() => {
                            setEditRule(rule);
                            setModalOpen(true);
                          }}
                        >
                           
                          <EditIcon />
                        </IconButton>
                      </Tooltip> 
                      <Tooltip title="Delete">
                        <IconButton
                          size="small"
                          color="error"
                          onClick={() => handleDelete(rule.id)}
                        >
                           
                          <DeleteIcon />
                        </IconButton>
                      </Tooltip> 
                    </TableCell> 
                  </TableRow>
                ))} 
              </TableBody> 
            </Table> 
          </TableContainer>
        ))} 
      {tabValue === 1 && (
        <Grid container spacing={2}>
           
          {templates.map((tmpl, idx) => (
            <Grid item xs={12} sm={6} md={4} key={idx}>
               
              <Card>
                 
                <CardContent>
                   
                  <Typography variant="h6">{tmpl.name}</Typography> 
                  <Typography variant="body2" color="text.secondary" mb={1}>
                    {tmpl.description}
                  </Typography> 
                  <Box display="flex" gap={1} mb={1}>
                     
                    <Chip
                      label={
                        TRIGGER_LABELS[tmpl.triggerType] || tmpl.triggerType
                      }
                      size="small"
                    /> 
                    <Chip
                      label={ACTION_LABELS[tmpl.actionType] || tmpl.actionType}
                      size="small"
                      color={ACTION_COLORS[tmpl.actionType] || "default"}
                    /> 
                  </Box> 
                  <Button
                    size="small"
                    variant="outlined"
                    onClick={() => handleApplyTemplate(tmpl)}
                  >
                     
                    Apply Template 
                  </Button> 
                </CardContent> 
              </Card> 
            </Grid>
          ))} 
        </Grid>
      )} 
      <RuleBuilderModal
        open={modalOpen}
        onClose={() => {
          setModalOpen(false);
          setEditRule(null);
        }}
        onSave={handleSaveRule}
        workspaceId={workspaceId}
        editRule={editRule}
      /> 
      <AutomationExecutionHistory
        open={historyOpen}
        onClose={() => setHistoryOpen(false)}
        workspaceId={workspaceId}
      /> 
      <ConfirmationDialog
        isOpen={showConfirmDialog}
        onClose={() => { setShowConfirmDialog(false); setRuleToDelete(null); }}
        onConfirm={confirmDelete}
        title="Delete automation rule?"
        message="This rule will stop running immediately. This cannot be undone."
        confirmText="Delete"
      />
      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}
      >
         
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert> 
      </Snackbar> 
    </Box>
  );
}
