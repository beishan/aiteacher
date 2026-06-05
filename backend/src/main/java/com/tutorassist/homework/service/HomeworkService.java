package com.tutorassist.homework.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorassist.classroom.entity.VirtualClass;
import com.tutorassist.classroom.mapper.VirtualClassMapper;
import com.tutorassist.common.PageResult;
import com.tutorassist.common.exception.BusinessException;
import com.tutorassist.homework.dto.*;
import com.tutorassist.homework.entity.Homework;
import com.tutorassist.homework.entity.HomeworkSubmission;
import com.tutorassist.homework.mapper.HomeworkMapper;
import com.tutorassist.homework.mapper.HomeworkSubmissionMapper;
import com.tutorassist.student.entity.Student;
import com.tutorassist.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeworkService {

    private final HomeworkMapper homeworkMapper;
    private final HomeworkSubmissionMapper submissionMapper;
    private final StudentMapper studentMapper;
    private final VirtualClassMapper classMapper;
    private final ObjectMapper objectMapper;

    // ==================== 作业 CRUD ====================

    public PageResult<HomeworkVO> listHomeworks(HomeworkQuery query) {
        LambdaQueryWrapper<Homework> wrapper = new LambdaQueryWrapper<>();

        if (query.getStudentId() != null) {
            wrapper.eq(Homework::getStudentId, query.getStudentId());
        }
        if (query.getClassId() != null) {
            wrapper.eq(Homework::getClassId, query.getClassId());
        }
        if (StringUtils.hasText(query.getSubject())) {
            wrapper.eq(Homework::getSubject, query.getSubject());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Homework::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(Homework::getCreatedAt);

        Page<Homework> page = homeworkMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()),
                wrapper
        );

        List<HomeworkVO> records = page.getRecords().stream()
                .map(this::toHomeworkVO)
                .toList();

        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public HomeworkVO getHomework(Long id) {
        Homework homework = homeworkMapper.selectById(id);
        if (homework == null) {
            throw new BusinessException(404, "作业不存在");
        }
        return toHomeworkVO(homework);
    }

    @Transactional
    public HomeworkVO createHomework(HomeworkRequest request, Long operatorId) {
        // 验证学生或班级存在
        if (request.getStudentId() != null) {
            Student student = studentMapper.selectById(request.getStudentId());
            if (student == null) {
                throw new BusinessException(404, "学生不存在");
            }
        }
        if (request.getClassId() != null) {
            VirtualClass vc = classMapper.selectById(request.getClassId());
            if (vc == null) {
                throw new BusinessException(404, "班级不存在");
            }
        }

        Homework homework = new Homework();
        homework.setTitle(request.getTitle());
        homework.setStudentId(request.getStudentId());
        homework.setClassId(request.getClassId());
        homework.setSubject(request.getSubject());
        homework.setContent(request.getContent());
        homework.setDueDate(request.getDueDate());
        homework.setScoreType(request.getScoreType());
        homework.setStatus("PENDING");

        homeworkMapper.insert(homework);
        log.info("创建作业：{}，操作人：{}", homework.getTitle(), operatorId);

        return toHomeworkVO(homework);
    }

    @Transactional
    public HomeworkVO updateHomework(Long id, HomeworkRequest request, Long operatorId) {
        Homework homework = homeworkMapper.selectById(id);
        if (homework == null) {
            throw new BusinessException(404, "作业不存在");
        }

        homework.setTitle(request.getTitle());
        homework.setStudentId(request.getStudentId());
        homework.setClassId(request.getClassId());
        homework.setSubject(request.getSubject());
        homework.setContent(request.getContent());
        homework.setDueDate(request.getDueDate());
        homework.setScoreType(request.getScoreType());

        homeworkMapper.updateById(homework);
        log.info("更新作业：{}，操作人：{}", homework.getTitle(), operatorId);

        return toHomeworkVO(homework);
    }

    @Transactional
    public void deleteHomework(Long id, Long operatorId) {
        Homework homework = homeworkMapper.selectById(id);
        if (homework == null) {
            throw new BusinessException(404, "作业不存在");
        }

        // 删除相关提交
        LambdaQueryWrapper<HomeworkSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeworkSubmission::getHomeworkId, id);
        submissionMapper.delete(wrapper);

        homeworkMapper.deleteById(id);
        log.info("删除作业：{}，操作人：{}", homework.getTitle(), operatorId);
    }

    // ==================== 作业提交 ====================

    @Transactional
    public SubmissionVO submitHomework(Long homeworkId, SubmissionRequest request, Long operatorId) {
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            throw new BusinessException(404, "作业不存在");
        }

        Long studentId = request.getStudentId() != null ? request.getStudentId() : homework.getStudentId();
        if (studentId == null) {
            throw new BusinessException("请指定提交学生");
        }

        HomeworkSubmission submission = new HomeworkSubmission();
        submission.setHomeworkId(homeworkId);
        submission.setStudentId(studentId);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setFiles(toJson(request.getFiles()));
        submission.setContent(request.getContent());
        submission.setStatus("SUBMITTED");

        submissionMapper.insert(submission);

        // 更新作业状态
        homework.setStatus("SUBMITTED");
        homeworkMapper.updateById(homework);

        log.info("提交作业：{}，学生ID：{}，操作人：{}", homework.getTitle(), studentId, operatorId);

        return toSubmissionVO(submission);
    }

    public List<SubmissionVO> getSubmissions(Long homeworkId) {
        LambdaQueryWrapper<HomeworkSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeworkSubmission::getHomeworkId, homeworkId)
                .orderByDesc(HomeworkSubmission::getSubmittedAt);

        return submissionMapper.selectList(wrapper).stream()
                .map(this::toSubmissionVO)
                .toList();
    }

    // ==================== 作业批改 ====================

    @Transactional
    public HomeworkVO gradeHomework(Long homeworkId, GradeRequest request, Long operatorId) {
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            throw new BusinessException(404, "作业不存在");
        }

        homework.setScore(request.getScore());
        homework.setComment(request.getComment());
        homework.setStatus("GRADED");

        homeworkMapper.updateById(homework);
        log.info("批改作业：{}，分数：{}，操作人：{}", homework.getTitle(), request.getScore(), operatorId);

        return toHomeworkVO(homework);
    }

    // ==================== 转换方法 ====================

    private HomeworkVO toHomeworkVO(Homework homework) {
        String studentName = null;
        if (homework.getStudentId() != null) {
            Student student = studentMapper.selectById(homework.getStudentId());
            if (student != null) {
                studentName = student.getName();
            }
        }

        String className = null;
        if (homework.getClassId() != null) {
            VirtualClass vc = classMapper.selectById(homework.getClassId());
            if (vc != null) {
                className = vc.getName();
            }
        }

        // 统计提交数
        LambdaQueryWrapper<HomeworkSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeworkSubmission::getHomeworkId, homework.getId());
        Long submissionCount = submissionMapper.selectCount(wrapper);

        return HomeworkVO.builder()
                .id(homework.getId())
                .title(homework.getTitle())
                .studentId(homework.getStudentId())
                .studentName(studentName)
                .classId(homework.getClassId())
                .className(className)
                .subject(homework.getSubject())
                .content(homework.getContent())
                .dueDate(homework.getDueDate())
                .status(homework.getStatus())
                .scoreType(homework.getScoreType())
                .score(homework.getScore())
                .comment(homework.getComment())
                .aiReport(homework.getAiReport())
                .submissionCount(submissionCount != null ? submissionCount.intValue() : 0)
                .createdAt(homework.getCreatedAt())
                .build();
    }

    private SubmissionVO toSubmissionVO(HomeworkSubmission submission) {
        Student student = studentMapper.selectById(submission.getStudentId());
        String studentName = student != null ? student.getName() : "未知";

        return SubmissionVO.builder()
                .id(submission.getId())
                .homeworkId(submission.getHomeworkId())
                .studentId(submission.getStudentId())
                .studentName(studentName)
                .submittedAt(submission.getSubmittedAt())
                .files(fromJson(submission.getFiles()))
                .content(submission.getContent())
                .status(submission.getStatus())
                .build();
    }

    private String toJson(List<String> list) {
        if (list == null) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<String> fromJson(String json) {
        if (!StringUtils.hasText(json)) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
