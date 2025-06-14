package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.ReceteGoruntuleDTO;
import com.hastane.hastanebackend.dto.ReceteIlacDetayDTO; // İlaç ekleme için kullanacağız
import com.hastane.hastanebackend.dto.ReceteOlusturDTO;
import com.hastane.hastanebackend.entity.Kullanici; // Aktif kullanıcıyı almak için
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.KullaniciRepository; // Aktif kullanıcıyı almak için
import com.hastane.hastanebackend.service.ReceteService;

import jakarta.validation.Valid; // DTO validasyonu için
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
@RequestMapping("/api/receteler")
// @CrossOrigin(origins = "http://localhost:5173") // SecurityConfig'deki global ayar genellikle yeterlidir
public class ReceteController {

    private static final Logger log = LoggerFactory.getLogger(ReceteController.class);
    private final ReceteService receteService;
    private final KullaniciRepository kullaniciRepository; // Aktif kullanıcı ID'sini almak için

    public ReceteController(ReceteService receteService, KullaniciRepository kullaniciRepository) {
        this.receteService = receteService;
        this.kullaniciRepository = kullaniciRepository;
    }

    // Helper metot: Aktif kullanıcı ID'sini alır
    private Integer getAktifKullaniciId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            log.warn("getAktifKullaniciId: Kimlik doğrulanmamış veya geçersiz principal.");
            throw new IllegalStateException("Bu işlem için kimlik doğrulaması gerekmektedir.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Kullanici kullanici = kullaniciRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> {
                    log.error("getAktifKullaniciId: Aktif kullanıcı veritabanında bulunamadı. Email: {}", userDetails.getUsername());
                    return new RuntimeException("Aktif kullanıcı bilgileri alınamadı.");
                });
        return kullanici.getId();
    }

    @PostMapping
    @PreAuthorize("hasRole('DOKTOR')")
    public ResponseEntity<?> createRecete(@Valid @RequestBody ReceteOlusturDTO receteOlusturDTO) {
        try {
            Integer doktorKullaniciId = getAktifKullaniciId();
            ReceteGoruntuleDTO olusturulanRecete = receteService.createRecete(receteOlusturDTO, doktorKullaniciId);
            return new ResponseEntity<>(olusturulanRecete, HttpStatus.CREATED);
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            log.warn("Reçete oluşturma hatası (client error): {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) {
            log.warn("Reçete oluşturma yetki hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Reçete oluşturulurken beklenmedik bir hata oluştu:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Reçete oluşturulurken sunucu hatası oluştu.");
        }
    }

    @GetMapping("/{receteId}")
    @PreAuthorize("isAuthenticated()") // Yetki kontrolü serviste yapılıyor
    public ResponseEntity<?> getReceteById(@PathVariable Integer receteId) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            Optional<ReceteGoruntuleDTO> receteDTO = receteService.getReceteById(receteId, talepEdenKullaniciId);
            return receteDTO.<ResponseEntity<?>>map(ResponseEntity::ok)
                              .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Belirtilen ID ile reçete bulunamadı."));
        } catch (AccessDeniedException e) {
            log.warn("Reçete görüntüleme yetki hatası (ID: {}): {}", receteId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Reçete (ID: {}) getirilirken beklenmedik bir hata oluştu:", receteId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Reçete bilgileri getirilirken sunucu hatası oluştu.");
        }
    }

    @GetMapping("/muayene/{muayeneId}")
    @PreAuthorize("isAuthenticated()") // Yetki kontrolü serviste yapılıyor
    public ResponseEntity<?> getRecetelerByMuayeneId(@PathVariable Integer muayeneId) {
         try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            List<ReceteGoruntuleDTO> receteler = receteService.getRecetelerByMuayeneId(muayeneId, talepEdenKullaniciId);
            return ResponseEntity.ok(receteler);
        } catch (AccessDeniedException e) {
            log.warn("Muayenenin (ID: {}) reçetelerini görüntüleme yetki hatası: {}", muayeneId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Muayenenin (ID: {}) reçeteleri getirilirken beklenmedik bir hata oluştu:", muayeneId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Reçete listesi getirilirken sunucu hatası oluştu.");
        }
    }

    @GetMapping("/hasta/{hastaId}")
    @PreAuthorize("isAuthenticated()") // Yetki kontrolü serviste yapılıyor
    public ResponseEntity<?> getRecetelerByHastaId(@PathVariable Integer hastaId) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            List<ReceteGoruntuleDTO> receteler = receteService.getRecetelerByHastaId(hastaId, talepEdenKullaniciId);
            return ResponseEntity.ok(receteler);
        } catch (AccessDeniedException e) {
            log.warn("Hastanın (ID: {}) reçetelerini görüntüleme yetki hatası: {}", hastaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Hastanın (ID: {}) reçeteleri getirilirken beklenmedik bir hata oluştu:", hastaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Reçete listesi getirilirken sunucu hatası oluştu.");
        }
    }

    @PostMapping("/{receteId}/ilaclar")
    @PreAuthorize("hasRole('DOKTOR')") // Sadece doktorlar reçeteye ilaç ekleyebilir
    public ResponseEntity<?> addIlacToRecete(
            @PathVariable Integer receteId,
            @Valid @RequestBody ReceteIlacDetayDTO ilacEkleDTO) {
        try {
            Integer doktorKullaniciId = getAktifKullaniciId();
            ReceteGoruntuleDTO guncellenmisRecete = receteService.addIlacToRecete(receteId, ilacEkleDTO, doktorKullaniciId);
            return ResponseEntity.ok(guncellenmisRecete);
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            log.warn("Reçeteye ilaç ekleme hatası (client error, Reçete ID: {}): {}", receteId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) {
            log.warn("Reçeteye ilaç ekleme yetki hatası (Reçete ID: {}): {}", receteId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Reçeteye (ID: {}) ilaç eklenirken beklenmedik bir hata oluştu:", receteId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Reçeteye ilaç eklenirken sunucu hatası oluştu.");
        }
    }

    @DeleteMapping("/{receteId}/ilaclar/{receteIlacId}")
    @PreAuthorize("hasRole('DOKTOR')") // Sadece doktorlar reçeteden ilaç çıkarabilir
    public ResponseEntity<?> removeIlacFromRecete(
            @PathVariable Integer receteId,
            @PathVariable Integer receteIlacId) {
        try {
            Integer doktorKullaniciId = getAktifKullaniciId();
            ReceteGoruntuleDTO guncellenmisRecete = receteService.removeIlacFromRecete(receteId, receteIlacId, doktorKullaniciId);
            return ResponseEntity.ok(guncellenmisRecete);
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            log.warn("Reçeteden ilaç çıkarma hatası (client error, Reçete ID: {}, ReceteIlac ID: {}): {}", receteId, receteIlacId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) {
            log.warn("Reçeteden ilaç çıkarma yetki hatası (Reçete ID: {}): {}", receteId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Reçeteden (ID: {}) ilaç (ReceteIlac ID: {}) çıkarılırken beklenmedik bir hata oluştu:", receteId, receteIlacId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Reçeteden ilaç çıkarılırken sunucu hatası oluştu.");
        }
    }
}