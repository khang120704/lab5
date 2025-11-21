package com.student.controller;

import com.student.dao.StudentDAO;
import com.student.model.Student;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/student")
public class StudentController extends HttpServlet {
    
    private StudentDAO studentDAO;
    
    @Override
    public void init() {
        studentDAO = new StudentDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if (action == null) {
            action = "list";
        }
        
        switch (action) {
            case "new":
                showNewForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "delete":
                deleteStudent(request, response);
                break;
            case "search":
                searchStudents(request, response);
                break;
            case "sort":
                sortStudents(request, response);
                break;
            case "filter":
                filterByMajor(request, response);
                break;
            default:
                listStudents(request, response);
                break;
        }
    }
    private void sortStudents(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

            String sortBy = request.getParameter("sortBy");
            String order = request.getParameter("order");

            // Call DAO
            List<Student> students = studentDAO.getStudentsSorted(sortBy, order);

            // Set attributes for JSP
            request.setAttribute("students", students);
            request.setAttribute("sortBy", sortBy);
            request.setAttribute("order", order);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-list.jsp");
            dispatcher.forward(request, response);
        }
    private void filterByMajor(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

            String major = request.getParameter("major");

            // If empty → show all
            List<Student> students;
            if (major == null || major.isEmpty()) {
                students = studentDAO.getAllStudents();
            } else {
                students = studentDAO.getStudentsByMajor(major);
            }

            // Set attributes
            request.setAttribute("students", students);
            request.setAttribute("selectedMajor", major);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-list.jsp");
            dispatcher.forward(request, response);
        }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        switch (action) {
            case "insert":
                insertStudent(request, response);
                break;
            case "update":
                updateStudent(request, response);
                break;
        }
    }
    
    // List all students
    private void listStudents(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<Student> students = studentDAO.getAllStudents();
        request.setAttribute("students", students);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-list.jsp");
        dispatcher.forward(request, response);
    }
    
    // Show form for new student
    private void showNewForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-form.jsp");
        dispatcher.forward(request, response);
    }
    
    // Show form for editing student
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        Student existingStudent = studentDAO.getStudentById(id);
        
        request.setAttribute("student", existingStudent);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-form.jsp");
        dispatcher.forward(request, response);
    }
    
    // Insert new student
    private void insertStudent(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {

    // 1. Get parameters
    String studentCode = request.getParameter("studentCode");
    String fullName = request.getParameter("fullName");
    String email = request.getParameter("email");
    String major = request.getParameter("major");

    // Create student object
    Student student = new Student(studentCode, fullName, email, major);

    // 2. Validate
    if (!validateStudent(student, request)) {
        // Preserve entered data
        request.setAttribute("student", student);

        // Forward back to form
        RequestDispatcher dispatcher = 
                request.getRequestDispatcher("/views/student-form.jsp");
        dispatcher.forward(request, response);
        return; // STOP here
    }

    // 3. Proceed with insert
    if (studentDAO.addStudent(student)) {
        response.sendRedirect("student?action=list&message=Student added successfully");
    } else {
        response.sendRedirect("student?action=list&error=Failed to add student");
    }
}
    
    // Update student
    private void updateStudent(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        int id = Integer.parseInt(request.getParameter("id"));
        String studentCode = request.getParameter("studentCode");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String major = request.getParameter("major");

        Student student = new Student(studentCode, fullName, email, major);
        student.setId(id);

        // 1. Validate
        if (!validateStudent(student, request)) {
            // Preserve entered data
            request.setAttribute("student", student);

            // Forward back to form
            RequestDispatcher dispatcher =
                    request.getRequestDispatcher("/views/student-form.jsp");
            dispatcher.forward(request, response);
            return; // STOP here
        }

        // 2. Proceed with update
        if (studentDAO.updateStudent(student)) {
            response.sendRedirect("student?action=list&message=Student updated successfully");
        } else {
            response.sendRedirect("student?action=list&error=Failed to update student");
        }
    }

    
    // Delete student
    private void deleteStudent(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        
        if (studentDAO.deleteStudent(id)) {
            response.sendRedirect("student?action=list&message=Student deleted successfully");
        } else {
            response.sendRedirect("student?action=list&error=Failed to delete student");
        }
    }
    // Search students
    private void searchStudents(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Get keyword from request
        String keyword = request.getParameter("keyword");

        // 2. Handle empty/null keyword → show all students
        List<Student> students;
        if (keyword == null || keyword.trim().isEmpty()) {
            students = studentDAO.getAllStudents();
            keyword = ""; // so the input field stays empty, not null
        } else {
            students = studentDAO.searchStudents(keyword.trim());
        }

        // 3. Set attributes
        request.setAttribute("students", students);
        request.setAttribute("keyword", keyword);

        // 4. Forward to list page
        RequestDispatcher dispatcher = request.getRequestDispatcher("/views/student-list.jsp");
        dispatcher.forward(request, response);
    }
    private boolean validateStudent(Student student, HttpServletRequest request) {
        boolean isValid = true;

        // ===== Validate Student Code =====
        String code = student.getStudentCode();
        String codePattern = "[A-Z]{2}[0-9]{3,}";

        if (code == null || code.trim().isEmpty()) {
            request.setAttribute("errorCode", "Student code is required.");
            isValid = false;
        } else if (!code.matches(codePattern)) {
            request.setAttribute("errorCode", "Invalid format. Use 2 letters + 3+ digits (e.g., SV001)");
            isValid = false;
        }

        // ===== Validate Full Name =====
        String name = student.getFullName();
        if (name == null || name.trim().isEmpty()) {
            request.setAttribute("errorName", "Full name is required.");
            isValid = false;
        } else if (name.trim().length() < 2) {
            request.setAttribute("errorName", "Full name must be at least 2 characters.");
            isValid = false;
        }

        // ===== Validate Email (Optional field) =====
        String email = student.getEmail();
        String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";

        if (email != null && !email.trim().isEmpty()) {
            if (!email.matches(emailPattern)) {
                request.setAttribute("errorEmail", "Invalid email format.");
                isValid = false;
            }
        }

        // ===== Validate Major =====
        String major = student.getMajor();
        if (major == null || major.trim().isEmpty()) {
            request.setAttribute("errorMajor", "Major is required.");
            isValid = false;
        }

        return isValid;
    }
}
