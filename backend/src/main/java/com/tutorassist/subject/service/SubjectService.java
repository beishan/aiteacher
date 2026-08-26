package com.tutorassist.subject.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorassist.classroom.entity.VirtualClass;
import com.tutorassist.classroom.mapper.VirtualClassMapper;
import com.tutorassist.common.exception.BusinessException;
import com.tutorassist.grade.entity.ExamRecord;
import com.tutorassist.grade.mapper.ExamRecordMapper;
import com.tutorassist.homework.entity.Homework;
import com.tutorassist.homework.mapper.HomeworkMapper;
import com.tutorassist.material.entity.Material;
import com.tutorassist.material.mapper.MaterialMapper;
import com.tutorassist.schedule.entity.Course;
import com.tutorassist.schedule.mapper.CourseMapper;
import com.tutorassist.student.entity.Student;
import com.tutorassist.student.entity.StudentFee;
import com.tutorassist.student.mapper.StudentFeeMapper;
import com.tutorassist.student.mapper.StudentMapper;
import com.tutorassist.subject.dto.SubjectRequest;
import com.tutorassist.subject.dto.SubjectVO;
import com.tutorassist.subject.entity.Subject;
import com.tutorassist.subject.mapper.SubjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectMapper subjectMapper;
    private final CourseMapper courseMapper;
    private final HomeworkMapper homeworkMapper;
    private final ExamRecordMapper examRecordMapper;
    private final VirtualClassMapper virtualClassMapper;
    private final MaterialMapper materialMapper;
    private final StudentFeeMapper studentFeeMapper;
    private final StudentMapper studentMapper;
    private final ObjectMapper objectMapper;

    public List<SubjectVO> listSubjects() {
        return subjectMapper.selectList(new LambdaQueryWrapper<Subject>()
                        .orderByAsc(Subject::getSortOrder)
                        .orderByAsc(Subject::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Transactional
    public SubjectVO createSubject(SubjectRequest request) {
        String name = normalizeName(request.getName());
        ensureNameAvailable(name, null);

        Subject subject = new Subject();
        subject.setName(name);
        subject.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : nextSortOrder());
        subjectMapper.insert(subject);
        return toVO(subject);
    }

    @Transactional
    public SubjectVO updateSubject(Long id, SubjectRequest request) {
        Subject subject = requireSubject(id);
        String oldName = subject.getName();
        String newName = normalizeName(request.getName());
        ensureNameAvailable(newName, id);

        if (!oldName.equals(newName)) {
            renameReferences(oldName, newName);
            subject.setName(newName);
        }
        if (request.getSortOrder() != null) {
            subject.setSortOrder(request.getSortOrder());
        }
        subjectMapper.updateById(subject);
        return toVO(subject);
    }

    @Transactional
    public void deleteSubject(Long id) {
        Subject subject = requireSubject(id);
        Usage usage = getUsage(subject.getName());
        if (usage.courseCount() > 0) {
            throw new BusinessException("该科目已有" + usage.courseCount() + "节课程，不能删除；请先调整或删除相关排课");
        }
        if (usage.totalCount() > 0) {
            throw new BusinessException("该科目已被学生、班级、作业、成绩、资料或收费记录使用，不能删除");
        }
        subjectMapper.deleteById(id);
    }

    private Subject requireSubject(Long id) {
        Subject subject = subjectMapper.selectById(id);
        if (subject == null) {
            throw new BusinessException(404, "科目不存在");
        }
        return subject;
    }

    private void ensureNameAvailable(String name, Long excludeId) {
        LambdaQueryWrapper<Subject> wrapper = new LambdaQueryWrapper<Subject>()
                .apply("LOWER(name) = LOWER({0})", name);
        if (excludeId != null) {
            wrapper.ne(Subject::getId, excludeId);
        }
        if (subjectMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("科目名称已存在");
        }
    }

    private String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException("科目名称不能为空");
        }
        return normalized;
    }

    private int nextSortOrder() {
        Subject last = subjectMapper.selectOne(new LambdaQueryWrapper<Subject>()
                .orderByDesc(Subject::getSortOrder)
                .orderByDesc(Subject::getId)
                .last("LIMIT 1"));
        return last == null ? 10 : Math.min(last.getSortOrder() + 10, 9999);
    }

    private SubjectVO toVO(Subject subject) {
        Usage usage = getUsage(subject.getName());
        return SubjectVO.builder()
                .id(subject.getId())
                .name(subject.getName())
                .sortOrder(subject.getSortOrder())
                .courseCount(usage.courseCount())
                .usageCount(usage.totalCount())
                .canDelete(usage.totalCount() == 0)
                .build();
    }

    private Usage getUsage(String name) {
        long courseCount = count(courseMapper, Course::getSubject, name);
        long otherCount = count(homeworkMapper, Homework::getSubject, name)
                + count(examRecordMapper, ExamRecord::getSubject, name)
                + count(virtualClassMapper, VirtualClass::getSubject, name)
                + count(materialMapper, Material::getSubject, name)
                + count(studentFeeMapper, StudentFee::getSubject, name)
                + countStudentsUsing(name);
        return new Usage(courseCount, courseCount + otherCount);
    }

    private <T> long count(com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper,
                           com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, ?> field,
                           String value) {
        return mapper.selectCount(new LambdaQueryWrapper<T>().eq(field, value));
    }

    private long countStudentsUsing(String name) {
        return studentMapper.selectList(new LambdaQueryWrapper<Student>().isNotNull(Student::getSubjects))
                .stream()
                .filter(student -> parseSubjects(student.getSubjects()).contains(name))
                .count();
    }

    private void renameReferences(String oldName, String newName) {
        courseMapper.update(null, new LambdaUpdateWrapper<Course>().eq(Course::getSubject, oldName).set(Course::getSubject, newName));
        homeworkMapper.update(null, new LambdaUpdateWrapper<Homework>().eq(Homework::getSubject, oldName).set(Homework::getSubject, newName));
        examRecordMapper.update(null, new LambdaUpdateWrapper<ExamRecord>().eq(ExamRecord::getSubject, oldName).set(ExamRecord::getSubject, newName));
        virtualClassMapper.update(null, new LambdaUpdateWrapper<VirtualClass>().eq(VirtualClass::getSubject, oldName).set(VirtualClass::getSubject, newName));
        materialMapper.update(null, new LambdaUpdateWrapper<Material>().eq(Material::getSubject, oldName).set(Material::getSubject, newName));
        studentFeeMapper.update(null, new LambdaUpdateWrapper<StudentFee>().eq(StudentFee::getSubject, oldName).set(StudentFee::getSubject, newName));

        studentMapper.selectList(new LambdaQueryWrapper<Student>().isNotNull(Student::getSubjects)).forEach(student -> {
            List<String> subjects = parseSubjects(student.getSubjects());
            if (!subjects.contains(oldName)) return;
            List<String> renamed = new ArrayList<>(subjects);
            for (int index = 0; index < renamed.size(); index++) {
                if (oldName.equals(renamed.get(index))) renamed.set(index, newName);
            }
            try {
                student.setSubjects(objectMapper.writeValueAsString(renamed));
                studentMapper.updateById(student);
            } catch (Exception e) {
                throw new BusinessException("同步学生科目失败");
            }
        });
    }

    private List<String> parseSubjects(String value) {
        if (value == null || value.isBlank()) return List.of();
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private record Usage(long courseCount, long totalCount) {
    }
}
