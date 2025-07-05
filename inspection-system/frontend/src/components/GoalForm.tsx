import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { TextField, Button, Box, Typography, Select, MenuItem, FormControl, InputLabel } from '@mui/material';
import { DatePicker, LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';


// Simplified interfaces for dropdowns
interface ProcessSimplified {
    id: number;
    name: string;
}

interface DepartmentSimplified {
    id: number;
    name: string;
}

interface UserSimplified {
    id: number;
    username: string;
}

// Align with backend Goal entity or DTO
interface GoalFormData {
    id?: number;
    name: string;
    description: string;
    processId?: number | string; // Optional, if goal can be standalone
    departmentId?: number | string; // Can be inferred from process or set directly
    responsibleUserId?: number | string;
    createdById: number | string; // Typically from logged-in user context
    targetValue?: number | string;
    targetMetricDescription?: string;
    startDate?: Date | null;
    endDate?: Date | null;
    status: string; // e.g., ACTIVE, COMPLETED
}

interface GoalFormProps {
    initialGoal?: GoalFormData;
    onSave: (goal: GoalFormData) => void;
    processes: ProcessSimplified[];
    departments: DepartmentSimplified[];
    users: UserSimplified[]; // For responsibleUser and createdBy (if selectable)
    // selectedProcessId?: number; // If form is contextually opened for a specific process
}

const GoalForm: React.FC<GoalFormProps> = ({ initialGoal, onSave, processes, departments, users }) => {
    const [goal, setGoal] = useState<GoalFormData>(
        initialGoal || {
            name: '',
            description: '',
            status: 'ACTIVE',
            createdById: 1, // Placeholder for logged-in user
            processId: '',
            departmentId: '',
            responsibleUserId: '',
            targetValue: '',
            targetMetricDescription: '',
            startDate: null,
            endDate: null,
        }
    );

    useEffect(() => {
        if (initialGoal) {
             // Ensure dates are Date objects or null for DatePicker
            setGoal({
                ...initialGoal,
                startDate: initialGoal.startDate ? new Date(initialGoal.startDate) : null,
                endDate: initialGoal.endDate ? new Date(initialGoal.endDate) : null,
            });
        }
    }, [initialGoal]);

    const handleChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = event.target;
        setGoal(prev => ({ ...prev, [name]: value }));
    };

    const handleSelectChange = (event: any) => { // Material UI SelectChangeEvent
        const { name, value } = event.target;
        setGoal(prev => ({ ...prev, [name as string]: value as string | number }));
    };

    const handleDateChange = (name: keyof GoalFormData, date: Date | null) => {
        setGoal(prev => ({ ...prev, [name]: date }));
    };


    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        if (!goal.name || (!goal.departmentId && !goal.processId)) {
            alert('Goal Name and either Department or Process association are required.');
            return;
        }

        const payload: any = {
            ...goal,
            // Convert IDs to numbers and nest if backend expects objects
            department: goal.departmentId ? { id: Number(goal.departmentId) } : undefined,
            process: goal.processId ? { id: Number(goal.processId) } : undefined,
            responsibleUser: goal.responsibleUserId ? { id: Number(goal.responsibleUserId) } : undefined,
            createdBy: { id: Number(goal.createdById) }, // Assuming createdById is always present
            startDate: goal.startDate ? goal.startDate.toISOString().split('T')[0] : null, // Format date as YYYY-MM-DD
            endDate: goal.endDate ? goal.endDate.toISOString().split('T')[0] : null,
        };

        // Clean up top-level IDs if nested objects are used
        // delete payload.departmentId;
        // delete payload.processId;
        // delete payload.responsibleUserId;
        // delete payload.createdById;


        try {
            let response;
            if (goal.id) {
                response = await axios.put(`http://localhost:8080/api/goals/${goal.id}`, payload);
            } else {
                response = await axios.post('http://localhost:8080/api/goals', payload);
            }
            alert(`Goal ${goal.id ? 'updated' : 'saved'} successfully!`);
            onSave(response.data);
        } catch (error) {
            console.error('Error saving goal:', error);
            alert('Failed to save goal. See console for details.');
        }
    };

    return (
        <LocalizationProvider dateAdapter={AdapterDateFns}>
            <Box component="form" onSubmit={handleSubmit} sx={{ '& .MuiTextField-root': { m: 1, width: 'calc(100% - 16px)' }, '& .MuiFormControl-root': { m: 1, width: 'calc(100% - 16px)'}, mt: 2 }}>
                <Typography variant="h6">{goal.id ? 'Edit Goal' : 'Create New Goal'}</Typography>
                <TextField label="Goal Name" name="name" value={goal.name} onChange={handleChange} required fullWidth />
                <TextField label="Description" name="description" value={goal.description} onChange={handleChange} multiline rows={3} fullWidth />

                <FormControl fullWidth margin="normal">
                    <InputLabel id="process-select-label">Associated Process (Optional)</InputLabel>
                    <Select labelId="process-select-label" name="processId" value={goal.processId || ''} label="Associated Process (Optional)" onChange={handleSelectChange}>
                        <MenuItem value=""><em>None</em></MenuItem>
                        {processes.map(proc => <MenuItem key={proc.id} value={proc.id}>{proc.name}</MenuItem>)}
                    </Select>
                </FormControl>

                <FormControl fullWidth margin="normal">
                    <InputLabel id="department-select-label">Associated Department</InputLabel>
                    <Select labelId="department-select-label" name="departmentId" value={goal.departmentId || ''} label="Associated Department" onChange={handleSelectChange} required={!goal.processId}>
                         <MenuItem value=""><em>Select Department</em></MenuItem>
                        {departments.map(dep => <MenuItem key={dep.id} value={dep.id}>{dep.name}</MenuItem>)}
                    </Select>
                </FormControl>

                <FormControl fullWidth margin="normal">
                    <InputLabel id="responsible-user-select-label">Responsible User (Optional)</InputLabel>
                    <Select labelId="responsible-user-select-label" name="responsibleUserId" value={goal.responsibleUserId || ''} label="Responsible User (Optional)" onChange={handleSelectChange}>
                        <MenuItem value=""><em>None</em></MenuItem>
                        {users.map(user => <MenuItem key={user.id} value={user.id}>{user.username}</MenuItem>)}
                    </Select>
                </FormControl>

                <TextField label="Target Value" name="targetValue" type="number" value={goal.targetValue || ''} onChange={handleChange} fullWidth />
                <TextField label="Target Metric Description" name="targetMetricDescription" value={goal.targetMetricDescription || ''} onChange={handleChange} fullWidth />

                <DatePicker
                    label="Start Date"
                    value={goal.startDate}
                    onChange={(date) => handleDateChange('startDate', date)}
                    renderInput={(params) => <TextField {...params} fullWidth />}
                />
                <DatePicker
                    label="End Date"
                    value={goal.endDate}
                    onChange={(date) => handleDateChange('endDate', date)}
                    renderInput={(params) => <TextField {...params} fullWidth />}
                />

                <TextField label="Status" name="status" value={goal.status} onChange={handleChange} select fullWidth>
                    {['ACTIVE', 'COMPLETED', 'ON_HOLD', 'CANCELLED'].map(option => (
                        <MenuItem key={option} value={option}>{option}</MenuItem>
                    ))}
                </TextField>

                <TextField label="Created By User ID (Placeholder)" name="createdById" type="number" value={goal.createdById} onChange={handleChange} disabled fullWidth />

                <Button type="submit" variant="contained" color="primary" sx={{ mt: 2, ml: 1 }}>
                    {goal.id ? 'Update Goal' : 'Save Goal'}
                </Button>
            </Box>
        </LocalizationProvider>
    );
};

export default GoalForm;
