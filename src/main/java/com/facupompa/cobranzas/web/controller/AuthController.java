package com.facupompa.cobranzas.web.controller;

import com.facupompa.cobranzas.domain.dto.LoginRequest;
import com.facupompa.cobranzas.web.security.JsonWebTokenUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JsonWebTokenUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, JsonWebTokenUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    ResponseEntity<Void> login (@RequestBody @Valid LoginRequest request){
        UsernamePasswordAuthenticationToken credentials = UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password());
        Authentication authentication = authenticationManager.authenticate(credentials);

        String token = jwtUtils.create(authentication.getName());

        return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + token).build();
    }

}
