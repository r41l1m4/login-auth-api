package com.ironia.loginauthapi.controllers;

import com.ironia.loginauthapi.domain.user.User;
import com.ironia.loginauthapi.dto.LoginRequestDTO;
import com.ironia.loginauthapi.dto.RegisterRequestDTO;
import com.ironia.loginauthapi.dto.ResponseDTO;
import com.ironia.loginauthapi.infra.security.TokenService;
import com.ironia.loginauthapi.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @RequestMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequestDTO userDetails) {
        User user = this.userRepository.findByEmail(userDetails.email())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if(passwordEncoder.matches(user.getPassword(), userDetails.password())) {
            String token = tokenService.generateToken(user);
            return ResponseEntity.ok(new ResponseDTO(user.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequestDTO userDetails) {
        Optional<User> maybeUser = this.userRepository.findByEmail(userDetails.email());

        if (maybeUser.isEmpty()) {
            User user = new User();
            user.setPassword(passwordEncoder.encode(userDetails.password()));
            user.setEmail(userDetails.email());
            user.setName(userDetails.name());

            this.userRepository.save(user);

            String token = tokenService.generateToken(user);
            return ResponseEntity.ok(new ResponseDTO(user.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }


}
