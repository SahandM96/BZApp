import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { TextField, Button, Box, Typography, Select, MenuItem, FormControl, InputLabel } from '@mui/material';

// Define interfaces for entities that might be simplified for the form
interface DepartmentSimplified {
    id: number;
    name: string;
}

interface UserSimplified {
    id: number;
    username: string;
}

// Interface for the Process entity as expected by the backend (or a DTO)
// This should align with your backend's Process entity or a specific DTO for creation/update
interface ProcessFormData {
    id?: number; // Optional: for editing existing processes
    name: string;
    description: string;
    inputs: string;
    outputs: string;
    status: string; // e.g., DRAFT, APPROVED
    departmentId: number | string; // Allow string for initial empty state if using Select
    createdById: number | string; // Or fetched from logged-in user context
}

// Props for the ProcessForm component
interface ProcessFormProps {
    initialProcess?: ProcessFormData; // For editing
    onSave: (process: ProcessFormData) => void; // Callback after saving
    departments: DepartmentSimplified[]; // List of departments to select from
    // users: UserSimplified[]; // List of users if 'createdBy' is selectable, otherwise fetched from context
}

const ProcessForm: React.FC<ProcessFormProps> = ({ initialProcess, onSave, departments }) => {
    const [process, setProcess] = useState<ProcessFormData>(
        initialProcess || {
            name: '',
            description: '',
            inputs: '',
            outputs: '',
            status: 'DRAFT', // Default status
            departmentId: '', // Initially empty
            createdById: 1, // Placeholder: This should be the logged-in user's ID
        }
    );

    useEffect(() => {
        if (initialProcess) {
            setProcess(initialProcess);
        }
    }, [initialProcess]);

    const handleChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | { name?: string; value: unknown }>) => {
        const { name, value } = event.target as { name: keyof ProcessFormData, value: string | number };
        setProcess(prev => ({ ...prev, [name]: value }));
    };

    const handleSelectChange = (event: any) => { // Material UI SelectChangeEvent
        const { name, value } = event.target;
         setProcess(prev => ({ ...prev, [name as string]: value as string | number}));
    };


    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        // Basic validation
        if (!process.name || !process.departmentId) {
            alert('Process Name and Department are required.');
            return;
        }

        const payload = {
            ...process,
            department: { id: Number(process.departmentId) }, // Nest department ID as expected by backend
            createdBy: { id: Number(process.createdById) }, // Nest creator ID
        };

        // Remove standalone IDs if nested objects are used for backend
        // delete payload.departmentId;
        // delete payload.createdById;


        try {
            let response;
            if (process.id) { // Editing existing process
                response = await axios.put(`http://localhost:8080/api/processes/${process.id}`, payload);
            } else { // Creating new process
                response = await axios.post('http://localhost:8080/api/processes', payload);
            }
            alert(`Process ${process.id ? 'updated' : 'saved'} successfully!`);
            onSave(response.data); // Pass the saved/updated process data back
        } catch (error) {
            console.error('Error saving process:', error);
            alert('Failed to save process. See console for details.');
        }
    };

    return (
        <Box component="form" onSubmit={handleSubmit} sx={{ '& .MuiTextField-root': { m: 1, width: '100%' }, mt: 2 }}>
            <Typography variant="h6">{process.id ? 'Edit Process' : 'Create New Process'}</Typography>
            <TextField
                label="Process Name"
                name="name"
                value={process.name}
                onChange={handleChange}
                required
                fullWidth
            />
            <TextField
                label="Description"
                name="description"
                value={process.description}
                onChange={handleChange}
                multiline
                rows={3}
                fullWidth
            />
            <TextField
                label="Inputs"
                name="inputs"
                value={process.inputs}
                onChange={handleChange}
                multiline
                rows={2}
                fullWidth
            />
            <TextField
                label="Outputs"
                name="outputs"
                value={process.outputs}
                onChange={handleChange}
                multiline
                rows={2}
                fullWidth
            />
            <FormControl fullWidth margin="normal" sx={{ m: 1 }}>
                <InputLabel id="department-select-label">Department</InputLabel>
                <Select
                    labelId="department-select-label"
                    id="departmentId"
                    name="departmentId"
                    value={process.departmentId}
                    label="Department"
                    onChange={handleSelectChange}
                    required
                >
                    <MenuItem value=""><em>Select Department</em></MenuItem>
                    {departments.map(dep => (
                        <MenuItem key={dep.id} value={dep.id}>{dep.name}</MenuItem>
                    ))}
                </Select>
            </FormControl>

            {/* Status - could be a Select as well if predefined statuses exist */}
            <TextField
                label="Status"
                name="status"
                value={process.status}
                onChange={handleChange}
                // disabled // Or make it a Select: DRAFT, PENDING_APPROVAL, APPROVED
                fullWidth
            />

            {/* Created By ID - In a real app, this would be hidden and taken from logged-in user context */}
             <TextField
                label="Created By User ID (Placeholder)"
                name="createdById"
                type="number"
                value={process.createdById}
                onChange={handleChange}
                disabled
                fullWidth
                helperText="This would be automatically set based on the logged-in user."
            />


            <Button type="submit" variant="contained" color="primary" sx={{ mt: 2, ml:1 }}>
                {process.id ? 'Update Process' : 'Save Process'}
            </Button>
        </Box>
    );
};

export default ProcessForm;

// Example of how you might fetch departments in a parent component:
// const [departments, setDepartments] = useState<DepartmentSimplified[]>([]);
// useEffect(() => {
//     axios.get('http://localhost:8080/api/departments')
//         .then(response => setDepartments(response.data.map((d: any) => ({id: d.id, name: d.name}))))
//         .catch(error => console.error("Error fetching departments", error));
// }, []);
// <ProcessForm onSave={handleProcessSave} departments={departments} />
