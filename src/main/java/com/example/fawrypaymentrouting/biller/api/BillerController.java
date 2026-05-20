package com.example.fawrypaymentrouting.biller.api;

import com.example.fawrypaymentrouting.biller.model.Biller;
import com.example.fawrypaymentrouting.biller.repository.BillerRepository;
import com.example.fawrypaymentrouting.shared.exception.ResourceConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billers")
@RequiredArgsConstructor
public class BillerController {

    private final BillerRepository billerRepository;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Biller> getAllBillers() {
        return billerRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Biller createBiller(@RequestBody Biller biller) {
        if (billerRepository.existsById(biller.getId())) {
            throw new ResourceConflictException("Biller with ID already exists");
        }
        return billerRepository.save(biller);
    }
}
