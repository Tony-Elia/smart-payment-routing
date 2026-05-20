package com.example.fawrypaymentrouting.biller.service;

import com.example.fawrypaymentrouting.biller.dto.BillerRequestDTO;
import com.example.fawrypaymentrouting.biller.dto.BillerResponseDTO;
import com.example.fawrypaymentrouting.biller.model.Biller;
import com.example.fawrypaymentrouting.biller.repository.BillerRepository;
import com.example.fawrypaymentrouting.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillerService {

    private final BillerRepository billerRepository;
    private final BillerMapper billerMapper;

    @Transactional(readOnly = true)
    public Page<BillerResponseDTO> findAll(Pageable pageable) {
        return billerRepository.findAll(pageable)
                .map(billerMapper::toDto);
    }

    @Transactional(readOnly = true)
    public BillerResponseDTO findById(UUID id) {
        return billerRepository.findById(id)
                .map(billerMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Biller not found"));
    }

    @Transactional
    public BillerResponseDTO create(BillerRequestDTO request) {
        Biller biller = billerMapper.toEntity(request);
        return billerMapper.toDto(billerRepository.save(biller));
    }

    @Transactional
    public BillerResponseDTO update(UUID id, BillerRequestDTO request) {
        Biller biller = billerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Biller not found"));

        billerMapper.updateEntityFromDto(request, biller);
        return billerMapper.toDto(billerRepository.save(biller));
    }

    @Transactional
    public void delete(UUID id) {
        if (!billerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Biller not found");
        }
        billerRepository.deleteById(id);
    }
}
