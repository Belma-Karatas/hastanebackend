package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.HemsireAtaDTO; // YENİ IMPORT
import com.hastane.hastanebackend.dto.YatisGoruntuleDTO;
import com.hastane.hastanebackend.dto.YatisOlusturDTO;
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.service.YatisService;

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
@RequestMapping("/api/yatislar")
public class YatisController {

    private static final Logger log = LoggerFactory.getLogger(YatisController.class);
    private final YatisService yatisService;
    private final KullaniciRepository kullaniciRepository;

    public YatisController(YatisService yatisService, KullaniciRepository kullaniciRepository) {
        this.yatisService = yatisService;
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

    // ... (hastaYatisiYap, hastaTaburcuEt ve diğer GET endpoint'leri aynı kalacak)
    // Önceki mesajdaki gibi olacaklar, buraya tekrar kopyalamıyorum.

    @PostMapping("/yatir")
    @PreAuthorize("hasAnyRole('ADMIN', 'HASTA_KABUL', 'DOKTOR', 'HEMSIRE')")
    public ResponseEntity<?> hastaYatisiYap(@Valid @RequestBody YatisOlusturDTO yatisOlusturDTO) {
        // ... (içerik aynı)
        log.info("POST /api/yatislar/yatir çağrıldı. Hasta ID: {}", yatisOlusturDTO.getHastaId());
        try {
            Integer yapanKullaniciId = getAktifKullaniciId();
            YatisGoruntuleDTO yeniYatis = yatisService.hastaYatisiYap(yatisOlusturDTO, yapanKullaniciId);
            log.info("Hasta yatışı başarıyla oluşturuldu. Yatış ID: {}", yeniYatis.getId());
            return new ResponseEntity<>(yeniYatis, HttpStatus.CREATED);
        } catch (ResourceNotFoundException | IllegalArgumentException | IllegalStateException e) {
            log.warn("Hasta yatışı oluşturma hatası: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) {
            log.warn("Hasta yatışı oluşturma yetki hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Hasta yatışı oluşturulurken beklenmedik bir hata oluştu:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hasta yatışı oluşturulurken sunucu hatası oluştu.");
        }
    }

    @PutMapping("/{yatisId}/taburcu")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOKTOR', 'HEMSIRE')")
    public ResponseEntity<?> hastaTaburcuEt(@PathVariable Integer yatisId) {
        // ... (içerik aynı)
        log.info("PUT /api/yatislar/{}/taburcu çağrıldı", yatisId);
        try {
            Integer yapanKullaniciId = getAktifKullaniciId();
            YatisGoruntuleDTO taburcuEdilmisYatis = yatisService.hastaTaburcuEt(yatisId, yapanKullaniciId);
            log.info("Hasta başarıyla taburcu edildi. Yatış ID: {}", taburcuEdilmisYatis.getId());
            return ResponseEntity.ok(taburcuEdilmisYatis);
        } catch (ResourceNotFoundException | IllegalStateException e) {
            log.warn("Hasta taburcu etme hatası: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) {
            log.warn("Hasta taburcu etme yetki hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Hasta taburcu edilirken beklenmedik bir hata oluştu (Yatış ID: {}):", yatisId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hasta taburcu edilirken sunucu hatası oluştu.");
        }
    }

    @GetMapping("/{yatisId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getYatisById(@PathVariable Integer yatisId) {
        // ... (içerik aynı)
        log.info("GET /api/yatislar/{} çağrıldı", yatisId);
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            Optional<YatisGoruntuleDTO> yatisDTO = yatisService.getYatisById(yatisId, talepEdenKullaniciId);
            return yatisDTO.<ResponseEntity<?>>map(ResponseEntity::ok)
                           .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Yatış kaydı bulunamadı."));
        } catch (AccessDeniedException e) {
            log.warn("Yatış görüntüleme yetki hatası (ID: {}): {}", yatisId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Yatış (ID: {}) getirilirken beklenmedik bir hata oluştu:", yatisId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Yatış bilgileri getirilirken sunucu hatası oluştu.");
        }
    }

    @GetMapping("/hasta/{hastaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTumYatislarByHastaId(@PathVariable Integer hastaId) {
        // ... (içerik aynı)
        log.info("GET /api/yatislar/hasta/{} çağrıldı", hastaId);
         try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            List<YatisGoruntuleDTO> yatislar = yatisService.getTumYatislarByHastaId(hastaId, talepEdenKullaniciId);
            return ResponseEntity.ok(yatislar);
        } catch (AccessDeniedException e) {
            log.warn("Hastanın (ID: {}) yatışlarını görüntüleme yetki hatası: {}", hastaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Hastanın (ID: {}) yatışları getirilirken beklenmedik bir hata oluştu:", hastaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Yatış listesi getirilirken sunucu hatası oluştu.");
        }
    }
    
    @GetMapping("/hasta/{hastaId}/aktif")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAktifYatisByHastaId(@PathVariable Integer hastaId) {
        // ... (içerik aynı)
        log.info("GET /api/yatislar/hasta/{}/aktif çağrıldı", hastaId);
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            Optional<YatisGoruntuleDTO> yatisDTO = yatisService.getAktifYatisByHastaId(hastaId, talepEdenKullaniciId);
             return yatisDTO.<ResponseEntity<?>>map(ResponseEntity::ok)
                           .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hastanın aktif yatış kaydı bulunamadı."));
        } catch (AccessDeniedException e) {
            log.warn("Hastanın (ID: {}) aktif yatışını görüntüleme yetki hatası: {}", hastaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Hastanın (ID: {}) aktif yatışı getirilirken beklenmedik bir hata oluştu:", hastaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Aktif yatış bilgisi getirilirken sunucu hatası oluştu.");
        }
    }

    @GetMapping("/yatak/{yatakId}/aktif")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAktifYatisByYatakId(@PathVariable Integer yatakId) {
        // ... (içerik aynı)
        log.info("GET /api/yatislar/yatak/{}/aktif çağrıldı", yatakId);
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            Optional<YatisGoruntuleDTO> yatisDTO = yatisService.getAktifYatisByYatakId(yatakId, talepEdenKullaniciId);
            return yatisDTO.<ResponseEntity<?>>map(ResponseEntity::ok)
                           .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Yatakta aktif yatış kaydı bulunamadı."));
        } catch (AccessDeniedException e) {
            log.warn("Yatağın (ID: {}) aktif yatışını görüntüleme yetki hatası: {}", yatakId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Yatağın (ID: {}) aktif yatışı getirilirken beklenmedik bir hata oluştu:", yatakId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Aktif yatış bilgisi getirilirken sunucu hatası oluştu.");
        }
    }

