package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.AcilDurumKaydiGuncelleDTO;
import com.hastane.hastanebackend.dto.AcilDurumKaydiGoruntuleDTO;
import com.hastane.hastanebackend.dto.AcilDurumKaydiOlusturDTO;
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.service.AcilDurumKaydiService;

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
import java.util.ArrayList; // Eklendi
import java.util.Comparator; // Eklendi
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/acildurumkayitlari")
public class AcilDurumKaydiController {

    private static final Logger log = LoggerFactory.getLogger(AcilDurumKaydiController.class);
    private final AcilDurumKaydiService acilDurumKaydiService;
    private final KullaniciRepository kullaniciRepository;

    public AcilDurumKaydiController(AcilDurumKaydiService acilDurumKaydiService, KullaniciRepository kullaniciRepository) {
        this.acilDurumKaydiService = acilDurumKaydiService;
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
    @PreAuthorize("hasRole('HEMSIRE')")
    public ResponseEntity<?> createAcilDurumKaydi(@Valid @RequestBody AcilDurumKaydiOlusturDTO dto) {
        try {
            Integer tetikleyenHemsireKullaniciId = getAktifKullaniciId();
            log.info("POST /api/acildurumkayitlari çağrıldı. Tetikleyen Hemşire ID: {}", tetikleyenHemsireKullaniciId);
            AcilDurumKaydiGoruntuleDTO yeniKayit = acilDurumKaydiService.createAcilDurumKaydi(dto, tetikleyenHemsireKullaniciId);
            return new ResponseEntity<>(yeniKayit, HttpStatus.CREATED);
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            log.warn("Acil durum kaydı oluşturma hatası: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) {
            log.warn("Acil durum kaydı oluşturma yetki hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Acil durum kaydı oluşturulurken beklenmedik bir hata oluştu:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Acil durum kaydı oluşturulurken sunucu hatası.");
        }
    }

    @PutMapping("/{kayitId}/durum")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOKTOR', 'HEMSIRE')")
    public ResponseEntity<?> updateAcilDurumKaydiDurumu(
            @PathVariable Integer kayitId,
            @Valid @RequestBody AcilDurumKaydiGuncelleDTO guncelleDTO) {
        try {
            Integer yapanKullaniciId = getAktifKullaniciId();
            log.info("PUT /api/acildurumkayitlari/{}/durum çağrıldı. Yeni Durum: {}, Yapan Kullanıcı ID: {}", kayitId, guncelleDTO.getYeniDurum(), yapanKullaniciId);
            AcilDurumKaydiGoruntuleDTO guncellenmisKayit = acilDurumKaydiService.updateAcilDurumKaydiDurumu(kayitId, guncelleDTO, yapanKullaniciId);
            return ResponseEntity.ok(guncellenmisKayit);
        } catch (ResourceNotFoundException | IllegalArgumentException | IllegalStateException e) {
            log.warn("Acil durum kaydı durumu güncelleme hatası: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) {
            log.warn("Acil durum kaydı durumu güncelleme yetki hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Acil durum kaydı (ID: {}) durumu güncellenirken beklenmedik bir hata oluştu:", kayitId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Acil durum kaydı durumu güncellenirken sunucu hatası.");
        }
    }

    @GetMapping("/{kayitId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAcilDurumKaydiById(@PathVariable Integer kayitId) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            log.debug("GET /api/acildurumkayitlari/{} çağrıldı. Talep Eden ID: {}", kayitId, talepEdenKullaniciId);
            Optional<AcilDurumKaydiGoruntuleDTO> dto = acilDurumKaydiService.getAcilDurumKaydiById(kayitId, talepEdenKullaniciId);
            return dto.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Acil durum kaydı bulunamadı."));
        } catch (AccessDeniedException e) {
            log.warn("Acil durum kaydı görüntüleme yetki hatası (ID: {}): {}", kayitId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Acil durum kaydı (ID: {}) getirilirken hata: ",kayitId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Sunucu hatası.");
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'YONETICI', 'DOKTOR', 'HEMSIRE')")
    public ResponseEntity<?> getAllOrFilteredAcilDurumKayitlari(
            @RequestParam(required = false) String durum,
            @RequestParam(required = false) Integer hastaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tarih) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            log.info("GET /api/acildurumkayitlari çağrıldı. Durum: {}, HastaID: {}, Tarih: {}, Talep Eden ID: {}", durum, hastaId, tarih, talepEdenKullaniciId);
            List<AcilDurumKaydiGoruntuleDTO> kayitlar;

            if (hastaId != null) {
                kayitlar = acilDurumKaydiService.getAcilDurumKayitlariByHastaId(hastaId, talepEdenKullaniciId);
            } else if (durum != null && !durum.trim().isEmpty()) {
                kayitlar = acilDurumKaydiService.getAcilDurumKayitlariByDurum(durum.toUpperCase(), talepEdenKullaniciId);
            } else if (tarih != null) {
                kayitlar = acilDurumKaydiService.getAcilDurumKayitlariByTarih(tarih, talepEdenKullaniciId);
            } else {
                kayitlar = acilDurumKaydiService.getAllAcilDurumKayitlari(talepEdenKullaniciId);
            }
            return ResponseEntity.ok(kayitlar);
        } catch (AccessDeniedException e) {
            log.warn("Acil durum kayıtlarını listeleme yetki hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Acil durum kayıtları listelenirken beklenmedik bir hata oluştu:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Acil durum kayıtları listelenirken sunucu hatası.");
        }
    }

    // --- YENİ ENDPOINT (Doktor Dashboard'u için) ---
    @GetMapping("/aktif-ve-mudahale")
    @PreAuthorize("hasRole('DOKTOR')")
    public ResponseEntity<?> getAktifVeMudahaleEdilenAcilDurumKayitlari() {
        log.info("GET /api/acildurumkayitlari/aktif-ve-mudahale çağrıldı (Doktor için)");
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            List<AcilDurumKaydiGoruntuleDTO> aktifKayitlar = acilDurumKaydiService.getAcilDurumKayitlariByDurum("AKTIF", talepEdenKullaniciId);
            List<AcilDurumKaydiGoruntuleDTO> mudahaleKayitlari = acilDurumKaydiService.getAcilDurumKayitlariByDurum("MÜDAHALE EDİLİYOR", talepEdenKullaniciId);
            
            List<AcilDurumKaydiGoruntuleDTO> birlesikListe = new ArrayList<>(aktifKayitlar);
            birlesikListe.addAll(mudahaleKayitlari);
            
            birlesikListe.sort(Comparator.comparing(AcilDurumKaydiGoruntuleDTO::getOlayZamani, Comparator.nullsLast(Comparator.reverseOrder())));
            
            return ResponseEntity.ok(birlesikListe);
        } catch (AccessDeniedException e) {
            log.warn("Aktif acil durum kayıtlarını listeleme yetki hatası (Doktor): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Aktif acil durum kayıtları listelenirken beklenmedik bir hata oluştu (Doktor):", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Acil durum kayıtları listelenirken sunucu hatası.");
        }
    }
    // --- YENİ ENDPOINT SONU ---
}