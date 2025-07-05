import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { List, ListItem, ListItemText, Typography, Button, Box, Paper, CircularProgress, Alert, Select, MenuItem, FormControl, InputLabel } from '@mui/material';

// Align with backend Goal entity/DTO
interface Goal {
    id: number;
    name: string;
    description?: string;
    status: string;
    process?: { id: number; name: string };
    department?: { id: number; name: string };
    responsibleUser?: { id: number; username: string };
    targetValue?: number;
    targetMetricDescription?: string;
    startDate?: string; // Assuming dates are strings from backend
    endDate?: string;
}

interface ProcessSimplified {
    id: number;
    name: string;
}
interface DepartmentSimplified {
    id: number;
    name: string;
}

interface GoalListProps {
    onEditGoal: (goal: Goal) => void;
    // selectedProcessId?: number; // To filter goals by a specific process if provided
}

const GoalList: React.FC<GoalListProps> = ({ onEditGoal }) => {
    const [goals, setGoals] = useState<Goal[]>([]);
    const [processes, setProcesses] = useState<ProcessSimplified[]>([]);
    const [departments, setDepartments] = useState<DepartmentSimplified[]>([]);

    const [filterProcess, setFilterProcess] = useState<string>('');
    const [filterDepartment, setFilterDepartment] = useState<string>('');

    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const fetchGoals = async () => {
        setLoading(true);
        setError(null);
        try {
            const params: any = {};
            if (filterProcess && filterProcess !== 'ALL') params.processId = filterProcess;
            if (filterDepartment && filterDepartment !== 'ALL') params.departmentId = filterDepartment;

            const response = await axios.get('http://localhost:8080/api/goals', { params });
             const formattedGoals = response.data.map((g: any) => ({
                ...g,
                process: g.process || {},
                department: g.department || {},
                responsibleUser: g.responsibleUser || {}
            }));
            setGoals(formattedGoals);
        } catch (err) {
            console.error("Error fetching goals:", err);
            setError("Failed to fetch goals. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    const fetchFilterData = async () => {
        try {
            const [procRes, deptRes] = await Promise.all([
                axios.get('http://localhost:8080/api/processes'),
                axios.get('http://localhost:8080/api/departments')
            ]);
            setProcesses(procRes.data.map((p:any) => ({id: p.id, name: p.name})));
            setDepartments(deptRes.data.map((d:any) => ({id: d.id, name: d.name})));
        } catch (error) {
            console.error("Error fetching filter data for goals:", error);
        }
    };


    useEffect(() => {
        fetchFilterData();
        fetchGoals(); // Initial fetch
    }, []); // Fetch once on mount

    useEffect(() => {
        fetchGoals(); // Refetch when filters change
    }, [filterProcess, filterDepartment]);


    const handleDeleteGoal = async (goalId: number) => {
        if (window.confirm('Are you sure you want to delete this goal and its associated KPIs?')) {
            try {
                await axios.delete(`http://localhost:8080/api/goals/${goalId}`);
                setGoals(goals.filter(g => g.id !== goalId));
                alert('Goal deleted successfully.');
            } catch (err) {
                console.error('Error deleting goal:', err);
                alert('Failed to delete goal.');
            }
        }
    };

    if (loading) return <CircularProgress />;
    if (error) return <Alert severity="error">{error}</Alert>;

    return (
        <Box sx={{ mt: 2, p: 2, width: '100%' }} component={Paper}>
            <Typography variant="h6" gutterBottom>Goal List</Typography>

            <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
                <FormControl fullWidth>
                    <InputLabel id="process-filter-label">Filter by Process</InputLabel>
                    <Select
                        labelId="process-filter-label"
                        value={filterProcess}
                        label="Filter by Process"
                        onChange={(e) => setFilterProcess(e.target.value as string)}
                    >
                        <MenuItem value="ALL"><em>All Processes</em></MenuItem>
                        {processes.map(proc => (
                            <MenuItem key={proc.id} value={proc.id.toString()}>{proc.name}</MenuItem>
                        ))}
                    </Select>
                </FormControl>
                <FormControl fullWidth>
                    <InputLabel id="department-filter-label">Filter by Department</InputLabel>
                    <Select
                        labelId="department-filter-label"
                        value={filterDepartment}
                        label="Filter by Department"
                        onChange={(e) => setFilterDepartment(e.target.value as string)}
                    >
                        <MenuItem value="ALL"><em>All Departments</em></MenuItem>
                        {departments.map(dep => (
                            <MenuItem key={dep.id} value={dep.id.toString()}>{dep.name}</MenuItem>
                        ))}
                    </Select>
                </FormControl>
            </Box>

            {goals.length === 0 ? (
                <Typography>No goals found for the selected criteria.</Typography>
            ) : (
                <List>
                    {goals.map(goal => (
                        <ListItem
                            key={goal.id}
                            divider
                            secondaryAction={
                                <>
                                    <Button onClick={() => onEditGoal(goal)} size="small" variant="outlined" sx={{ mr: 1 }}>Edit</Button>
                                    <Button onClick={() => handleDeleteGoal(goal.id)} size="small" variant="outlined" color="error">Delete</Button>
                                </>
                            }
                        >
                            <ListItemText
                                primary={`${goal.name} (Status: ${goal.status})`}
                                secondary={
                                    <>
                                        <Typography component="span" variant="body2" color="text.primary">
                                            Process: {goal.process?.name || 'N/A'} | Department: {goal.department?.name || 'N/A'}
                                        </Typography>
                                        <br />
                                        {goal.description && <Typography component="span" variant="body2">{goal.description}<br /></Typography>}
                                        Target: {goal.targetValue ? `${goal.targetValue} (${goal.targetMetricDescription || 'N/A'})` : 'N/A'}
                                        <br />
                                        Dates: {goal.startDate ? new Date(goal.startDate).toLocaleDateString() : 'N/A'} - {goal.endDate ? new Date(goal.endDate).toLocaleDateString() : 'N/A'}
                                        <br />
                                        <Typography component="span" variant="caption" color="text.secondary">
                                            Responsible: {goal.responsibleUser?.username || 'N/A'} | ID: {goal.id}
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

export default GoalList;
