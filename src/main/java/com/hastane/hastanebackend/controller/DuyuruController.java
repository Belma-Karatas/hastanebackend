package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.DuyuruDTO;
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.service.DuyuruService;

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

@RestController
@RequestMapping("/api/duyurular")
// @CrossOrigin(...)
public class DuyuruController {

    private static final Logger log = LoggerFactory.getLogger(DuyuruController.class);
    private final DuyuruService duyuruService;
    private final KullaniciRepository kullaniciRepository; // Aktif kullanıcı ID'sini almak için

    public DuyuruController(DuyuruService duyuruService, KullaniciRepository kullaniciRepository) {
        this.duyuruService = duyuruService;
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

    @GetMapping
    @PreAuthorize("isAuthenticated()") // Tüm giriş yapmış kullanıcılar duyuruları görebilir
    public ResponseEntity<List<DuyuruDTO>> getAllDuyurular() {
        log.info("GET /api/duyurular çağrıldı");
        List<DuyuruDTO> duyurular = duyuruService.getAllDuyurular();
        return ResponseEntity.ok(duyurular);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DuyuruDTO> getDuyuruById(@PathVariable("id") Integer id) {
        log.info("GET /api/duyurular/{} çağrıldı", id);
        return duyuruService.getDuyuruById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    log.warn("Duyuru bulunamadı, ID: {}", id);
                    return new ResourceNotFoundException("Duyuru bulunamadı, ID: " + id);
                });
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createDuyuru(@Valid @RequestBody DuyuruDTO duyuruDTO) {
        try {
            Integer adminKullaniciId = getAktifKullaniciId();
            log.info("POST /api/duyurular çağrıldı. Başlık: {}, Yapan Admin ID: {}", duyuruDTO.getBaslik(), adminKullaniciId);
            if (duyuruDTO.getId() != null) {
                return ResponseEntity.badRequest().body("Yeni duyuru oluşturulurken ID belirtilmemelidir.");
            }
            DuyuruDTO yeniDuyuru = duyuruService.createDuyuru(duyuruDTO, adminKullaniciId);
            return new ResponseEntity<>(yeniDuyuru, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) { // Admin'in personel kaydı bulunamazsa
            log.warn("Duyuru oluşturma hatası - admin personel kaydı bulunamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Duyuru oluşturulurken beklenmedik bir hata: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Duyuru oluşturulurken bir hata oluştu.");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateDuyuru(@PathVariable("id") Integer id, @Valid @RequestBody DuyuruDTO duyuruDTO) {
        try {
            Integer adminKullaniciId = getAktifKullaniciId();
            log.info("PUT /api/duyurular/{} çağrıldı. Başlık: {}, Yapan Admin ID: {}", id, duyuruDTO.getBaslik(), adminKullaniciId);
            DuyuruDTO guncellenmisDuyuru = duyuruService.updateDuyuru(id, duyuruDTO, adminKullaniciId);
            return ResponseEntity.ok(guncellenmisDuyuru);
        } catch (ResourceNotFoundException e) {
            log.warn("Duyuru güncelleme hatası - bulunamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AccessDeniedException e) {
             log.warn("Duyuru güncelleme yetki hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Duyuru (ID: {}) güncellenirken beklenmedik bir hata: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Duyuru güncellenirken bir hata oluştu.");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDuyuru(@PathVariable("id") Integer id) { // ResponseEntity<?> olarak güncellendi
        try {
            Integer adminKullaniciId = getAktifKullaniciId();
            log.info("DELETE /api/duyurular/{} çağrıldı. Yapan Admin ID: {}", id, adminKullaniciId);
            duyuruService.deleteDuyuru(id, adminKullaniciId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.warn("Duyuru silme hatası - bulunamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AccessDeniedException e) {
            log.warn("Duyuru silme yetki hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Duyuru (ID: {}) silinirken beklenmedik bir hata: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Duyuru silinirken bir hata oluştu.");
        }
    }
}