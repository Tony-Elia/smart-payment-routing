package com.example.fawrypaymentrouting.gateway.api;
import com.example.fawrypaymentrouting.gateway.dto.GatewayRequestDto;
import com.example.fawrypaymentrouting.gateway.dto.GatewayResponseDto;
import com.example.fawrypaymentrouting.gateway.service.GatewayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gateways")
@RequiredArgsConstructor
public class GatewayController {

    private final GatewayService gatewayService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<GatewayResponseDto> getAllGateways() {
        return gatewayService.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GatewayResponseDto getGatewayById(@PathVariable UUID id) {
        return gatewayService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GatewayResponseDto createGateway(@Valid @RequestBody GatewayRequestDto dto) {
        return gatewayService.create(dto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GatewayResponseDto updateGateway(@PathVariable UUID id, @Valid @RequestBody GatewayRequestDto dto) {
        return gatewayService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGateway(@PathVariable UUID id) {
        gatewayService.delete(id);
    }
}
