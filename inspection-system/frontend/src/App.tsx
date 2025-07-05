import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { AppBar, Toolbar, Typography, Container, Button, Box } from '@mui/material';
import DashboardView from './components/DashboardView';
// You can create other specific pages if needed, e.g., UserManagementPage, SettingsPage
// import UserManagementPage from './pages/UserManagementPage';

function App() {
  return (
    <Router>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            Inspection System
          </Typography>
          <Button color="inherit" component={Link} to="/">Dashboard</Button>
          {/* Add more navigation links here as needed */}
          {/* <Button color="inherit" component={Link} to="/users">User Management</Button> */}
          {/* <Button color="inherit" component={Link} to="/settings">Settings</Button> */}
        </Toolbar>
      </AppBar>

      <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}> {/* Using xl for wider content area */}
        <Routes>
          <Route path="/" element={<DashboardView />} />
          {/* <Route path="/users" element={<UserManagementPage />} /> */}
          {/* Define other routes here */}
          <Route path="*" element={
            <Box sx={{textAlign: 'center', mt: 5}}>
              <Typography variant="h4">Page Not Found</Typography>
              <Button component={Link} to="/" variant="contained" sx={{mt:2}}>Go to Dashboard</Button>
            </Box>
          } />
        </Routes>
      </Container>
    </Router>
  );
}

export default App;
