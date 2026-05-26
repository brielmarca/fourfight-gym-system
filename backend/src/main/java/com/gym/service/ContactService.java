package com.gym.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.dto.request.CreateContactRequest;
import com.gym.dto.response.ContactResponse;
import com.gym.entity.Contact;
import com.gym.entity.Contact.ContactStatus;
import com.gym.entity.User;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.ContactRepository;
import com.gym.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    public Page<ContactResponse> getAll(Pageable pageable) {
        return contactRepository.findAll(pageable).map(ContactResponse::from);
    }

    public Page<ContactResponse> getByStatus(Contact.ContactStatus status, Pageable pageable) {
        return contactRepository.findByStatus(status, pageable).map(ContactResponse::from);
    }

    public ContactResponse getById(UUID id) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact", id));
        return ContactResponse.from(contact);
    }

    @Transactional
    public ContactResponse create(CreateContactRequest request) {
        Contact contact = Contact.builder()
            .name(InputSanitizer.trimToNull(request.name()))
            .email(InputSanitizer.normalizeEmail(request.email()))
            .phone(InputSanitizer.trimToNull(request.phone()))
            .subject(InputSanitizer.trimToNull(request.subject()))
            .message(InputSanitizer.trimToNull(request.message()))
            .status(Contact.ContactStatus.PENDING)
            .build();

        contact = contactRepository.save(contact);
        log.info("Contact created: {}", contact.getEmail());
        return ContactResponse.from(contact);
    }

    @Transactional
    public ContactResponse resolve(UUID id, User resolvedBy) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact", id));
        contact.resolve(resolvedBy);
        contact = contactRepository.save(contact);
        log.info("Contact resolved: {}", id);
        return ContactResponse.from(contact);
    }

    @Transactional
    public void delete(UUID id) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact", id));
        contact.softDelete();
        contactRepository.save(contact);
        log.info("Contact deleted: {}", id);
    }
}
