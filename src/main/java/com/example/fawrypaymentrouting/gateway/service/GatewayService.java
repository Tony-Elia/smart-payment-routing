package com.example.fawrypaymentrouting.gateway.service;
import com.example.fawrypaymentrouting.gateway.dto.GatewayRequestDto;
import com.example.fawrypaymentrouting.gateway.dto.GatewayResponseDto;
import com.example.fawrypaymentrouting.gateway.model.Gateway;
import com.example.fawrypaymentrouting.gateway.repository.GatewayRepository;
import com.example.fawrypaymentrouting.shared.exception.ResourceConflictException;
import com.example.fawrypaymentrouting.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class GatewayService {

    private final GatewayRepository gatewayRepository;
    private final GatewayMapper gatewayMapper;

    @Transactional(readOnly = true)
    public List<GatewayResponseDto> findAll() {
        return gatewayRepository.findAll().stream()
                .map(gatewayMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GatewayResponseDto findById(UUID id) {
        return gatewayRepository.findById(id)
                .map(gatewayMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Gateway not found"));
    }

    @Transactional
    public GatewayResponseDto create(GatewayRequestDto dto) {
        // check on duplicate names first
        if(gatewayRepository.existsByNameIgnoreCase(dto.name()))
            throw new ResourceConflictException("Gateway with the same name already exists");

        Gateway gateway = gatewayMapper.toEntity(dto);
        return gatewayMapper.toDto(gatewayRepository.save(gateway));
    }

    @Transactional
    public GatewayResponseDto update(UUID id, GatewayRequestDto dto) {
        Gateway gateway = gatewayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gateway not found"));

        // check on duplicate names first if the name was changed
        if(!dto.name().equalsIgnoreCase(gateway.getName()) && gatewayRepository.existsByNameIgnoreCase(dto.name()))
            throw new ResourceConflictException("Gateway with the same name already exists");

        gatewayMapper.updateEntityFromDto(dto, gateway);

        return gatewayMapper.toDto(gatewayRepository.save(gateway));
    }

    @Transactional
    public void delete(UUID id) {
        // check if the gateway exists first to throw 404 if not found
        if(!gatewayRepository.existsById(id))
            throw new ResourceNotFoundException("Gateway not found");

        gatewayRepository.deleteById(id);
    }
}
