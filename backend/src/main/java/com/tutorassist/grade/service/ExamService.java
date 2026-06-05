package com.tutorassist.grade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutorassist.common.PageResult;
import com.tutorassist.common.exception.BusinessException;
import com.tutorassist.grade.dto.*;
import com.tutorassist.grade.entity.ExamRecord;
import com.tutorassist.grade.mapper.ExamRecordMapper;
import com.tutorassist.student.entity.Student;
import com.tutorassist.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRecordMapper examMapper;
    private final StudentMapper studentMapper;

    public PageResult<ExamVO> listExams(ExamQuery query) {
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();

        if (query.getStudentId() != null) {
            wrapper.eq(ExamRecord::getStudentId, query.getStudentId());
        }
        if (StringUtils.hasText(query.getSubject())) {
            wrapper.eq(ExamRecord::getSubject, query.getSubject());
        }
        if (StringUtils.hasText(query.getExamType())) {
            wrapper.eq(ExamRecord::getExamType, query.getExamType());
        }

        wrapper.orderByDesc(ExamRecord::getExamDate);

        Page<ExamRecord> page = examMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()),
                wrapper
        );

        List<ExamVO> records = page.getRecords().stream()
                .map(this::toExamVO)
                .toList();

        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public ExamVO getExam(Long id) {
        ExamRecord exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException(404, "成绩记录不存在");
        }
        return toExamVO(exam);
    }

    @Transactional
    public ExamVO createExam(ExamRequest request, Long operatorId) {
        Student student = studentMapper.selectById(request.getStudentId());
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        ExamRecord exam = new ExamRecord();
        exam.setStudentId(request.getStudentId());
        exam.setExamName(request.getExamName());
        exam.setExamType(request.getExamType());
        exam.setExamDate(request.getExamDate());
        exam.setSubject(request.getSubject());
        exam.setScore(request.getScore());
        exam.setFullScore(request.getFullScore());
        exam.setClassRank(request.getClassRank());
        exam.setGradeRank(request.getGradeRank());
        exam.setPaperUrl(request.getPaperUrl());
        exam.setAnalysis(request.getAnalysis());

        examMapper.insert(exam);
        log.info("新增成绩：学生{}，考试{}，操作人：{}", student.getName(), request.getExamName(), operatorId);

        return toExamVO(exam);
    }

    @Transactional
    public ExamVO updateExam(Long id, ExamRequest request, Long operatorId) {
        ExamRecord exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException(404, "成绩记录不存在");
        }

        exam.setExamName(request.getExamName());
        exam.setExamType(request.getExamType());
        exam.setExamDate(request.getExamDate());
        exam.setSubject(request.getSubject());
        exam.setScore(request.getScore());
        exam.setFullScore(request.getFullScore());
        exam.setClassRank(request.getClassRank());
        exam.setGradeRank(request.getGradeRank());
        exam.setPaperUrl(request.getPaperUrl());
        exam.setAnalysis(request.getAnalysis());

        examMapper.updateById(exam);
        log.info("更新成绩：{}，操作人：{}", exam.getExamName(), operatorId);

        return toExamVO(exam);
    }

    @Transactional
    public void deleteExam(Long id, Long operatorId) {
        ExamRecord exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException(404, "成绩记录不存在");
        }
        examMapper.deleteById(id);
        log.info("删除成绩：{}，操作人：{}", exam.getExamName(), operatorId);
    }

    public List<ExamVO> getStudentExams(Long studentId) {
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getStudentId, studentId)
                .orderByDesc(ExamRecord::getExamDate);

        return examMapper.selectList(wrapper).stream()
                .map(this::toExamVO)
                .toList();
    }

    public List<TrendVO> getStudentTrend(Long studentId) {
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getStudentId, studentId)
                .orderByAsc(ExamRecord::getExamDate);

        List<ExamRecord> exams = examMapper.selectList(wrapper);

        // 按科目分组
        Map<String, List<ExamRecord>> bySubject = exams.stream()
                .collect(Collectors.groupingBy(ExamRecord::getSubject));

        List<TrendVO> trends = new ArrayList<>();
        for (Map.Entry<String, List<ExamRecord>> entry : bySubject.entrySet()) {
            List<TrendVO.TrendPoint> points = entry.getValue().stream()
                    .map(exam -> TrendVO.TrendPoint.builder()
                            .examDate(exam.getExamDate())
                            .examName(exam.getExamName())
                            .score(exam.getScore())
                            .fullScore(exam.getFullScore())
                            .percentage(exam.getScore()
                                    .multiply(new BigDecimal("100"))
                                    .divide(exam.getFullScore(), 1, RoundingMode.HALF_UP))
                            .build())
                    .toList();

            trends.add(TrendVO.builder()
                    .subject(entry.getKey())
                    .points(points)
                    .build());
        }

        return trends;
    }

    private ExamVO toExamVO(ExamRecord exam) {
        String studentName = null;
        if (exam.getStudentId() != null) {
            Student student = studentMapper.selectById(exam.getStudentId());
            if (student != null) {
                studentName = student.getName();
            }
        }

        BigDecimal percentage = null;
        if (exam.getScore() != null && exam.getFullScore() != null && exam.getFullScore().compareTo(BigDecimal.ZERO) > 0) {
            percentage = exam.getScore()
                    .multiply(new BigDecimal("100"))
                    .divide(exam.getFullScore(), 1, RoundingMode.HALF_UP);
        }

        return ExamVO.builder()
                .id(exam.getId())
                .studentId(exam.getStudentId())
                .studentName(studentName)
                .examName(exam.getExamName())
                .examType(exam.getExamType())
                .examDate(exam.getExamDate())
                .subject(exam.getSubject())
                .score(exam.getScore())
                .fullScore(exam.getFullScore())
                .percentage(percentage)
                .classRank(exam.getClassRank())
                .gradeRank(exam.getGradeRank())
                .paperUrl(exam.getPaperUrl())
                .analysis(exam.getAnalysis())
                .createdAt(exam.getCreatedAt())
                .build();
    }
}
