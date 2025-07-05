import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { List, ListItem, ListItemText, Typography, Button, Box, Paper, CircularProgress, Alert, Select, MenuItem, FormControl, InputLabel } from '@mui/material';

// Align with backend Process entity (or a DTO for list view)
interface Process {
    id: number;
    name: string;
    description: string;
    status: string;
    department?: { id: number; name: string }; // Optional and nested
    createdBy?: { id: number; username: string }; // Optional and nested
    // Add other relevant fields like inputs, outputs, createdAt, updatedAt if needed for display
}

interface DepartmentSimplified {
    id: number;
    name: string;
}

interface ProcessListProps {
    onEditProcess: (process: Process) => void; // Callback to handle editing a process
    // onSelectProcess?: (processId: number) => void; // Optional: if selecting a process triggers other actions
}

const ProcessList: React.FC<ProcessListProps> = ({ onEditProcess }) => {
    const [processes, setProcesses] = useState<Process[]>([]);
    const [departments, setDepartments] = useState<DepartmentSimplified[]>([]);
    const [selectedDepartment, setSelectedDepartment] = useState<string>(''); // Store department ID as string
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const fetchProcesses = async (departmentId?: string) => {
        setLoading(true);
        setError(null);
        try {
            const params = departmentId ? { departmentId } : {};
            const response = await axios.get('http://localhost:8080/api/processes', { params });
            // Ensure department and createdBy are at least empty objects if null/undefined from API
            const formattedProcesses = response.data.map((p: any) => ({
                ...p,
                department: p.department || {},
                createdBy: p.createdBy || {}
            }));
            setProcesses(formattedProcesses);
        } catch (err) {
            console.error("Error fetching processes:", err);
            setError("Failed to fetch processes. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    const fetchDepartments = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/departments');
            setDepartments(response.data.map((d: any) => ({ id: d.id, name: d.name })));
        } catch (err) {
            console.error("Error fetching departments:", err);
            // setError("Failed to fetch departments."); // Or handle silently
        }
    };

    useEffect(() => {
        fetchDepartments();
        fetchProcesses(); // Initial fetch for all processes
    }, []);

    const handleDepartmentChange = (event: any) => { // Material UI SelectChangeEvent
        const deptId = event.target.value as string;
        setSelectedDepartment(deptId);
        fetchProcesses(deptId === 'ALL' ? undefined : deptId);
    };

    const handleDeleteProcess = async (processId: number) => {
        if (window.confirm('Are you sure you want to delete this process?')) {
            try {
                await axios.delete(`http://localhost:8080/api/processes/${processId}`);
                setProcesses(processes.filter(p => p.id !== processId));
                alert('Process deleted successfully.');
            } catch (err) {
                console.error('Error deleting process:', err);
                alert('Failed to delete process.');
            }
        }
    };


    if (loading) return <CircularProgress />;
    if (error) return <Alert severity="error">{error}</Alert>;

    return (
        <Box sx={{ mt: 2, p:2,  width: '100%'}} component={Paper}>
            <Typography variant="h6" gutterBottom>Process List</Typography>

            <FormControl fullWidth margin="normal" sx={{ mb: 2 }}>
                <InputLabel id="department-filter-label">Filter by Department</InputLabel>
                <Select
                    labelId="department-filter-label"
                    value={selectedDepartment}
                    label="Filter by Department"
                    onChange={handleDepartmentChange}
                >
                    <MenuItem value="ALL"><em>All Departments</em></MenuItem>
                    {departments.map(dep => (
                        <MenuItem key={dep.id} value={dep.id.toString()}>{dep.name}</MenuItem>
                    ))}
                </Select>
            </FormControl>

            {processes.length === 0 ? (
                <Typography>No processes found.</Typography>
            ) : (
                <List>
                    {processes.map(process => (
                        <ListItem
                            key={process.id}
                            divider
                            secondaryAction={
                                <>
                                    <Button
                                        onClick={() => onEditProcess(process)}
                                        size="small"
                                        variant="outlined"
                                        sx={{ mr: 1 }}
                                    >
                                        Edit
                                    </Button>
                                    <Button
                                        onClick={() => handleDeleteProcess(process.id)}
                                        size="small"
                                        variant="outlined"
                                        color="error"
                                    >
                                        Delete
                                    </Button>
                                </>
                            }
                        >
                            <ListItemText
                                primary={`${process.name} (Status: ${process.status || 'N/A'})`}
                                secondary={
                                    <>
                                        <Typography component="span" variant="body2" color="text.primary">
                                            Department: {process.department?.name || 'N/A'}
                                        </Typography>
                                        <br />
                                        {process.description}
                                        <br />
                                        <Typography component="span" variant="caption" color="text.secondary">
                                            Created by: {process.createdBy?.username || 'N/A'} | ID: {process.id}
                                        </Typography>
                                    </>
                                }
                            />
                        </ListItem>
                    ))}
                </List>
            )}
        </Box>
    );
};

export default ProcessList;
