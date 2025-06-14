package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.SevkGoruntuleDTO;
import com.hastane.hastanebackend.dto.SevkOlusturDTO;
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.service.SevkService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sevkler")
// @CrossOrigin(...)
public class SevkController {

    private static final Logger log = LoggerFactory.getLogger(SevkController.class);
    private final SevkService sevkService;
    private final KullaniciRepository kullaniciRepository;

    public SevkController(SevkService sevkService, KullaniciRepository kullaniciRepository) {
        this.sevkService = sevkService;
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
    @PreAuthorize("hasRole('DOKTOR')")
    public ResponseEntity<?> createSevk(@Valid @RequestBody SevkOlusturDTO sevkOlusturDTO) {
        try {
            Integer sevkEdenDoktorKullaniciId = getAktifKullaniciId();
            log.info("POST /api/sevkler çağrıldı. Hasta ID: {}, Yapan Doktor ID: {}", sevkOlusturDTO.getHastaId(), sevkEdenDoktorKullaniciId);
            SevkGoruntuleDTO yeniSevk = sevkService.createSevk(sevkOlusturDTO, sevkEdenDoktorKullaniciId);
            return new ResponseEntity<>(yeniSevk, HttpStatus.CREATED);
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            log.warn("Sevk oluşturma hatası: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) { // Servis katmanından gelebilir
            log.warn("Sevk oluşturma yetki hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Sevk oluşturulurken beklenmedik bir hata oluştu:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Sevk oluşturulurken sunucu hatası.");
        }
    }

    @GetMapping("/{sevkId}")
    @PreAuthorize("isAuthenticated()") // Yetki kontrolü serviste
    public ResponseEntity<?> getSevkById(@PathVariable Integer sevkId) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            log.debug("GET /api/sevkler/{} çağrıldı. Talep Eden Kullanıcı ID: {}", sevkId, talepEdenKullaniciId);
            Optional<SevkGoruntuleDTO> dto = sevkService.getSevkById(sevkId, talepEdenKullaniciId);
            return dto.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sevk kaydı bulunamadı."));
        } catch (AccessDeniedException e) {
            log.warn("Sevk görüntüleme yetki hatası (ID: {}): {}", sevkId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Sevk (ID: {}) getirilirken beklenmedik bir hata oluştu:", sevkId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Sevk bilgileri getirilirken sunucu hatası.");
        }
    }

    @GetMapping("/hasta/{hastaId}")
    @PreAuthorize("isAuthenticated()") // Yetki kontrolü serviste
    public ResponseEntity<?> getSevklerByHastaId(@PathVariable Integer hastaId) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            log.info("GET /api/sevkler/hasta/{} çağrıldı. Talep Eden Kullanıcı ID: {}", hastaId, talepEdenKullaniciId);
            List<SevkGoruntuleDTO> sevkler = sevkService.getSevklerByHastaId(hastaId, talepEdenKullaniciId);
            return ResponseEntity.ok(sevkler);
        } catch (AccessDeniedException e) {
            log.warn("Hastanın (ID: {}) sevklerini görüntüleme yetki hatası: {}", hastaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Hastanın (ID: {}) sevkleri getirilirken beklenmedik bir hata oluştu:", hastaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Sevk listesi getirilirken sunucu hatası.");
        }
    }

    @GetMapping("/doktor/{doktorPersonelId}")
    @PreAuthorize("isAuthenticated()") // Yetki kontrolü serviste
    public ResponseEntity<?> getSevklerByDoktorId(@PathVariable Integer doktorPersonelId) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            log.info("GET /api/sevkler/doktor/{} çağrıldı. Talep Eden Kullanıcı ID: {}", doktorPersonelId, talepEdenKullaniciId);
            List<SevkGoruntuleDTO> sevkler = sevkService.getSevklerByDoktorId(doktorPersonelId, talepEdenKullaniciId);
            return ResponseEntity.ok(sevkler);
        } catch (AccessDeniedException e) {
            log.warn("Doktorun (Personel ID: {}) sevklerini görüntüleme yetki hatası: {}", doktorPersonelId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Doktorun (Personel ID: {}) sevkleri getirilirken beklenmedik bir hata oluştu:", doktorPersonelId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Sevk listesi getirilirken sunucu hatası.");
        }
    }

    // Sadece ADMIN veya YONETICI tüm sevkleri veya duruma göre filtrelenmiş sevkleri görebilir
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'YONETICI')")
    public ResponseEntity<?> getAllOrFilteredSevkler(@RequestParam(required = false) String durum) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            log.info("GET /api/sevkler (Admin/Yonetici) çağrıldı. Durum: {}, Talep Eden ID: {}", durum, talepEdenKullaniciId);
            List<SevkGoruntuleDTO> sevkler;
            if (durum != null && !durum.trim().isEmpty()) {
                sevkler = sevkService.getSevklerByDurum(durum.toUpperCase(), talepEdenKullaniciId);
            } else {
                sevkler = sevkService.getAllSevkler(talepEdenKullaniciId);
            }
            return ResponseEntity.ok(sevkler);
        } catch (AccessDeniedException e) {
            log.warn("Sevkleri listeleme yetki hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Sevkler (Admin/Yonetici) listelenirken beklenmedik bir hata oluştu:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Sevk listesi getirilirken sunucu hatası.");
        }
    }
}