package com.example.fawrypaymentrouting.shared.security.controller;

import com.example.fawrypaymentrouting.shared.security.JwtService;
import com.example.fawrypaymentrouting.shared.security.dto.AuthenticationRequestDto;
import com.example.fawrypaymentrouting.shared.security.dto.AuthenticationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> authenticate(@RequestBody AuthenticationRequestDto request) {
        // 1. Spring checks the RAM to see if the credentials match
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // 2. If we reach here, the password was correct. Load the user.
        UserDetails user = userDetailsService.loadUserByUsername(request.username());

        // 3. Generate the JWT
        String jwtToken = jwtService.generateToken(user);

        return ResponseEntity.ok(new AuthenticationResponseDto(jwtToken));
    }
}