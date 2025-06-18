package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.LoginRequestDTO;
import com.hastane.hastanebackend.dto.LoginResponseDTO;
import com.hastane.hastanebackend.entity.Hasta;
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.repository.HastaRepository;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.security.jwt.JwtTokenProvider;
import org.slf4j.Logger; // Logger
import org.slf4j.LoggerFactory; // Logger
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600) // Geliştirme için, production'da daha spesifik olmalı
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final KullaniciRepository kullaniciRepository; // Kullanici detaylarını çekmek için
    private final HastaRepository hastaRepository;       // Hasta ID'sini çekmek için

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          KullaniciRepository kullaniciRepository,
                          HastaRepository hastaRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.kullaniciRepository = kullaniciRepository;
        this.hastaRepository = hastaRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDTO loginRequest) {
        logger.info("Login isteği alındı: {}", loginRequest.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getSifre()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername(); // Bu email olmalı

        // Kullanıcı bilgilerini ve rollerini çek
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Kullanici entity'sini veritabanından çek (Kullanici ID'sini almak için)
        Kullanici kullanici = kullaniciRepository.findByEmail(username)
                .orElse(null); // Kullanıcı bulunamazsa null (bu durum normalde olmamalı)

        Integer kullaniciId = null;
        Integer hastaId = null;

        if (kullanici != null) {
            kullaniciId = kullanici.getId();
            logger.info("Kullanıcı bulundu: ID={}, Email={}", kullaniciId, username);

            // Eğer kullanıcı HASTA rolüne sahipse, Hasta ID'sini de çek
            if (roles.contains("ROLE_HASTA")) {
                Optional<Hasta> hastaOpt = hastaRepository.findByKullanici_Id(kullaniciId);
                if (hastaOpt.isPresent()) {
                    hastaId = hastaOpt.get().getId();
                    logger.info("Kullanıcı (ID: {}) için Hasta ID: {} bulundu.", kullaniciId, hastaId);
                } else {
                    logger.warn("Kullanıcı (ID: {}) HASTA rolüne sahip ancak hasta profili bulunamadı.", kullaniciId);
                }
            }
        } else {
            logger.warn("Login işlemi başarılı olmasına rağmen kullanıcı ('{}') veritabanında bulunamadı.", username);
        }

        // LoginResponseDTO'yu oluştur ve set et (Lombok @Data getter/setter sağlar)
        LoginResponseDTO responseDTO = new LoginResponseDTO(jwt, "Bearer");
        responseDTO.setRoller(roles);
        responseDTO.setHastaId(hastaId); // null olabilir
        responseDTO.setEmail(username);  // Giriş yapılan email
        responseDTO.setKullaniciId(kullaniciId); // Kullanici tablosundaki ID, null olabilir

        logger.info("Login başarılı. Token ve kullanıcı bilgileri dönülüyor: {}", username);
        return ResponseEntity.ok(responseDTO);
    }
}