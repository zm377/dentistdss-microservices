package com.dentistdss.audit.controller;

import lombok.RequiredArgsConstructor;
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
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PostMapping
    public ResponseEntity<ApiResponse<AuditEntryResponse>> record(@RequestBody AuditEntryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(auditService.record(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditEntryResponse>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(auditService.listAll()));
    }
} 