package com.kaki.doctrack.authservice.service;

import com.kaki.doctrack.authservice.security.jwt.JwtUtil;
import com.kaki.doctrack.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;



}
