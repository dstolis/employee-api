CREATE TABLE employees
(
    id        UUID PRIMARY KEY,
    email     VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    birthday  DATE         NOT NULL
);

-- Create employee_hobbies table
CREATE TABLE employee_hobbies
(
    id          SERIAL PRIMARY KEY,
    employee_id UUID REFERENCES employees (id),
    hobby       VARCHAR(255) NOT NULL
);
