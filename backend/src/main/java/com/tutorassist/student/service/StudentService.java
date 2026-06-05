package com.tutorassist.student.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorassist.common.PageResult;
import com.tutorassist.common.exception.BusinessException;
import com.tutorassist.student.dto.*;
import com.tutorassist.student.entity.FeeRecord;
import com.tutorassist.student.entity.Student;
import com.tutorassist.student.entity.StudentFee;
import com.tutorassist.student.mapper.FeeRecordMapper;
import com.tutorassist.student.mapper.StudentFeeMapper;
import com.tutorassist.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentMapper studentMapper;
    private final StudentFeeMapper studentFeeMapper;
    private final FeeRecordMapper feeRecordMapper;
    private final ObjectMapper objectMapper;

    // ==================== 学生 CRUD ====================

    public PageResult<StudentVO> listStudents(StudentQuery query) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getName())) {
            wrapper.like(Student::getName, query.getName());
        }
        if (StringUtils.hasText(query.getGrade())) {
            wrapper.eq(Student::getGrade, query.getGrade());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Student::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getSubject())) {
            wrapper.like(Student::getSubjects, query.getSubject());
        }

        wrapper.orderByDesc(Student::getCreatedAt);

        Page<Student> page = studentMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()),
                wrapper
        );

        List<StudentVO> records = page.getRecords().stream()
                .map(this::toStudentVO)
                .toList();

        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public StudentVO getStudent(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }
        return toStudentVO(student);
    }

    @Transactional
    public StudentVO createStudent(StudentRequest request, Long operatorId) {
        Student student = new Student();
        student.setName(request.getName());
        student.setGender(request.getGender());
        student.setBirthDate(request.getBirthDate());
        student.setGrade(request.getGrade());
        student.setSchool(request.getSchool());
        student.setSubjects(toJsonStrings(request.getSubjects()));
        student.setSource(request.getSource());
        student.setAddress(request.getAddress());
        student.setPhone(request.getPhone());
        student.setParentName(request.getParentName());
        student.setParentPhone(request.getParentPhone());
        student.setParentRelation(request.getParentRelation());
        student.setRemark(request.getRemark());
        student.setEnrollmentDate(request.getEnrollmentDate());
        student.setStatus("ACTIVE");
        student.setTags(toJsonStrings(request.getTags()));

        studentMapper.insert(student);
        log.info("新增学生：{}，操作人：{}", student.getName(), operatorId);

        return toStudentVO(student);
    }

    @Transactional
    public StudentVO updateStudent(Long id, StudentRequest request, Long operatorId) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        student.setName(request.getName());
        student.setGender(request.getGender());
        student.setBirthDate(request.getBirthDate());
        student.setGrade(request.getGrade());
        student.setSchool(request.getSchool());
        student.setSubjects(toJsonStrings(request.getSubjects()));
        student.setSource(request.getSource());
        student.setAddress(request.getAddress());
        student.setPhone(request.getPhone());
        student.setParentName(request.getParentName());
        student.setParentPhone(request.getParentPhone());
        student.setParentRelation(request.getParentRelation());
        student.setRemark(request.getRemark());
        student.setEnrollmentDate(request.getEnrollmentDate());
        student.setTags(toJsonStrings(request.getTags()));

        studentMapper.updateById(student);
        log.info("更新学生：{}，操作人：{}", student.getName(), operatorId);

        return toStudentVO(student);
    }

    @Transactional
    public void deleteStudent(Long id, Long operatorId) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }
        studentMapper.deleteById(id);
        log.info("删除学生：{}，操作人：{}", student.getName(), operatorId);
    }

    @Transactional
    public void updateStudentStatus(Long id, String status, Long operatorId) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }
        student.setStatus(status);
        studentMapper.updateById(student);
        log.info("更新学生状态：{} -> {}，操作人：{}", student.getName(), status, operatorId);
    }

    // ==================== 课时费管理 ====================

    public List<StudentFeeVO> listStudentFees(Long studentId) {
        LambdaQueryWrapper<StudentFee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentFee::getStudentId, studentId)
                .orderByDesc(StudentFee::getCreatedAt);

        return studentFeeMapper.selectList(wrapper).stream()
                .map(this::toStudentFeeVO)
                .toList();
    }

    @Transactional
    public StudentFeeVO createStudentFee(Long studentId, StudentFeeRequest request, Long operatorId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        StudentFee fee = new StudentFee();
        fee.setStudentId(studentId);
        fee.setFeeType(request.getFeeType());
        fee.setUnitPrice(request.getUnitPrice());
        fee.setPrepaidCount(request.getPrepaidCount());
        fee.setRemainingCount(request.getPrepaidCount());
        fee.setPeriodStart(request.getPeriodStart());
        fee.setPeriodEnd(request.getPeriodEnd());
        fee.setSubject(request.getSubject());
        fee.setStatus("ACTIVE");
        fee.setRemark(request.getRemark());
        fee.setPriceTiers(toJson(request.getPriceTiers()));

        studentFeeMapper.insert(fee);
        log.info("新增课时费：学生{}，类型{}，操作人：{}", student.getName(), request.getFeeType(), operatorId);

        return toStudentFeeVO(fee);
    }

    @Transactional
    public StudentFeeVO updateStudentFee(Long studentId, Long feeId, StudentFeeRequest request, Long operatorId) {
        StudentFee fee = studentFeeMapper.selectById(feeId);
        if (fee == null || !fee.getStudentId().equals(studentId)) {
            throw new BusinessException(404, "课时费记录不存在");
        }

        fee.setFeeType(request.getFeeType());
        fee.setUnitPrice(request.getUnitPrice());
        fee.setPrepaidCount(request.getPrepaidCount());
        fee.setPeriodStart(request.getPeriodStart());
        fee.setPeriodEnd(request.getPeriodEnd());
        fee.setSubject(request.getSubject());
        fee.setRemark(request.getRemark());
        fee.setPriceTiers(toJson(request.getPriceTiers()));

        studentFeeMapper.updateById(fee);
        log.info("更新课时费：{}，操作人：{}", feeId, operatorId);

        return toStudentFeeVO(fee);
    }

    // ==================== 费用记录管理 ====================

    public PageResult<FeeRecordVO> listFeeRecords(Long studentId, Integer page, Integer size) {
        LambdaQueryWrapper<FeeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeeRecord::getStudentId, studentId)
                .orderByDesc(FeeRecord::getPaymentDate);

        Page<FeeRecord> pageResult = feeRecordMapper.selectPage(
                new Page<>(page, size),
                wrapper
        );

        List<FeeRecordVO> records = pageResult.getRecords().stream()
                .map(this::toFeeRecordVO)
                .toList();

        return new PageResult<>(records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    @Transactional
    public FeeRecordVO createFeeRecord(Long studentId, FeeRecordRequest request, Long operatorId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        FeeRecord record = new FeeRecord();
        record.setStudentId(studentId);
        record.setFeeId(request.getFeeId());
        record.setAmount(request.getAmount());
        record.setPaymentType(request.getPaymentType());
        record.setPaymentDate(request.getPaymentDate());
        record.setPaymentMethod(request.getPaymentMethod());
        record.setNote(request.getNote());
        record.setCreatedBy(operatorId);

        feeRecordMapper.insert(record);

        // 如果关联了课时费，更新剩余课时
        if (request.getFeeId() != null && "INCOME".equals(request.getPaymentType())) {
            StudentFee fee = studentFeeMapper.selectById(request.getFeeId());
            if (fee != null && fee.getRemainingCount() != null && fee.getRemainingCount() > 0) {
                fee.setRemainingCount(fee.getRemainingCount() - 1);
                studentFeeMapper.updateById(fee);
            }
        }

        log.info("新增费用记录：学生{}，金额{}，操作人：{}", student.getName(), request.getAmount(), operatorId);

        return toFeeRecordVO(record);
    }

    // ==================== 转换方法 ====================

    private StudentVO toStudentVO(Student student) {
        Integer age = null;
        if (student.getBirthDate() != null) {
            age = Period.between(student.getBirthDate(), LocalDate.now()).getYears();
        }

        return StudentVO.builder()
                .id(student.getId())
                .name(student.getName())
                .gender(student.getGender())
                .birthDate(student.getBirthDate())
                .age(age)
                .grade(student.getGrade())
                .school(student.getSchool())
                .subjects(fromJson(student.getSubjects()))
                .source(student.getSource())
                .address(student.getAddress())
                .phone(student.getPhone())
                .parentName(student.getParentName())
                .parentPhone(student.getParentPhone())
                .parentRelation(student.getParentRelation())
                .remark(student.getRemark())
                .enrollmentDate(student.getEnrollmentDate())
                .status(student.getStatus())
                .avatarUrl(student.getAvatarUrl())
                .tags(fromJson(student.getTags()))
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }

    private StudentFeeVO toStudentFeeVO(StudentFee fee) {
        return StudentFeeVO.builder()
                .id(fee.getId())
                .studentId(fee.getStudentId())
                .feeType(fee.getFeeType())
                .unitPrice(fee.getUnitPrice())
                .prepaidCount(fee.getPrepaidCount())
                .remainingCount(fee.getRemainingCount())
                .periodStart(fee.getPeriodStart())
                .periodEnd(fee.getPeriodEnd())
                .subject(fee.getSubject())
                .status(fee.getStatus())
                .remark(fee.getRemark())
                .createdAt(fee.getCreatedAt())
                .priceTiers(fromJsonPriceTiers(fee.getPriceTiers()))
                .build();
    }

    private FeeRecordVO toFeeRecordVO(FeeRecord record) {
        return FeeRecordVO.builder()
                .id(record.getId())
                .studentId(record.getStudentId())
                .feeId(record.getFeeId())
                .amount(record.getAmount())
                .paymentType(record.getPaymentType())
                .paymentDate(record.getPaymentDate())
                .paymentMethod(record.getPaymentMethod())
                .note(record.getNote())
                .createdBy(record.getCreatedBy())
                .createdAt(record.getCreatedAt())
                .build();
    }

    private String toJsonStrings(List<String> list) {
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

    private String toJson(List<PriceTier> tiers) {
        if (tiers == null || tiers.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(tiers);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<PriceTier> fromJsonPriceTiers(String json) {
        if (!StringUtils.hasText(json)) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
