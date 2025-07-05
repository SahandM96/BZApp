import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { TextField, Button, Box, Typography, Select, MenuItem, FormControl, InputLabel } from '@mui/material';

// Simplified interfaces for dropdowns
interface GoalSimplified {
    id: number;
    name: string;
}

interface UserSimplified {
    id: number;
    username: string;
}

// Align with backend KPI entity or DTO
interface KpiFormData {
    id?: number;
    name: string;
    description?: string;
    goalId: number | string;
    responsibleUserId?: number | string;
    targetValue?: number | string;
    actualValue?: number | string;
    unit?: string;
    measurementFrequency?: string; // e.g., DAILY, WEEKLY
}

interface KpiFormProps {
    initialKpi?: KpiFormData;
    onSave: (kpi: KpiFormData) => void;
    goals: GoalSimplified[]; // List of goals to associate KPI with
    users: UserSimplified[]; // For responsible user
    // selectedGoalId?: number; // If form is contextually opened for a specific goal
}

const KpiForm: React.FC<KpiFormProps> = ({ initialKpi, onSave, goals, users, /*selectedGoalId*/ }) => {
    const [kpi, setKpi] = useState<KpiFormData>(
        initialKpi || {
            name: '',
            description: '',
            goalId: /*selectedGoalId ||*/ '', // Pre-fill if selectedGoalId is provided
            responsibleUserId: '',
            targetValue: '',
            actualValue: '',
            unit: '',
            measurementFrequency: 'MONTHLY', // Default frequency
        }
    );

    useEffect(() => {
        if (initialKpi) {
            setKpi(initialKpi);
        }
        // If selectedGoalId is provided and no initialKpi, set it
        // else if (selectedGoalId && !initialKpi) {
        //    setKpi(prev => ({ ...prev, goalId: selectedGoalId }));
        // }
    }, [initialKpi, /*selectedGoalId*/]);

    const handleChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = event.target;
        setKpi(prev => ({ ...prev, [name]: value }));
    };

    const handleSelectChange = (event: any) => { // Material UI SelectChangeEvent
        const { name, value } = event.target;
        setKpi(prev => ({ ...prev, [name as string]: value as string | number }));
    };

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        if (!kpi.name || !kpi.goalId) {
            alert('KPI Name and Goal association are required.');
            return;
        }

        const payload = {
            ...kpi,
            goal: { id: Number(kpi.goalId) },
            responsibleUser: kpi.responsibleUserId ? { id: Number(kpi.responsibleUserId) } : undefined,
            targetValue: kpi.targetValue !== '' ? Number(kpi.targetValue) : null, // Ensure numbers or null
            actualValue: kpi.actualValue !== '' ? Number(kpi.actualValue) : null,
        };
        // delete payload.goalId;
        // delete payload.responsibleUserId;

        try {
            let response;
            if (kpi.id) {
                response = await axios.put(`http://localhost:8080/api/kpis/${kpi.id}`, payload);
            } else {
                response = await axios.post('http://localhost:8080/api/kpis', payload);
            }
            alert(`KPI ${kpi.id ? 'updated' : 'saved'} successfully!`);
            onSave(response.data);
        } catch (error) {
            console.error('Error saving KPI:', error);
            alert('Failed to save KPI. See console for details.');
        }
    };

    return (
        <Box component="form" onSubmit={handleSubmit} sx={{ '& .MuiTextField-root': { m: 1, width: 'calc(100% - 16px)' }, '& .MuiFormControl-root': { m: 1, width: 'calc(100% - 16px)'}, mt: 2 }}>
            <Typography variant="h6">{kpi.id ? 'Edit KPI' : 'Create New KPI'}</Typography>
            <TextField label="KPI Name" name="name" value={kpi.name} onChange={handleChange} required fullWidth />
            <TextField label="Description" name="description" value={kpi.description || ''} onChange={handleChange} multiline rows={2} fullWidth />

            <FormControl fullWidth margin="normal" required>
                <InputLabel id="goal-select-label">Associated Goal</InputLabel>
                <Select labelId="goal-select-label" name="goalId" value={kpi.goalId} label="Associated Goal" onChange={handleSelectChange} >
                    <MenuItem value=""><em>Select Goal</em></MenuItem>
                    {goals.map(g => <MenuItem key={g.id} value={g.id}>{g.name}</MenuItem>)}
                </Select>
            </FormControl>

            <FormControl fullWidth margin="normal">
                <InputLabel id="responsible-user-kpi-label">Responsible User (Optional)</InputLabel>
                <Select labelId="responsible-user-kpi-label" name="responsibleUserId" value={kpi.responsibleUserId || ''} label="Responsible User (Optional)" onChange={handleSelectChange}>
                    <MenuItem value=""><em>None</em></MenuItem>
                    {users.map(user => <MenuItem key={user.id} value={user.id}>{user.username}</MenuItem>)}
                </Select>
            </FormControl>

            <TextField label="Target Value" name="targetValue" type="number" value={kpi.targetValue || ''} onChange={handleChange} fullWidth
                       InputLabelProps={{ shrink: kpi.targetValue !== '' && kpi.targetValue !== undefined }}/>
            <TextField label="Actual Value" name="actualValue" type="number" value={kpi.actualValue || ''} onChange={handleChange} fullWidth
                       InputLabelProps={{ shrink: kpi.actualValue !== '' && kpi.actualValue !== undefined }}/>
            <TextField label="Unit" name="unit" value={kpi.unit || ''} onChange={handleChange} fullWidth />

            <TextField
                label="Measurement Frequency"
                name="measurementFrequency"
                value={kpi.measurementFrequency || 'MONTHLY'}
                onChange={handleChange}
                select
                fullWidth
            >
                {['DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'ANNUALLY'].map(option => (
                    <MenuItem key={option} value={option}>{option}</MenuItem>
                ))}
            </TextField>

            <Button type="submit" variant="contained" color="primary" sx={{ mt: 2, ml:1 }}>
                {kpi.id ? 'Update KPI' : 'Save KPI'}
            </Button>
        </Box>
    );
};

export default KpiForm;
