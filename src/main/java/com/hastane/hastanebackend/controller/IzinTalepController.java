package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.IzinTalepDurumGuncelleDTO;
import com.hastane.hastanebackend.dto.IzinTalepGoruntuleDTO;
import com.hastane.hastanebackend.dto.IzinTalepOlusturDTO;
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.service.IzinTalepService;

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
@RequestMapping("/api/izintalepleri")
// @CrossOrigin(...) // Global ayar yeterli olmalı
public class IzinTalepController {

    private static final Logger log = LoggerFactory.getLogger(IzinTalepController.class);
    private final IzinTalepService izinTalepService;
    private final KullaniciRepository kullaniciRepository; // Aktif kullanıcı ID'sini almak için

    public IzinTalepController(IzinTalepService izinTalepService, KullaniciRepository kullaniciRepository) {
        this.izinTalepService = izinTalepService;
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
    @PreAuthorize("isAuthenticated()") // İzin talebini herhangi bir giriş yapmış personel yapabilir
    public ResponseEntity<?> createIzinTalep(@Valid @RequestBody IzinTalepOlusturDTO izinTalepOlusturDTO) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            log.info("POST /api/izintalepleri çağrıldı. Talep Eden Kullanıcı ID: {}", talepEdenKullaniciId);
            IzinTalepGoruntuleDTO yeniTalep = izinTalepService.createIzinTalep(izinTalepOlusturDTO, talepEdenKullaniciId);
            return new ResponseEntity<>(yeniTalep, HttpStatus.CREATED);
        } catch (ResourceNotFoundException | IllegalArgumentException | IllegalStateException e) {
            log.warn("İzin talebi oluşturma hatası: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("İzin talebi oluşturulurken beklenmedik bir hata oluştu:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("İzin talebi oluşturulurken sunucu hatası.");
        }
    }

    @PutMapping("/{izinTalepId}/durum")
    @PreAuthorize("hasRole('ADMIN')") // Sadece ADMIN durumu güncelleyebilir
    public ResponseEntity<?> updateIzinTalepDurumu(
            @PathVariable Integer izinTalepId,
            @Valid @RequestBody IzinTalepDurumGuncelleDTO durumGuncelleDTO) {
        try {
            Integer onaylayanKullaniciId = getAktifKullaniciId();
            log.info("PUT /api/izintalepleri/{}/durum çağrıldı. Yeni Durum: {}, Onaylayan Kullanıcı ID: {}", izinTalepId, durumGuncelleDTO.getYeniDurum(), onaylayanKullaniciId);
            IzinTalepGoruntuleDTO guncellenmisTalep = izinTalepService.updateIzinTalepDurumu(izinTalepId, durumGuncelleDTO, onaylayanKullaniciId);
            return ResponseEntity.ok(guncellenmisTalep);
        } catch (ResourceNotFoundException | IllegalStateException e) {
            log.warn("İzin talebi durumu güncelleme hatası: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) {
            log.warn("İzin talebi durumu güncelleme yetki hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("İzin talebi (ID: {}) durumu güncellenirken beklenmedik bir hata oluştu:", izinTalepId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("İzin talebi durumu güncellenirken sunucu hatası.");
        }
    }

    @GetMapping("/{izinTalepId}")
    @PreAuthorize("isAuthenticated()") // Yetki kontrolü serviste yapılıyor (talep sahibi veya admin)
    public ResponseEntity<?> getIzinTalepById(@PathVariable Integer izinTalepId) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            log.debug("GET /api/izintalepleri/{} çağrıldı. Talep Eden Kullanıcı ID: {}", izinTalepId, talepEdenKullaniciId);
            Optional<IzinTalepGoruntuleDTO> dto = izinTalepService.getIzinTalepById(izinTalepId, talepEdenKullaniciId);
            return dto.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("İzin talebi bulunamadı."));
        } catch (AccessDeniedException e) {
            log.warn("İzin talebi görüntüleme yetki hatası (ID: {}): {}", izinTalepId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("İzin talebi (ID: {}) getirilirken beklenmedik bir hata oluştu:", izinTalepId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("İzin talebi bilgileri getirilirken sunucu hatası.");
        }
    }

    @GetMapping("/personel/mevcut") // Giriş yapmış personelin kendi izin talepleri
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyIzinTalepleri() {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            log.info("GET /api/izintalepleri/personel/mevcut çağrıldı. Talep Eden Kullanıcı ID: {}", talepEdenKullaniciId);
            List<IzinTalepGoruntuleDTO> talepler = izinTalepService.getIzinTalepleriByPersonel(talepEdenKullaniciId, talepEdenKullaniciId);
            return ResponseEntity.ok(talepler);
        } catch (Exception e) {
            log.error("Mevcut personelin izin talepleri getirilirken beklenmedik bir hata oluştu:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("İzin talepleri listelenirken sunucu hatası.");
        }
    }
    
    // Admin'in belirli bir personelin izin taleplerini görmesi için (opsiyonel)
    @GetMapping("/personel/{personelKullaniciId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getIzinTalepleriByPersonelIdForAdmin(@PathVariable Integer personelKullaniciId) {
        try {
            Integer adminKullaniciId = getAktifKullaniciId(); // Adminin kendi ID'si
            log.info("GET /api/izintalepleri/personel/{} (ADMIN) çağrıldı. Admin ID: {}", personelKullaniciId, adminKullaniciId);
            List<IzinTalepGoruntuleDTO> talepler = izinTalepService.getIzinTalepleriByPersonel(personelKullaniciId, adminKullaniciId);
            return ResponseEntity.ok(talepler);
        } catch (AccessDeniedException e) { // Servis katmanından gelebilir
            log.warn("Admin, personelin (Kullanıcı ID: {}) izinlerini görüntüleme yetkisine sahip değil: {}", personelKullaniciId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Personelin (Kullanıcı ID: {}) izin talepleri getirilirken (ADMIN) beklenmedik bir hata oluştu:", personelKullaniciId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("İzin talepleri listelenirken sunucu hatası.");
        }
    }


    @GetMapping // ADMIN için tüm talepleri veya duruma göre filtrelenmiş talepleri getirir
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getIzinTalepleriForAdmin(@RequestParam(required = false) String durum) {
        try {
            Integer adminKullaniciId = getAktifKullaniciId();
            log.info("GET /api/izintalepleri (ADMIN) çağrıldı. Durum: {}, Admin ID: {}", durum, adminKullaniciId);
            List<IzinTalepGoruntuleDTO> talepler;
            if (durum != null && !durum.trim().isEmpty()) {
                talepler = izinTalepService.getIzinTalepleriByDurum(durum.toUpperCase(), adminKullaniciId);
            } else {
                talepler = izinTalepService.getAllIzinTalepleri(adminKullaniciId);
            }
            return ResponseEntity.ok(talepler);
        } catch (AccessDeniedException e) {
            log.warn("Admin, izin taleplerini listeleme yetkisine sahip değil: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("İzin talepleri (ADMIN) listelenirken beklenmedik bir hata oluştu:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("İzin talepleri listelenirken sunucu hatası.");
        }
    }
}