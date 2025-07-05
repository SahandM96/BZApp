import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Grid, Paper, Typography, Box, Modal, Backdrop, Fade, CircularProgress, Alert } from '@mui/material';

import ProcessList from './ProcessList';
import ProcessForm from './ProcessForm';
import GoalList from './GoalList';
import GoalForm from './GoalForm';
import KpiList from './KpiList';
import KpiForm from './KpiForm';

// Define interfaces for data types (can be imported from a shared types file)
// These should match the structures used by your list and form components.
interface ProcessData { id?: number; name: string; description: string; inputs: string; outputs: string; status: string; departmentId: number | string; createdById: number | string; }
interface GoalData { id?: number; name: string; description: string; status: string; createdById: number | string; processId?: number | string; departmentId?: number | string; responsibleUserId?: number | string; targetValue?: number | string; targetMetricDescription?: string; startDate?: Date | null; endDate?: Date | null; }
interface KpiData { id?: number; name: string; description?: string; goalId: number | string; responsibleUserId?: number | string; targetValue?: number | string; actualValue?: number | string; unit?: string; measurementFrequency?: string; }

// Simplified interfaces for dropdowns - these might be fetched once here
interface DepartmentSimplified { id: number; name: string; }
interface UserSimplified { id: number; username: string; }
interface ProcessSimplified { id: number; name: string; }
interface GoalSimplified { id: number; name: string; }


