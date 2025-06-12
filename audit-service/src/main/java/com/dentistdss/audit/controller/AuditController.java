package com.dentistdss.audit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dentistdss.audit.dto.ApiResponse;
import com.dentistdss.audit.dto.AuditEntryRequest;
import com.dentistdss.audit.dto.AuditEntryResponse;
import com.dentistdss.audit.service.AuditService;

import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Slf4j
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PostMapping
    public ResponseEntity<ApiResponse<AuditEntryResponse>> record(@Valid @RequestBody AuditEntryRequest request) {
        try {
            log.info("Recording audit entry for actor: {}, action: {}", request.getActor(), request.getAction());
            AuditEntryResponse response = auditService.record(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Failed to record audit entry: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to record audit entry"));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditEntryResponse>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(auditService.listAll()));
    }
} 