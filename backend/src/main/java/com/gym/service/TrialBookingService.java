package com.gym.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.BookTrialRequest;
import com.gym.dto.response.TrialBookingResponse;
import com.gym.entity.TrialBooking.BookingStatus;
import com.gym.entity.TrialBooking;
import com.gym.entity.User;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.TrialBookingRepository;
import com.gym.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrialBookingService {

    private final TrialBookingRepository trialBookingRepository;

    public Page<TrialBookingResponse> getAll(Pageable pageable) {
        return trialBookingRepository.findAll(pageable).map(TrialBookingResponse::from);
    }

    public Page<TrialBookingResponse> getByStatus(TrialBooking.BookingStatus status, Pageable pageable) {
        return trialBookingRepository.findByStatus(status, pageable).map(TrialBookingResponse::from);
    }

    public TrialBookingResponse getById(UUID id) {
        TrialBooking booking = trialBookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("TrialBooking", id));
        return TrialBookingResponse.from(booking);
    }

    @Transactional
    public TrialBookingResponse create(BookTrialRequest request, String program) {
        TrialBooking booking = TrialBooking.builder()
            .name(InputSanitizer.trimToNull(request.name()))
            .email(InputSanitizer.normalizeEmail(request.email()))
            .phone(InputSanitizer.trimToNull(request.phone()))
            .program(program)
            .status(TrialBooking.BookingStatus.PENDING)
            .build();

        booking = trialBookingRepository.save(booking);
        log.info("Trial booking created: {} for program {}", booking.getEmail(), program);
        return TrialBookingResponse.from(booking);
    }

    @Transactional
    public TrialBookingResponse process(UUID id, User processedBy) {
        TrialBooking booking = trialBookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("TrialBooking", id));
        booking.markAsProcessed(processedBy);
        booking = trialBookingRepository.save(booking);
        log.info("Trial booking processed: {}", id);
        return TrialBookingResponse.from(booking);
    }

    @Transactional
    public void delete(UUID id) {
        TrialBooking booking = trialBookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("TrialBooking", id));
        booking.softDelete();
        trialBookingRepository.save(booking);
        log.info("Trial booking deleted: {}", id);
    }
}
