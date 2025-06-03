package press.mizhifei.dentist.system.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.system.dto.ApiResponse;
import press.mizhifei.dentist.system.dto.SystemSettingRequest;
import press.mizhifei.dentist.system.dto.SystemSettingResponse;
import press.mizhifei.dentist.system.service.SystemSettingService;

import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@RestController
@RequestMapping("/system/setting")
@RequiredArgsConstructor
public class SystemSettingController {

    private final SystemSettingService service;

    @PostMapping
    public ResponseEntity<ApiResponse<SystemSettingResponse>> createOrUpdate(@RequestBody SystemSettingRequest request) {
        SystemSettingResponse response = service.createOrUpdate(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(service.listAll()));
    }
} 