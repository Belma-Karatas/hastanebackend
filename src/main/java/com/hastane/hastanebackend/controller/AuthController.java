package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.LoginRequestDTO;
import com.hastane.hastanebackend.dto.LoginResponseDTO;
import com.hastane.hastanebackend.entity.Hasta;
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.entity.Personel; // Personel entity'sini import et
import com.hastane.hastanebackend.repository.HastaRepository;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.repository.PersonelRepository; // PersonelRepository'yi import et
import com.hastane.hastanebackend.security.jwt.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final KullaniciRepository kullaniciRepository;
    private final HastaRepository hastaRepository;
    private final PersonelRepository personelRepository; // YENİ: PersonelRepository bağımlılığı

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          KullaniciRepository kullaniciRepository,
                          HastaRepository hastaRepository,
                          PersonelRepository personelRepository) { // YENİ: Constructor'a eklendi
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.kullaniciRepository = kullaniciRepository;
        this.hastaRepository = hastaRepository;
        this.personelRepository = personelRepository; // YENİ: Atama yapıldı
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
        String email = userDetails.getUsername(); // Bu email olmalı

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Kullanici kullanici = kullaniciRepository.findByEmail(email)
                .orElseThrow(() -> {
                    // Bu durum normalde UserDetailsServiceImpl'de yakalanmalı,
                    // ama bir güvenlik önlemi olarak burada da loglayabiliriz.
                    logger.error("loadUserByUsername'den geçen kullanıcı ('{}') veritabanında bulunamadı!", email);
                    return new RuntimeException("Kullanıcı doğrulama sonrası bulunamadı.");
                });

        Integer responseKullaniciId = kullanici.getId();
        Integer responsePersonelId = null;
        Integer responseHastaId = null;

        logger.info("Kullanıcı bulundu: ID={}, Email={}", responseKullaniciId, email);

        // Personel ID'sini çek (Kullanici'nin bir Personel profili varsa)
        // Bir kullanıcı hem personel hem de hasta olamayacağı varsayımıyla ilerliyoruz.
        // Eğer olabiliyorsa, rollerine göre önceliklendirme veya farklı bir mantık gerekebilir.
        Optional<Personel> personelOpt = personelRepository.findByKullanici_Id(responseKullaniciId);
        if (personelOpt.isPresent()) {
            responsePersonelId = personelOpt.get().getId();
            logger.info("Kullanıcı (ID: {}) için Personel ID: {} bulundu.", responseKullaniciId, responsePersonelId);
        }

        // Eğer kullanıcı HASTA rolüne sahipse ve personel değilse (veya personel olsa bile hasta profilini de alabiliriz)
        // Genellikle bir kullanıcı ya personeldir ya da hasta (sistem özelinde).
        // Eğer bir personel aynı zamanda hasta olabiliyorsa, bu kontrol daha esnek olmalı.
        // Şimdilik, eğer personel ID'si bulunamadıysa ve HASTA rolü varsa hasta ID'sini arayalım.
        // Ya da, her iki ID'yi de (personel ve hasta) null değilse gönderebiliriz, frontend karar verir.
        if (roles.contains("ROLE_HASTA")) {
            Optional<Hasta> hastaOpt = hastaRepository.findByKullanici_Id(responseKullaniciId);
            if (hastaOpt.isPresent()) {
                responseHastaId = hastaOpt.get().getId();
                logger.info("Kullanıcı (ID: {}) için Hasta ID: {} bulundu.", responseKullaniciId, responseHastaId);
            } else {
                logger.warn("Kullanıcı (ID: {}) HASTA rolüne sahip ancak hasta profili bulunamadı.", responseKullaniciId);
            }
        }
        
        // LoginResponseDTO oluşturulurken tüm yeni alanları kullanalım.
        // Lombok @AllArgsConstructor kullandığımız için direkt constructor ile oluşturabiliriz.
        LoginResponseDTO responseDTO = new LoginResponseDTO(
                jwt,
                "Bearer",
                roles,
                email,
                responseKullaniciId,
                responsePersonelId, // personelId eklendi
                responseHastaId
        );

        logger.info("Login başarılı. Token, roller ve ID'ler dönülüyor: {}", email);
        return ResponseEntity.ok(responseDTO);
    }
}