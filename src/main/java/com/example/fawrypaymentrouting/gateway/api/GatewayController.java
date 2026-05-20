package com.example.fawrypaymentrouting.gateway.api;
import com.example.fawrypaymentrouting.gateway.dto.GatewayRequestDto;
import com.example.fawrypaymentrouting.gateway.dto.GatewayResponseDto;
import com.example.fawrypaymentrouting.gateway.service.GatewayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@Slf4j
@RestController
@RequestMapping("/api/gateways")
@RequiredArgsConstructor
public class GatewayController {
    private final GatewayService gatewayService;

    @GetMapping
    public List<GatewayResponseDto> getAllGateways() {
        log.info("Fetching all gateways");
        return gatewayService.findAll();
    }

    @GetMapping("/{id}")
    public GatewayResponseDto getGateway(@PathVariable UUID id) {
        log.info("Fetching gateway with id: {}", id);
        return gatewayService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GatewayResponseDto createGateway(@Valid @RequestBody GatewayRequestDto dto) {
        log.info("Creating new gateway: {}", dto.name());
        return gatewayService.create(dto);
    }

    @PutMapping("/{id}")
    public GatewayResponseDto updateGateway(@PathVariable UUID id, @Valid @RequestBody GatewayRequestDto dto) {
        log.info("Updating gateway with id: {}", id);
        return gatewayService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> deleteGateway(@PathVariable UUID id) {
        log.info("Deleting gateway with id: {}", id);
        gatewayService.delete(id);
        return Map.of("message", "Gateway deleted successfully");
    }
}
