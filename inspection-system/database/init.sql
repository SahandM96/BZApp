CREATE DATABASE inspection_db;

\c inspection_db

CREATE TABLE department (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    parent_id INT REFERENCES department(id)
);

CREATE TABLE process (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    department_id INT REFERENCES department(id),
    inputs TEXT,
    outputs TEXT
);

CREATE TABLE kpi (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    target_value FLOAT,
    actual_value FLOAT,
    unit VARCHAR(50),
    process_id INT REFERENCES process(id)
);

CREATE TABLE goal (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    target_value FLOAT,
    start_date DATE,
    end_date DATE,
    department_id INT REFERENCES department(id)
);

CREATE TABLE incident (
    id SERIAL PRIMARY KEY,
    department_id INT REFERENCES department(id),
    description TEXT,
    incident_date DATE,
    reported_year INT
);

CREATE INDEX idx_process_department_id ON process(department_id);
CREATE INDEX idx_kpi_process_id ON kpi(process_id);
CREATE INDEX idx_goal_department_id ON goal(department_id);