const DashboardView: React.FC = () => {
    // Modal states
    const [openProcessModal, setOpenProcessModal] = useState(false);
    const [editingProcess, setEditingProcess] = useState<ProcessData | undefined>(undefined);

    const [openGoalModal, setOpenGoalModal] = useState(false);
    const [editingGoal, setEditingGoal] = useState<GoalData | undefined>(undefined);

    const [openKpiModal, setOpenKpiModal] = useState(false);
    const [editingKpi, setEditingKpi] = useState<KpiData | undefined>(undefined);

    // Data for forms
    const [departments, setDepartments] = useState<DepartmentSimplified[]>([]);
    const [users, setUsers] = useState<UserSimplified[]>([]);
    const [processesSimple, setProcessesSimple] = useState<ProcessSimplified[]>([]);
    const [goalsSimple, setGoalsSimple] = useState<GoalSimplified[]>([]);

    // Selected entities for context
    const [selectedProcessForGoals, setSelectedProcessForGoals] = useState<ProcessData | undefined>(undefined);
    const [selectedGoalForKpis, setSelectedGoalForKpis] = useState<GoalData | undefined>(undefined);

    // Loading and error states for initial data fetch
    const [loadingDropdownData, setLoadingDropdownData] = useState(true);
    const [errorDropdownData, setErrorDropdownData] = useState<string | null>(null);

    // State to trigger list refreshes
    const [refreshProcessListKey, setRefreshProcessListKey] = useState(0);
    const [refreshGoalListKey, setRefreshGoalListKey] = useState(0);
    const [refreshKpiListKey, setRefreshKpiListKey] = useState(0);


    useEffect(() => {
        const fetchDropdownData = async () => {
            setLoadingDropdownData(true);
            setErrorDropdownData(null);
            try {
                const [deptRes, userRes, procRes, goalRes] = await Promise.all([
                    axios.get('http://localhost:8080/api/departments'),
                    axios.get('http://localhost:8080/api/users'), // Assuming a /api/users endpoint exists
                    axios.get('http://localhost:8080/api/processes'),
                    axios.get('http://localhost:8080/api/goals')
                ]);
                setDepartments(deptRes.data.map((d: any) => ({ id: d.id, name: d.name })));
                setUsers(userRes.data.map((u: any) => ({ id: u.id, username: u.username })));
                setProcessesSimple(procRes.data.map((p: any) => ({id: p.id, name: p.name})));
                setGoalsSimple(goalRes.data.map((g:any) => ({id: g.id, name: g.name})));

            } catch (error) {
                console.error("Error fetching dropdown data:", error);
                setErrorDropdownData("Failed to load essential data for forms. Some functionalities might be limited.");
            } finally {
                setLoadingDropdownData(false);
            }
        };
        fetchDropdownData();
    }, []);

    // Handlers for opening modals
    const handleOpenProcessModal = (process?: ProcessData) => {
        setEditingProcess(process);
        setOpenProcessModal(true);
    };
    const handleOpenGoalModal = (goal?: GoalData) => {
        setEditingGoal(goal);
        setOpenGoalModal(true);
    };
    const handleOpenKpiModal = (kpi?: KpiData) => {
        setEditingKpi(kpi);
        setOpenKpiModal(true);
    };

    // Handlers for closing modals
    const handleCloseProcessModal = () => setOpenProcessModal(false);
    const handleCloseGoalModal = () => setOpenGoalModal(false);
    const handleCloseKpiModal = () => setOpenKpiModal(false);

    // Save handlers
    const handleProcessSave = (process: ProcessData) => {
        console.log('Process saved:', process);
        setRefreshProcessListKey(prev => prev + 1); // Trigger list refresh
        handleCloseProcessModal();
        // If it's a newly created process, or if the currently selected process was edited
        if (!selectedProcessForGoals || selectedProcessForGoals.id === process.id || !process.id) {
            // Potentially update the selected process if it was the one being edited
            // Or clear selection if a new one is made to avoid stale data for goals
        }
    };
    const handleGoalSave = (goal: GoalData) => {
        console.log('Goal saved:', goal);
        setRefreshGoalListKey(prev => prev + 1);
        handleCloseGoalModal();
    };
    const handleKpiSave = (kpi: KpiData) => {
        console.log('KPI saved:', kpi);
        setRefreshKpiListKey(prev => prev + 1);
        handleCloseKpiModal();
    };

    // When a process is selected from the list (e.g., for viewing its goals)
    // This is a simplified selection handler. ProcessList would need an onSelect prop.
    // For now, we'll assume ProcessList calls onEdit which can also serve to select.
    const handleSelectProcessForGoals = (process: ProcessData) => {
        setSelectedProcessForGoals(process);
        setSelectedGoalForKpis(undefined); // Clear KPI selection when process changes
        setRefreshGoalListKey(prev => prev + 1); // Refresh goal list for the new process
    };

    // Similar for selecting a goal to see its KPIs
    const handleSelectGoalForKpis = (goal: GoalData) => {
        setSelectedGoalForKpis(goal);
        setRefreshKpiListKey(prev => prev + 1); // Refresh KPI list for the new goal
    };


    const modalStyle = {
        position: 'absolute' as 'absolute',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        width: '80%',
        maxWidth: 600,
        bgcolor: 'background.paper',
        border: '2px solid #000',
        boxShadow: 24,
        p: 4,
        maxHeight: '90vh',
        overflowY: 'auto',
    };

    if (loadingDropdownData) return <CircularProgress />;
    // Do not render forms if essential data like departments or users failed to load
    if (errorDropdownData && (!departments.length || !users.length)) {
        return <Alert severity="error">{errorDropdownData} - Cannot display forms.</Alert>;
    }


    return (
        <Box sx={{ flexGrow: 1, p: 2 }}>
            <Typography variant="h4" gutterBottom component="div" sx={{textAlign: 'center', mb:3}}>
                Inspection Management Dashboard
            </Typography>
            {errorDropdownData && <Alert severity="warning" sx={{mb:2}}>{errorDropdownData}</Alert>}

            <Grid container spacing={3}>
                {/* Processes Section */}
                <Grid item xs={12} md={selectedProcessForGoals ? 4 : 12}>
                    <Paper sx={{ p: 2 }}>
                        <Box sx={{display: 'flex', justifyContent:'space-between', alignItems:'center', mb:1}}>
                            <Typography variant="h6">Processes</Typography>
                            <Button variant="contained" onClick={() => handleOpenProcessModal()}>Add Process</Button>
                        </Box>
                        <ProcessList
                            key={refreshProcessListKey}
                            onEditProcess={(process) => {
                                handleOpenProcessModal(process); // For editing
                                handleSelectProcessForGoals(process); // For context
                            }}
                        />
                    </Paper>
                </Grid>

                {/* Goals Section - Visible if a process is selected */}
                {selectedProcessForGoals && (
                    <Grid item xs={12} md={selectedGoalForKpis ? 4 : 8}>
                        <Paper sx={{ p: 2 }}>
                             <Box sx={{display: 'flex', justifyContent:'space-between', alignItems:'center', mb:1}}>
                                <Typography variant="h6">Goals for: {selectedProcessForGoals.name}</Typography>
                                <Button variant="contained" onClick={() => handleOpenGoalModal()}>Add Goal</Button>
                            </Box>
                            <GoalList
                                key={refreshGoalListKey}
                                // selectedProcessId={selectedProcessForGoals.id}
                                onEditGoal={(goal) => {
                                    handleOpenGoalModal(goal);
                                    handleSelectGoalForKpis(goal);
                                }}
                            />
                        </Paper>
                    </Grid>
                )}

                {/* KPIs Section - Visible if a goal is selected */}
                {selectedProcessForGoals && selectedGoalForKpis && (
                    <Grid item xs={12} md={4}>
                        <Paper sx={{ p: 2 }}>
                            <Box sx={{display: 'flex', justifyContent:'space-between', alignItems:'center', mb:1}}>
                                <Typography variant="h6">KPIs for: {selectedGoalForKpis.name}</Typography>
                                <Button variant="contained" onClick={() => handleOpenKpiModal()}>Add KPI</Button>
                            </Box>
                            <KpiList
                                key={refreshKpiListKey}
                                selectedGoalId={selectedGoalForKpis.id}
                                onEditKpi={handleOpenKpiModal}
                            />
                        </Paper>
                    </Grid>
                )}
            </Grid>

            {/* Process Modal */}
            <Modal open={openProcessModal} onClose={handleCloseProcessModal} closeAfterTransition BackdropComponent={Backdrop} BackdropProps={{ timeout: 500 }}>
                <Fade in={openProcessModal}>
                    <Box sx={modalStyle}>
                        <ProcessForm
                            initialProcess={editingProcess}
                            onSave={handleProcessSave}
                            departments={departments}
                        />
                    </Box>
                </Fade>
            </Modal>

            {/* Goal Modal */}
            <Modal open={openGoalModal} onClose={handleCloseGoalModal} closeAfterTransition BackdropComponent={Backdrop} BackdropProps={{ timeout: 500 }}>
                <Fade in={openGoalModal}>
                    <Box sx={modalStyle}>
                        <GoalForm
                            initialGoal={editingGoal}
                            onSave={handleGoalSave}
                            processes={processesSimple}
                            departments={departments}
                            users={users}
                            // selectedProcessId={selectedProcessForGoals?.id}
                        />
                    </Box>
                </Fade>
            </Modal>

            {/* KPI Modal */}
            <Modal open={openKpiModal} onClose={handleCloseKpiModal} closeAfterTransition BackdropComponent={Backdrop} BackdropProps={{ timeout: 500 }}>
                <Fade in={openKpiModal}>
                    <Box sx={modalStyle}>
                        <KpiForm
                            initialKpi={editingKpi}
                            onSave={handleKpiSave}
                            goals={goalsSimple}
                            users={users}
                            // selectedGoalId={selectedGoalForKpis?.id}
                        />
                    </Box>
                </Fade>
            </Modal>
        </Box>
    );
};

export default DashboardView;
