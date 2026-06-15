import { useState, useEffect } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography,
  Chip,
  CircularProgress,
  Box,
  Tabs,
  Tab,
} from "@mui/material";
import { getRecentLogs } from "../../api/automationApi";
const STATUS_COLORS = {
  SUCCESS: "success",
  FAILURE: "error",
  SKIPPED: "warning",
  RETRYING: "info",
};
export default function AutomationExecutionHistory({
  open,
  onClose,
  workspaceId,
}) {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filter, setFilter] = useState("ALL");
  useEffect(() => {
    if (open && workspaceId) {
      setLoading(true);
      getRecentLogs(workspaceId)
        .then((data) => {
          setLogs(Array.isArray(data) ? data : []);
        })
        .catch(() => {
          setLogs([]);
        })
        .finally(() => setLoading(false));
    }
  }, [open, workspaceId]);
  const filteredLogs =
    filter === "ALL" ? logs : logs.filter((l) => l.status === filter);
  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
       
      <DialogTitle>Automation Execution History</DialogTitle> 
      <DialogContent>
         
        <Tabs value={filter} onChange={(e, v) => setFilter(v)} sx={{ mb: 2 }}>
           
          <Tab label="All" value="ALL" /> 
          <Tab label="Success" value="SUCCESS" /> 
          <Tab label="Failed" value="FAILURE" /> 
          <Tab label="Skipped" value="SKIPPED" /> 
        </Tabs> 
        {loading ? (
          <CircularProgress />
        ) : (
          <TableContainer component={Paper} variant="outlined">
             
            <Table size="small">
               
              <TableHead>
                 
                <TableRow>
                   
                  <TableCell>Rule</TableCell> <TableCell>Entity</TableCell> 
                  <TableCell>Status</TableCell> 
                  <TableCell align="right">Duration</TableCell> 
                  <TableCell>Error</TableCell> <TableCell>Time</TableCell> 
                </TableRow> 
              </TableHead> 
              <TableBody>
                 
                {filteredLogs.map((log) => (
                  <TableRow key={log.id}>
                     
                    <TableCell>
                       
                      <Typography variant="body2" fontWeight={500}>
                        {log.ruleName}
                      </Typography> 
                    </TableCell> 
                    <TableCell>
                       
                      <Typography variant="caption">
                         
                        {log.entityType}#{log.entityId} 
                      </Typography> 
                    </TableCell> 
                    <TableCell>
                       
                      <Chip
                        label={log.status}
                        size="small"
                        color={STATUS_COLORS[log.status] || "default"}
                      /> 
                    </TableCell> 
                    <TableCell align="right">
                       
                      <Typography variant="caption">
                        {log.executionTimeMs}ms
                      </Typography> 
                    </TableCell> 
                    <TableCell>
                       
                      <Typography variant="caption" color="error">
                         
                        {log.errorMessage
                          ? log.errorMessage.substring(0, 50)
                          : "-"} 
                      </Typography> 
                    </TableCell> 
                    <TableCell>
                       
                      <Typography variant="caption">
                        {log.createdAt || "-"}
                      </Typography> 
                    </TableCell> 
                  </TableRow>
                ))} 
                {filteredLogs.length === 0 && (
                  <TableRow>
                     
                    <TableCell colSpan={6} align="center">
                       
                      <Typography py={2} color="text.secondary">
                        No execution logs found.
                      </Typography> 
                    </TableCell> 
                  </TableRow>
                )} 
              </TableBody> 
            </Table> 
          </TableContainer>
        )} 
      </DialogContent> 
      <DialogActions>
         
        <Button onClick={onClose}>Close</Button> 
      </DialogActions> 
    </Dialog>
  );
}
