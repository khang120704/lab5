CREATE DATABASE student_management;
USE student_management;

CREATE TABLE students (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_code VARCHAR(10) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    major VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO students (student_code, full_name, email, major) VALUES
('SV001', 'John Smith', 'john.smith@email.com', 'Computer Science'),
('SV002', 'Emily Johnson', 'emily.j@email.com', 'Information Technology'),
('SV003', 'Michael Brown', 'michael.b@email.com', 'Software Engineering'),
('SV004', 'Sarah Davis', 'sarah.d@email.com', 'Data Science'),
('SV005', 'David Wilson', 'david.w@email.com', 'Computer Science');
