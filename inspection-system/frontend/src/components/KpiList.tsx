import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { List, ListItem, ListItemText, Typography, Button, Box, Paper, CircularProgress, Alert, TextField } from '@mui/material';

// Align with backend KPI entity/DTO
interface KPI {
    id: number;
    name: string;
    description?: string;
    goal?: { id: number; name: string };
    responsibleUser?: { id: number; username: string };
    targetValue?: number;
    actualValue?: number;
    unit?: string;
    measurementFrequency?: string;
    lastUpdatedValueDate?: string;
}

// Simplified interface for Goal, if needed for filtering, but typically this list is shown in context of a Goal
interface GoalSimplified {
    id: number;
    name: string;
}

interface KpiListProps {
    onEditKpi: (kpi: KPI) => void;
    selectedGoalId?: number; // KPIs are usually listed in the context of a specific goal
}

const KpiList: React.FC<KpiListProps> = ({ onEditKpi, selectedGoalId }) => {
    const [kpis, setKpis] = useState<KPI[]>([]);
    const [loading, setLoading] = useState<boolean>(false); // Initially false, true when selectedGoalId is present
    const [error, setError] = useState<string | null>(null);

    // State for inline editing of actualValue
    const [editingKpiId, setEditingKpiId] = useState<number | null>(null);
    const [currentActualValue, setCurrentActualValue] = useState<string>('');


    const fetchKpis = async (goalId: number) => {
        setLoading(true);
        setError(null);
        try {
            const response = await axios.get(`http://localhost:8080/api/kpis`, { params: { goalId } });
            const formattedKpis = response.data.map((k: any) => ({
                ...k,
                goal: k.goal || {},
                responsibleUser: k.responsibleUser || {}
            }));
            setKpis(formattedKpis);
        } catch (err) {
            console.error(`Error fetching KPIs for goal ${goalId}:`, err);
            setError("Failed to fetch KPIs. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (selectedGoalId) {
            fetchKpis(selectedGoalId);
        } else {
            setKpis([]); // Clear KPIs if no goal is selected
            setLoading(false);
        }
    }, [selectedGoalId]);

    const handleUpdateActualValue = async (kpiId: number) => {
        if (currentActualValue === '' || isNaN(parseFloat(currentActualValue))) {
            alert("Please enter a valid number for actual value.");
            return;
        }
        try {
            await axios.put(`http://localhost:8080/api/kpis/${kpiId}/value`, { actualValue: parseFloat(currentActualValue) });
            // Refetch or update local state
            if(selectedGoalId) fetchKpis(selectedGoalId);
            setEditingKpiId(null); // Exit edit mode
            alert('KPI actual value updated.');
        } catch (error) {
            console.error('Error updating KPI actual value:', error);
            alert('Failed to update KPI actual value.');
        }
    };

    const handleDeleteKpi = async (kpiId: number) => {
        if (window.confirm('Are you sure you want to delete this KPI?')) {
            try {
                await axios.delete(`http://localhost:8080/api/kpis/${kpiId}`);
                setKpis(kpis.filter(k => k.id !== kpiId));
                alert('KPI deleted successfully.');
            } catch (err) {
                console.error('Error deleting KPI:', err);
                alert('Failed to delete KPI.');
            }
        }
    };


    if (!selectedGoalId) {
        return <Typography sx={{mt:2, p:2}} component={Paper}>Select a goal to view its KPIs.</Typography>;
    }
    if (loading) return <CircularProgress />;
    if (error) return <Alert severity="error">{error}</Alert>;

    return (
        <Box sx={{ mt: 2, p:2, width: '100%' }} component={Paper}>
            <Typography variant="h6" gutterBottom>KPIs for Selected Goal</Typography>
            {kpis.length === 0 ? (
                <Typography>No KPIs found for this goal.</Typography>
            ) : (
                <List>
                    {kpis.map(kpi => (
                        <ListItem
                            key={kpi.id}
                            divider
                            sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start' }}
                        >
                            <Box sx={{ width: '100%', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <ListItemText
                                    primary={`${kpi.name}`}
                                    secondary={
                                        <>
                                            Target: {kpi.targetValue ?? 'N/A'} {kpi.unit || ''} |
                                            Actual: {kpi.actualValue ?? 'N/A'} {kpi.unit || ''}
                                            <br/>
                                            Frequency: {kpi.measurementFrequency || 'N/A'}
                                            {kpi.description && <><br/>Desc: {kpi.description}</>}
                                            <br />
                                            <Typography component="span" variant="caption" color="text.secondary">
                                                Responsible: {kpi.responsibleUser?.username || 'N/A'} | Last Updated: {kpi.lastUpdatedValueDate ? new Date(kpi.lastUpdatedValueDate).toLocaleDateString() : 'N/A'}
                                            </Typography>
                                        </>
                                    }
                                />
                                <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                                    <Button onClick={() => onEditKpi(kpi)} size="small" variant="outlined">Edit</Button>
                                    <Button onClick={() => handleDeleteKpi(kpi.id)} size="small" variant="outlined" color="error">Delete</Button>
                                </Box>
                            </Box>
                             <Box sx={{ width: '100%', mt: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
                                {editingKpiId === kpi.id ? (
                                    <>
                                        <TextField
                                            size="small"
                                            type="number"
                                            label="New Actual Value"
                                            variant="outlined"
                                            value={currentActualValue}
                                            onChange={(e) => setCurrentActualValue(e.target.value)}
                                            sx={{width: '150px'}}
                                        />
                                        <Button onClick={() => handleUpdateActualValue(kpi.id)} size="small" variant="contained">Save</Button>
                                        <Button onClick={() => setEditingKpiId(null)} size="small" variant="outlined">Cancel</Button>
                                    </>
                                ) : (
                                    <Button onClick={() => {
                                        setEditingKpiId(kpi.id);
                                        setCurrentActualValue(kpi.actualValue?.toString() || '');
                                    }} size="small" variant="outlined" color="info">
                                        Update Actual
                                    </Button>
                                )}
                            </Box>
                        </ListItem>
                    ))}
                </List>
            )}
        </Box>
    );
};

export default KpiList;
