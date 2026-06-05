package com.tutorassist.homework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorassist.common.PageResult;
import com.tutorassist.homework.controller.HomeworkController;
import com.tutorassist.homework.dto.HomeworkQuery;
import com.tutorassist.homework.dto.HomeworkRequest;
import com.tutorassist.homework.dto.HomeworkVO;
import com.tutorassist.homework.service.HomeworkService;
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

@WebMvcTest(HomeworkController.class)
class HomeworkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HomeworkService homeworkService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void listHomeworks_Success() throws Exception {
        HomeworkVO homework = HomeworkVO.builder()
                .id(1L)
                .title("数学作业")
                .studentId(1L)
                .studentName("张三")
                .subject("数学")
                .status("PENDING")
                .dueDate(LocalDate.now().plusDays(3))
                .build();

        PageResult<HomeworkVO> pageResult = new PageResult<>(List.of(homework), 1, 1, 20);

        when(homeworkService.listHomeworks(any(HomeworkQuery.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/homeworks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].title").value("数学作业"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createHomework_Success() throws Exception {
        HomeworkRequest request = new HomeworkRequest();
        request.setTitle("数学作业");
        request.setStudentId(1L);
        request.setSubject("数学");
        request.setDueDate(LocalDate.now().plusDays(3));

        HomeworkVO homework = HomeworkVO.builder()
                .id(1L)
                .title("数学作业")
                .status("PENDING")
                .build();

        when(homeworkService.createHomework(any(HomeworkRequest.class), eq(1L))).thenReturn(homework);

        mockMvc.perform(post("/api/v1/homeworks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("数学作业"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void gradeHomework_Success() throws Exception {
        HomeworkVO homework = HomeworkVO.builder()
                .id(1L)
                .title("数学作业")
                .status("GRADED")
                .score("95")
                .comment("完成得很好")
                .build();

        when(homeworkService.gradeHomework(eq(1L), any(), eq(1L))).thenReturn(homework);

        mockMvc.perform(put("/api/v1/homeworks/1/grade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\": \"95\", \"comment\": \"完成得很好\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("GRADED"));
    }

    @Test
    void listHomeworks_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/homeworks"))
                .andExpect(status().isUnauthorized());
    }
}
