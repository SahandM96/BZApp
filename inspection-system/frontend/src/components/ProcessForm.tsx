import React, { useState } from 'react';
import axios from 'axios';
import { TextField, Button } from '@mui/material';

interface Process {
  name: string;
  description: string;
  departmentId: string;
}

const ProcessForm: React.FC = () => {
  const [process, setProcess] = useState<Process>({ name: '', description: '', departmentId: '' });

  const handleSubmit = async () => {
    try {
      await axios.post('http://localhost:8080/api/processes', process);
      alert('Process saved!');
    } catch (error) {
      console.error('Error saving process:', error);
    }
  };

  return (
    <div>
      <TextField
        label="Process Name"
        value={process.name}
        onChange={(e) => setProcess({ ...process, name: e.target.value })}
      />
      <TextField
        label="Description"
        value={process.description}
        onChange={(e) => setProcess({ ...process, description: e.target.value })}
      />
      <TextField
        label="Department ID"
        value={process.departmentId}
        onChange={(e) => setProcess({ ...process, departmentId: e.target.value })}
      />
      <Button onClick={handleSubmit} variant="contained" color="primary">
        Save Process
      </Button>
    </div>
  );
};

export default ProcessForm;
