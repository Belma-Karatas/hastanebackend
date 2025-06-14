package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.PersonelVardiyaGoruntuleDTO;
import com.hastane.hastanebackend.dto.PersonelVardiyaOlusturDTO;
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.service.PersonelVardiyaService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/personelvardiyalari")
// @CrossOrigin(...)
public class PersonelVardiyaController {

    private static final Logger log = LoggerFactory.getLogger(PersonelVardiyaController.class);
    private final PersonelVardiyaService personelVardiyaService;
    private final KullaniciRepository kullaniciRepository;

    public PersonelVardiyaController(PersonelVardiyaService personelVardiyaService, KullaniciRepository kullaniciRepository) {
        this.personelVardiyaService = personelVardiyaService;
        this.kullaniciRepository = kullaniciRepository;
    }

    private Integer getAktifKullaniciId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new IllegalStateException("Bu işlem için kimlik doğrulaması gerekmektedir.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Kullanici kullanici = kullaniciRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Aktif kullanıcı bilgileri alınamadı. Email: " + userDetails.getUsername()));
        return kullanici.getId();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> vardiyaAta(@Valid @RequestBody PersonelVardiyaOlusturDTO dto) {
        try {
            Integer yapanKullaniciId = getAktifKullaniciId();
            log.info("POST /api/personelvardiyalari çağrıldı. Personel ID: {}, Yapan Kullanıcı ID: {}", dto.getPersonelId(), yapanKullaniciId);
            PersonelVardiyaGoruntuleDTO atama = personelVardiyaService.vardiyaAta(dto, yapanKullaniciId);
            return new ResponseEntity<>(atama, HttpStatus.CREATED);
        } catch (ResourceNotFoundException | IllegalArgumentException | IllegalStateException e) {
            log.warn("Personel vardiya atama hatası: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) {
            log.warn("Personel vardiya atama yetki hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Personel vardiya atanırken beklenmedik bir hata oluştu:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Vardiya atanırken sunucu hatası.");
        }
    }

    @DeleteMapping("/{personelVardiyaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> vardiyaAtamasiniKaldir(@PathVariable Integer personelVardiyaId) {
        try {
            Integer yapanKullaniciId = getAktifKullaniciId();
            log.info("DELETE /api/personelvardiyalari/{} çağrıldı. Yapan Kullanıcı ID: {}", personelVardiyaId, yapanKullaniciId);
            personelVardiyaService.vardiyaAtamasiniKaldir(personelVardiyaId, yapanKullaniciId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.warn("Personel vardiya ataması kaldırma hatası - bulunamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AccessDeniedException e) {
            log.warn("Personel vardiya ataması kaldırma yetki hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Personel vardiya ataması (ID: {}) kaldırılırken beklenmedik bir hata oluştu:", personelVardiyaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Vardiya ataması kaldırılırken sunucu hatası.");
        }
    }

    @GetMapping("/{personelVardiyaId}")
    @PreAuthorize("isAuthenticated()") // Yetki serviste kontrol ediliyor
    public ResponseEntity<?> getPersonelVardiyaById(@PathVariable Integer personelVardiyaId) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            log.debug("GET /api/personelvardiyalari/{} çağrıldı. Talep Eden Kullanıcı ID: {}", personelVardiyaId, talepEdenKullaniciId);
            Optional<PersonelVardiyaGoruntuleDTO> dto = personelVardiyaService.getPersonelVardiyaById(personelVardiyaId, talepEdenKullaniciId);
            return dto.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Personel vardiya ataması bulunamadı."));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Personel vardiya ataması (ID: {}) getirilirken hata: ",personelVardiyaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Sunucu hatası.");
        }
    }

    @GetMapping("/personel/{personelId}")
    @PreAuthorize("isAuthenticated()") // Yetki serviste
    public ResponseEntity<?> getVardiyalarByPersonelId(@PathVariable Integer personelId) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            log.debug("GET /api/personelvardiyalari/personel/{} çağrıldı. Talep Eden Kullanıcı ID: {}", personelId, talepEdenKullaniciId);
            List<PersonelVardiyaGoruntuleDTO> vardiyalar = personelVardiyaService.getVardiyalarByPersonelId(personelId, talepEdenKullaniciId);
            return ResponseEntity.ok(vardiyalar);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
             log.error("Personel (ID: {}) vardiyaları getirilirken hata: ",personelId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Sunucu hatası.");
        }
    }

    @GetMapping("/tarih/{tarih}")
    @PreAuthorize("hasAnyRole('ADMIN', 'YONETICI')") // Sadece yetkili roller belirli bir tarihteki tüm vardiyaları görebilir
    public ResponseEntity<List<PersonelVardiyaGoruntuleDTO>> getVardiyalarByTarih(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tarih) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            log.info("GET /api/personelvardiyalari/tarih/{} çağrıldı. Talep Eden Kullanıcı ID: {}", tarih, talepEdenKullaniciId);
            List<PersonelVardiyaGoruntuleDTO> vardiyalar = personelVardiyaService.getVardiyalarByTarih(tarih, talepEdenKullaniciId);
            return ResponseEntity.ok(vardiyalar);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Veya .body(e.getMessage())
        } catch (Exception e) {
             log.error("{} tarihli vardiyalar getirilirken hata: ",tarih, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/personel/mevcut/yaklasan")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PersonelVardiyaGoruntuleDTO>> getMyYaklasanVardiyalar() {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            log.info("GET /api/personelvardiyalari/personel/mevcut/yaklasan çağrıldı. Kullanıcı ID: {}", talepEdenKullaniciId);
            List<PersonelVardiyaGoruntuleDTO> vardiyalar = personelVardiyaService.getMyYaklasanVardiyalar(talepEdenKullaniciId);
            return ResponseEntity.ok(vardiyalar);
        } catch (Exception e) {
            log.error("Mevcut personelin yaklaşan vardiyaları getirilirken hata: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}