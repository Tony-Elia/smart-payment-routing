package com.example.fawrypaymentrouting.biller.api;

import com.example.fawrypaymentrouting.biller.dto.BillerRequestDTO;
import com.example.fawrypaymentrouting.biller.dto.BillerResponseDTO;
import com.example.fawrypaymentrouting.biller.service.BillerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/billers")
@RequiredArgsConstructor
public class BillerController {

    private final BillerService billerService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<BillerResponseDTO> getAllBillers(Pageable pageable) {
        return billerService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BillerResponseDTO getBillerById(@PathVariable UUID id) {
        return billerService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BillerResponseDTO createBiller(@Valid @RequestBody BillerRequestDTO request) {
        return billerService.create(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BillerResponseDTO updateBiller(@PathVariable UUID id, @Valid @RequestBody BillerRequestDTO request) {
        return billerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> deleteBiller(@PathVariable UUID id) {
        billerService.delete(id);

        return Map.of("message", "Biller deleted successfully");
    }
}
