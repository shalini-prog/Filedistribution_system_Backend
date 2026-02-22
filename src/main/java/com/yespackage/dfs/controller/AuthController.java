package com.yespackage.dfs.controller;

import com.yespackage.dfs.dto.AuthRequest;
import com.yespackage.dfs.dto.AuthResponse;
import com.yespackage.dfs.entity.User;
import com.yespackage.dfs.repository.UserRepository;
import com.yespackage.dfs.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {

        if (userRepository.existsByUsername(user.getUsername()))
            return "Username already exists";

        if (userRepository.existsByEmail(user.getEmail()))
            return "Email already exists";

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return "User registered successfully!";
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String token = jwtUtil.generateToken(request.getUsername());

        return new AuthResponse(token);
    }
    
    @PostMapping("/logout")
    public String logout() {
        // Since JWT is stateless, logout is handled client-side by deleting the token
        return "Logout successful. Please delete the JWT token on client side.";
    }
}