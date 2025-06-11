package com.dentistdss.audit.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.dentistdss.audit.model.AuditEntry;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Repository
public interface AuditEntryRepository extends MongoRepository<AuditEntry, String> {
} 