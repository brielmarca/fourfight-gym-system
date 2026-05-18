package com.gym.service;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.CreateStudentMartialArtRequest;
import com.gym.dto.response.StudentMartialArtResponse;
import com.gym.entity.Graduation;
import com.gym.entity.MartialArt;
import com.gym.entity.Student;
import com.gym.entity.StudentMartialArt;
import com.gym.exception.BusinessRuleException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.GraduationRepository;
import com.gym.repository.MartialArtRepository;
import com.gym.repository.StudentMartialArtRepository;
import com.gym.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentMartialArtService {

    private final StudentMartialArtRepository studentMartialArtRepository;
    private final StudentRepository studentRepository;
    private final MartialArtRepository martialArtRepository;
    private final GraduationRepository graduationRepository;

    public List<StudentMartialArtResponse> getByStudentId(java.util.UUID studentId) {
        List<StudentMartialArt> records = studentMartialArtRepository.findByStudentId(studentId);
        return records.stream().map(StudentMartialArtResponse::from).toList();
    }

    @Transactional
    public StudentMartialArtResponse create(CreateStudentMartialArtRequest request) {
        Student student = studentRepository.findById(request.studentId())
            .orElseThrow(() -> new ResourceNotFoundException("Student", request.studentId()));
        MartialArt martialArt = martialArtRepository.findById(request.martialArtId())
            .orElseThrow(() -> new ResourceNotFoundException("MartialArt", request.martialArtId()));
        Graduation graduation = graduationRepository.findById(request.graduationId())
            .orElseThrow(() -> new ResourceNotFoundException("Graduation", request.graduationId()));
        
        // Rule 1: Prevent duplicate martial art per student
        if (studentMartialArtRepository.existsByStudentIdAndMartialArtId(student.getId(), martialArt.getId())) {
            throw new BusinessRuleException("Student '" + student.getName() + "' already has martial art '" + martialArt.getName() + "' assigned");
        }
        
        // Rule 3: Prevent invalid graduation assignment - graduation must belong to selected martial art
        if (!graduation.getMartialArt().getId().equals(martialArt.getId())) {
            throw new BusinessRuleException("Graduation '" + graduation.getName() + "' does not belong to martial art '" + martialArt.getName() + "'");
        }
        
        StudentMartialArt record = StudentMartialArt.builder()
            .student(student)
            .martialArt(martialArt)
            .graduation(graduation)
            .startDate(request.startDate())
            .build();
        record = studentMartialArtRepository.save(record);
        log.info("Student martial art created: student={}, art={}, graduation={}", student.getName(), martialArt.getName(), graduation.getName());
        return StudentMartialArtResponse.from(record);
    }

    @Transactional
    public StudentMartialArtResponse promote(java.util.UUID id) {
        StudentMartialArt record = studentMartialArtRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("StudentMartialArt", id));
        
        Graduation currentGraduation = record.getGraduation();
        MartialArt martialArt = record.getMartialArt();
        
        // Find next graduation by levelOrder
        List<Graduation> nextGraduations = graduationRepository.findByMartialArtId(
                martialArt.getId(), PageRequest.of(0, 1))
            .getContent().stream()
            .filter(g -> g.getLevelOrder() > currentGraduation.getLevelOrder())
            .sorted((a, b) -> Integer.compare(a.getLevelOrder(), b.getLevelOrder()))
            .toList();
        
        if (nextGraduations.isEmpty()) {
            throw new BusinessRuleException("No higher graduation level found for martial art '" + martialArt.getName() + "'");
        }
        
        Graduation nextGraduation = nextGraduations.get(0);
        
        // Rule 2: Enforce graduation progression - cannot skip levels
        if (nextGraduation.getLevelOrder() != currentGraduation.getLevelOrder() + 1) {
            throw new BusinessRuleException("Cannot skip graduation levels. Current: " + 
                currentGraduation.getLevelOrder() + ", Next available: " + nextGraduation.getLevelOrder());
        }
        
        record.setGraduation(nextGraduation);
        record = studentMartialArtRepository.save(record);
        log.info("Student promoted: student={}, art={}, from={} to={}", 
            record.getStudent().getName(), martialArt.getName(), 
            currentGraduation.getName(), nextGraduation.getName());
        return StudentMartialArtResponse.from(record);
    }

    @Transactional
    public void delete(java.util.UUID id) {
        StudentMartialArt record = studentMartialArtRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("StudentMartialArt", id));
        studentMartialArtRepository.delete(record);
        log.info("Student martial art record deleted: {}", id);
    }
}
