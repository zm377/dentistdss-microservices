package press.mizhifei.dentist.system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import press.mizhifei.dentist.system.dto.SystemSettingRequest;
import press.mizhifei.dentist.system.dto.SystemSettingResponse;
import press.mizhifei.dentist.system.model.SystemSetting;
import press.mizhifei.dentist.system.repository.SystemSettingRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Service
@RequiredArgsConstructor
public class SystemSettingService {

    private final SystemSettingRepository repository;

    @Transactional
    public SystemSettingResponse createOrUpdate(SystemSettingRequest request) {
        SystemSetting setting = repository.findByKey(request.getKey())
                .map(existing -> {
                    existing.setValue(request.getValue());
                    existing.setDescription(request.getDescription());
                    return existing;
                })
                .orElse(SystemSetting.builder()
                        .key(request.getKey())
                        .value(request.getValue())
                        .description(request.getDescription())
                        .build());
        SystemSetting saved = repository.save(setting);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SystemSettingResponse> listAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    private SystemSettingResponse toDto(SystemSetting setting) {
        return SystemSettingResponse.builder()
                .id(setting.getId())
                .key(setting.getKey())
                .value(setting.getValue())
                .description(setting.getDescription())
                .build();
    }
} 