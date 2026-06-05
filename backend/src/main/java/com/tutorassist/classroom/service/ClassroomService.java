package com.tutorassist.classroom.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorassist.classroom.dto.ClassMemberVO;
import com.tutorassist.classroom.dto.ClassroomRequest;
import com.tutorassist.classroom.dto.ClassroomVO;
import com.tutorassist.classroom.entity.ClassMember;
import com.tutorassist.classroom.entity.VirtualClass;
import com.tutorassist.classroom.mapper.ClassMemberMapper;
import com.tutorassist.classroom.mapper.VirtualClassMapper;
import com.tutorassist.common.exception.BusinessException;
import com.tutorassist.student.entity.Student;
import com.tutorassist.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final VirtualClassMapper classMapper;
    private final ClassMemberMapper memberMapper;
    private final StudentMapper studentMapper;

    public List<ClassroomVO> listClassrooms() {
        LambdaQueryWrapper<VirtualClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(VirtualClass::getCreatedAt);

        return classMapper.selectList(wrapper).stream()
                .map(this::toClassroomVO)
                .toList();
    }

    public ClassroomVO getClassroom(Long id) {
        VirtualClass vc = classMapper.selectById(id);
        if (vc == null) {
            throw new BusinessException(404, "班级不存在");
        }
        return toClassroomVO(vc);
    }

    @Transactional
    public ClassroomVO createClassroom(ClassroomRequest request, Long operatorId) {
        VirtualClass vc = new VirtualClass();
        vc.setName(request.getName());
        vc.setSubject(request.getSubject());
        vc.setDescription(request.getDescription());
        vc.setStatus("ACTIVE");

        classMapper.insert(vc);
        log.info("创建班级：{}，操作人：{}", vc.getName(), operatorId);

        return toClassroomVO(vc);
    }

    @Transactional
    public ClassroomVO updateClassroom(Long id, ClassroomRequest request, Long operatorId) {
        VirtualClass vc = classMapper.selectById(id);
        if (vc == null) {
            throw new BusinessException(404, "班级不存在");
        }

        vc.setName(request.getName());
        vc.setSubject(request.getSubject());
        vc.setDescription(request.getDescription());

        classMapper.updateById(vc);
        log.info("更新班级：{}，操作人：{}", vc.getName(), operatorId);

        return toClassroomVO(vc);
    }

    @Transactional
    public void deleteClassroom(Long id, Long operatorId) {
        VirtualClass vc = classMapper.selectById(id);
        if (vc == null) {
            throw new BusinessException(404, "班级不存在");
        }

        // 删除班级成员
        LambdaQueryWrapper<ClassMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClassMember::getClassId, id);
        memberMapper.delete(wrapper);

        classMapper.deleteById(id);
        log.info("删除班级：{}，操作人：{}", vc.getName(), operatorId);
    }

    @Transactional
    public void updateClassroomStatus(Long id, String status, Long operatorId) {
        VirtualClass vc = classMapper.selectById(id);
        if (vc == null) {
            throw new BusinessException(404, "班级不存在");
        }
        vc.setStatus(status);
        classMapper.updateById(vc);
        log.info("更新班级状态：{} -> {}，操作人：{}", vc.getName(), status, operatorId);
    }

    // ==================== 成员管理 ====================

    public List<ClassMemberVO> listMembers(Long classId) {
        LambdaQueryWrapper<ClassMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClassMember::getClassId, classId);

        return memberMapper.selectList(wrapper).stream()
                .map(this::toMemberVO)
                .toList();
    }

    @Transactional
    public void addMember(Long classId, Long studentId, Long operatorId) {
        VirtualClass vc = classMapper.selectById(classId);
        if (vc == null) {
            throw new BusinessException(404, "班级不存在");
        }

        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        // 检查是否已是成员
        LambdaQueryWrapper<ClassMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClassMember::getClassId, classId)
                .eq(ClassMember::getStudentId, studentId);
        Long count = memberMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException("该学生已在班级中");
        }

        ClassMember member = new ClassMember();
        member.setClassId(classId);
        member.setStudentId(studentId);
        member.setJoinedAt(LocalDateTime.now());

        memberMapper.insert(member);
        log.info("添加班级成员：班级{}，学生{}，操作人：{}", classId, studentId, operatorId);
    }

    @Transactional
    public void removeMember(Long classId, Long studentId, Long operatorId) {
        LambdaQueryWrapper<ClassMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClassMember::getClassId, classId)
                .eq(ClassMember::getStudentId, studentId);

        int deleted = memberMapper.delete(wrapper);
        if (deleted == 0) {
            throw new BusinessException(404, "成员不存在");
        }
        log.info("移除班级成员：班级{}，学生{}，操作人：{}", classId, studentId, operatorId);
    }

    // ==================== 转换方法 ====================

    private ClassroomVO toClassroomVO(VirtualClass vc) {
        LambdaQueryWrapper<ClassMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClassMember::getClassId, vc.getId());
        List<ClassMember> members = memberMapper.selectList(wrapper);

        return ClassroomVO.builder()
                .id(vc.getId())
                .name(vc.getName())
                .subject(vc.getSubject())
                .description(vc.getDescription())
                .status(vc.getStatus())
                .memberCount(members.size())
                .members(members.stream().map(this::toMemberVO).toList())
                .createdAt(vc.getCreatedAt())
                .build();
    }

    private ClassMemberVO toMemberVO(ClassMember member) {
        Student student = studentMapper.selectById(member.getStudentId());
        String studentName = student != null ? student.getName() : "未知";
        String grade = student != null ? student.getGrade() : null;

        return ClassMemberVO.builder()
                .id(member.getId())
                .classId(member.getClassId())
                .studentId(member.getStudentId())
                .studentName(studentName)
                .grade(grade)
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
