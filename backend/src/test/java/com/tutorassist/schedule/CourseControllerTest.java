package com.tutorassist.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorassist.common.PageResult;
import com.tutorassist.schedule.controller.CourseController;
import com.tutorassist.schedule.dto.CourseQuery;
import com.tutorassist.schedule.dto.CourseRequest;
import com.tutorassist.schedule.dto.CourseVO;
import com.tutorassist.schedule.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void listCourses_Success() throws Exception {
        CourseVO course = CourseVO.builder()
                .id(1L)
                .studentId(1L)
                .studentName("张三")
                .subject("数学")
                .courseType("ONE_ON_ONE")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .status("SCHEDULED")
                .build();

        PageResult<CourseVO> pageResult = new PageResult<>(List.of(course), 1, 1, 50);

        when(courseService.listCourses(any(CourseQuery.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].subject").value("数学"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCourse_Success() throws Exception {
        CourseRequest request = new CourseRequest();
        request.setStudentId(1L);
        request.setSubject("数学");
        request.setCourseType("ONE_ON_ONE");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        CourseVO course = CourseVO.builder()
                .id(1L)
                .studentId(1L)
                .subject("数学")
                .courseType("ONE_ON_ONE")
                .status("SCHEDULED")
                .build();

        when(courseService.createCourse(any(CourseRequest.class), eq(1L)))
                .thenReturn(List.of(course));

        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getCalendarCourses_Success() throws Exception {
        CourseVO course = CourseVO.builder()
                .id(1L)
                .subject("数学")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .build();

        when(courseService.getCalendarCourses(any(), any(), any(), any(), any()))
                .thenReturn(List.of(course));

        mockMvc.perform(get("/api/v1/courses/calendar")
                        .param("start", LocalDateTime.now().toString())
                        .param("end", LocalDateTime.now().plusDays(7).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