    @GetMapping("/aktif")
    @PreAuthorize("hasAnyRole('ADMIN', 'YONETICI', 'HASTA_KABUL', 'DOKTOR', 'HEMSIRE')")
    public ResponseEntity<?> getTumAktifYatislar() {
        // ... (içerik aynı)
        log.info("GET /api/yatislar/aktif çağrıldı");
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            List<YatisGoruntuleDTO> aktifYatislar = yatisService.getTumAktifYatislar(talepEdenKullaniciId);
            return ResponseEntity.ok(aktifYatislar);
        } catch (AccessDeniedException e) {
            log.warn("Tüm aktif yatışları görüntüleme yetki hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Tüm aktif yatışlar getirilirken beklenmedik bir hata oluştu:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Aktif yatış listesi getirilirken sunucu hatası oluştu.");
        }
    }

    // --- YENİ EKLENEN ENDPOINT'LER ---
    @PostMapping("/{yatisId}/hemsireler")
    @PreAuthorize("hasRole('ADMIN')") // Senin isteğin üzerine sadece ADMIN
    public ResponseEntity<?> hemsireAta(
            @PathVariable Integer yatisId,
            @Valid @RequestBody HemsireAtaDTO hemsireAtaDTO) {
        log.info("POST /api/yatislar/{}/hemsireler çağrıldı. Hemşire Personel ID: {}", yatisId, hemsireAtaDTO.getHemsirePersonelId());
        try {
            Integer yapanKullaniciId = getAktifKullaniciId();
            YatisGoruntuleDTO guncellenmisYatis = yatisService.hemsireAta(yatisId, hemsireAtaDTO, yapanKullaniciId);
            return ResponseEntity.ok(guncellenmisYatis);
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            log.warn("Yatışa hemşire atama hatası (Yatış ID: {}): {}", yatisId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) {
            log.warn("Yatışa hemşire atama yetki hatası (Yatış ID: {}): {}", yatisId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Yatışa (ID: {}) hemşire atanırken beklenmedik bir hata oluştu:", yatisId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Yatışa hemşire atanırken sunucu hatası oluştu.");
        }
    }

    @DeleteMapping("/{yatisId}/hemsireler/{yatisHemsireAtamaId}")
    @PreAuthorize("hasRole('ADMIN')") // Senin isteğin üzerine sadece ADMIN
    public ResponseEntity<?> hemsireAtamasiniKaldir(
            @PathVariable Integer yatisId,
            @PathVariable Integer yatisHemsireAtamaId) {
        log.info("DELETE /api/yatislar/{}/hemsireler/{} çağrıldı", yatisId, yatisHemsireAtamaId);
        try {
            Integer yapanKullaniciId = getAktifKullaniciId();
            YatisGoruntuleDTO guncellenmisYatis = yatisService.hemsireAtamasiniKaldir(yatisId, yatisHemsireAtamaId, yapanKullaniciId);
            return ResponseEntity.ok(guncellenmisYatis);
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            log.warn("Yatıştaki hemşire atamasını kaldırma hatası (Yatış ID: {}, Atama ID: {}): {}", yatisId, yatisHemsireAtamaId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) {
            log.warn("Yatıştaki hemşire atamasını kaldırma yetki hatası (Yatış ID: {}): {}", yatisId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Yatıştaki (ID: {}) hemşire ataması (ID: {}) kaldırılırken beklenmedik bir hata oluştu:", yatisId, yatisHemsireAtamaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hemşire ataması kaldırılırken sunucu hatası oluştu.");
        }
    }
}