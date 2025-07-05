-- Drop existing tables if they exist to avoid conflicts during re-creation
DROP TABLE IF EXISTS kpi CASCADE;
DROP TABLE IF EXISTS goal CASCADE;
DROP TABLE IF EXISTS process CASCADE;
DROP TABLE IF EXISTS users CASCADE; -- Changed from 'user' to 'users' to avoid SQL keyword conflict
DROP TABLE IF EXISTS department CASCADE;
DROP TABLE IF EXISTS roles CASCADE; -- Changed from 'role' to 'roles'

-- Create Roles Table
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL -- e.g., 'ADMIN', 'MANAGER', 'DEPARTMENT_HEAD', 'USER'
);

-- Create Department Table
CREATE TABLE department (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id INT REFERENCES department(id) ON DELETE SET NULL, -- Allow parent to be null for top-level departments
    manager_user_id INT -- To be linked to users table later, intentionally no FK yet to avoid circular dependency during creation
);

-- Create Users Table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL, -- Store hashed passwords
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    department_id INT REFERENCES department(id) ON DELETE SET NULL, -- User can be unassigned or department deleted
    role_id INT NOT NULL REFERENCES roles(id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint for manager_user_id in department table now that users table exists
ALTER TABLE department
ADD CONSTRAINT fk_department_manager_user
FOREIGN KEY (manager_user_id) REFERENCES users(id) ON DELETE SET NULL;

-- Create Process Table
CREATE TABLE process (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    department_id INT NOT NULL REFERENCES department(id) ON DELETE CASCADE, -- If department is deleted, its processes are deleted
    created_by_user_id INT NOT NULL REFERENCES users(id) ON DELETE RESTRICT, -- Don't delete user if they created processes
    status VARCHAR(50) DEFAULT 'DRAFT', -- e.g., 'DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED'
    inputs TEXT, -- Detailed inputs if necessary
    outputs TEXT, -- Detailed outputs if necessary
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create Goal Table
CREATE TABLE goal (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    process_id INT REFERENCES process(id) ON DELETE CASCADE, -- Goal is part of a process
    target_value FLOAT, -- General target, could be qualitative or quantitative
    target_metric_description VARCHAR(255), -- Description of what the target value represents
    start_date DATE,
    end_date DATE,
    created_by_user_id INT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    responsible_user_id INT REFERENCES users(id) ON DELETE SET NULL, -- If responsible user is deleted, set to NULL
    department_id INT REFERENCES department(id) ON DELETE CASCADE, -- Goal is associated with a department
    status VARCHAR(50) DEFAULT 'ACTIVE', -- e.g., 'ACTIVE', 'COMPLETED', 'ON_HOLD'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create KPI Table
CREATE TABLE kpi (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    goal_id INT NOT NULL REFERENCES goal(id) ON DELETE CASCADE, -- KPI measures a specific goal
    target_value FLOAT,
    actual_value FLOAT,
    unit VARCHAR(50),
    measurement_frequency VARCHAR(50), -- e.g., 'DAILY', 'WEEKLY', 'MONTHLY'
    responsible_user_id INT REFERENCES users(id) ON DELETE SET NULL,
    last_updated_value_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create Incident Table (as originally defined, seems okay for now)
CREATE TABLE incident (
    id SERIAL PRIMARY KEY,
    department_id INT REFERENCES department(id) ON DELETE SET NULL,
    description TEXT NOT NULL,
    incident_date DATE NOT NULL,
    reported_by_user_id INT REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(50) DEFAULT 'OPEN', -- e.g., 'OPEN', 'INVESTIGATING', 'RESOLVED', 'CLOSED'
    severity VARCHAR(50), -- e.g., 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_process_department_id ON process(department_id);
CREATE INDEX idx_process_created_by_user_id ON process(created_by_user_id);
CREATE INDEX idx_goal_process_id ON goal(process_id);
CREATE INDEX idx_goal_responsible_user_id ON goal(responsible_user_id);
CREATE INDEX idx_goal_department_id ON goal(department_id);
CREATE INDEX idx_kpi_goal_id ON kpi(goal_id);
CREATE INDEX idx_kpi_responsible_user_id ON kpi(responsible_user_id);
CREATE INDEX idx_users_department_id ON users(department_id);
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_incident_department_id ON incident(department_id);
CREATE INDEX idx_incident_reported_by_user_id ON incident(reported_by_user_id);

-- Insert some default roles
INSERT INTO roles (name) VALUES ('ADMIN');
INSERT INTO roles (name) VALUES ('MANAGER'); -- Manages a department or processes
INSERT INTO roles (name) VALUES ('DEPARTMENT_HEAD'); -- Specific type of Manager
INSERT INTO roles (name) VALUES ('USER'); -- Regular user who can input data for their processes/KPIs

-- Sample data (optional, for testing)
-- Note: For this to work without error, ensure the referenced IDs will exist.
-- This might require inserting users first, then departments with manager_user_id.

-- Example: Create a top-level admin user (password is 'admin_password_hashed')
-- INSERT INTO users (username, password_hash, email, first_name, last_name, role_id)
-- VALUES ('admin', 'admin_password_hashed', 'admin@example.com', 'Admin', 'User', (SELECT id from roles WHERE name = 'ADMIN'));

-- Example: Create a department
-- INSERT INTO department (name, manager_user_id)
-- VALUES ('Quality Assurance', (SELECT id from users WHERE username = 'admin'));

-- Example: Create a regular user in that department
-- INSERT INTO users (username, password_hash, email, first_name, last_name, department_id, role_id)
-- VALUES ('qatester', 'qatester_password_hashed', 'qatester@example.com', 'QA', 'Tester',
--         (SELECT id from department WHERE name = 'Quality Assurance'),
--         (SELECT id from roles WHERE name = 'USER'));

-- Note on Security:
-- - Passwords should be hashed using a strong algorithm (e.g., bcrypt) in the backend.
-- - The 'ADMIN' role should have superuser privileges.
-- - 'MANAGER' or 'DEPARTMENT_HEAD' might have rights over their specific department's processes and users.
-- - 'USER' role would have more restricted permissions, perhaps to their own assigned tasks or processes.

\c inspection_db; -- This line was originally at the top, but it's better to connect to the DB after creating it if running this script stand-alone.
-- However, docker-entrypoint-initdb.d scripts typically run within the context of the POSTGRES_DB already.
-- The CREATE DATABASE inspection_db; line at the top is only needed if inspection_db doesn't exist.
-- If docker-compose creates it, that line might cause an error.
-- For docker-entrypoint-initdb.d, it's usually assumed the DB specified by POSTGRES_DB is the current context.
-- Removing the initial CREATE DATABASE and \c commands if this is strictly for docker-entrypoint-initdb.d

-- Final structure for docker-entrypoint-initdb.d:
-- Assume inspection_db is created by docker-compose `POSTGRES_DB: inspection_db`

-- Drop existing tables if they exist to avoid conflicts during re-creation
DROP TABLE IF EXISTS kpi CASCADE;
DROP TABLE IF EXISTS goal CASCADE;
DROP TABLE IF EXISTS process CASCADE;
DROP TABLE IF EXISTS incident CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS department CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- Create Roles Table
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL -- e.g., 'ADMIN', 'MANAGER', 'DEPARTMENT_HEAD', 'USER'
);

-- Create Department Table
CREATE TABLE department (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id INT REFERENCES department(id) ON DELETE SET NULL,
    manager_user_id INT -- To be linked to users table later
);

-- Create Users Table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    department_id INT REFERENCES department(id) ON DELETE SET NULL,
    role_id INT NOT NULL REFERENCES roles(id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint for manager_user_id in department table
ALTER TABLE department
ADD CONSTRAINT fk_department_manager_user
FOREIGN KEY (manager_user_id) REFERENCES users(id) ON DELETE SET NULL;

-- Create Process Table
CREATE TABLE process (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    department_id INT NOT NULL REFERENCES department(id) ON DELETE CASCADE,
    created_by_user_id INT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    status VARCHAR(50) DEFAULT 'DRAFT',
    inputs TEXT,
    outputs TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create Goal Table
CREATE TABLE goal (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    process_id INT REFERENCES process(id) ON DELETE CASCADE,
    target_value FLOAT,
    target_metric_description VARCHAR(255),
    start_date DATE,
    end_date DATE,
    created_by_user_id INT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    responsible_user_id INT REFERENCES users(id) ON DELETE SET NULL,
    department_id INT REFERENCES department(id) ON DELETE CASCADE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create KPI Table
CREATE TABLE kpi (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    goal_id INT NOT NULL REFERENCES goal(id) ON DELETE CASCADE,
    target_value FLOAT,
    actual_value FLOAT,
    unit VARCHAR(50),
    measurement_frequency VARCHAR(50),
    responsible_user_id INT REFERENCES users(id) ON DELETE SET NULL,
    last_updated_value_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create Incident Table
CREATE TABLE incident (
    id SERIAL PRIMARY KEY,
    department_id INT REFERENCES department(id) ON DELETE SET NULL,
    description TEXT NOT NULL,
    incident_date DATE NOT NULL,
    reported_by_user_id INT REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(50) DEFAULT 'OPEN',
    severity VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_process_department_id ON process(department_id);
CREATE INDEX idx_process_created_by_user_id ON process(created_by_user_id);
CREATE INDEX idx_goal_process_id ON goal(process_id);
CREATE INDEX idx_goal_responsible_user_id ON goal(responsible_user_id);
CREATE INDEX idx_goal_department_id ON goal(department_id);
CREATE INDEX idx_kpi_goal_id ON kpi(goal_id);
CREATE INDEX idx_kpi_responsible_user_id ON kpi(responsible_user_id);
CREATE INDEX idx_users_department_id ON users(department_id);
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_incident_department_id ON incident(department_id);
CREATE INDEX idx_incident_reported_by_user_id ON incident(reported_by_user_id);

-- Default roles
INSERT INTO roles (name) VALUES ('ADMIN');
INSERT INTO roles (name) VALUES ('MANAGER');
INSERT INTO roles (name) VALUES ('DEPARTMENT_HEAD');
INSERT INTO roles (name) VALUES ('USER');

-- Note: The \c inspection_db and CREATE DATABASE inspection_db commands have been removed
-- as they are typically handled by the Docker environment when using docker-entrypoint-initdb.d.
-- The script now assumes it's running within the 'inspection_db' database context.
