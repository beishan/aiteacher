package com.tutorassist.student;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorassist.common.PageResult;
import com.tutorassist.student.controller.StudentController;
import com.tutorassist.student.dto.StudentQuery;
import com.tutorassist.student.dto.StudentRequest;
import com.tutorassist.student.dto.StudentVO;
import com.tutorassist.student.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void listStudents_Success() throws Exception {
        StudentVO student = StudentVO.builder()
                .id(1L)
                .name("张三")
                .grade("JUNIOR_1")
                .status("ACTIVE")
                .enrollmentDate(LocalDate.now())
                .build();

        PageResult<StudentVO> pageResult = new PageResult<>(List.of(student), 1, 1, 20);

        when(studentService.listStudents(any(StudentQuery.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].name").value("张三"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getStudent_Success() throws Exception {
        StudentVO student = StudentVO.builder()
                .id(1L)
                .name("张三")
                .grade("JUNIOR_1")
                .status("ACTIVE")
                .build();

        when(studentService.getStudent(1L)).thenReturn(student);

        mockMvc.perform(get("/api/v1/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("张三"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createStudent_Success() throws Exception {
        StudentRequest request = new StudentRequest();
        request.setName("李四");
        request.setGrade("JUNIOR_2");
        request.setParentPhone("13800138000");
        request.setEnrollmentDate(LocalDate.now());

        StudentVO student = StudentVO.builder()
                .id(2L)
                .name("李四")
                .grade("JUNIOR_2")
                .status("ACTIVE")
                .build();

        when(studentService.createStudent(any(StudentRequest.class), eq(1L))).thenReturn(student);

        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("李四"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteStudent_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void listStudents_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/students"))
                .andExpect(status().isUnauthorized());
    }
}
