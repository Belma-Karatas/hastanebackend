package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.KatDTO;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.service.KatService;

import jakarta.validation.Valid; // @Valid anotasyonu için
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/katlar")
// @CrossOrigin(origins = "http://localhost:5173") // Global ayar yeterli olmalı
public class KatController {

    private static final Logger log = LoggerFactory.getLogger(KatController.class);
    private final KatService katService;

    public KatController(KatService katService) {
        this.katService = katService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()") // Veya daha spesifik bir rol: hasAnyRole('ADMIN', 'DOKTOR', 'HEMSIRE')
    public ResponseEntity<List<KatDTO>> getAllKatlar() {
        log.info("GET /api/katlar çağrıldı");
        List<KatDTO> katlar = katService.getAllKatlar();
        return ResponseEntity.ok(katlar);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Veya daha spesifik bir rol
    public ResponseEntity<KatDTO> getKatById(@PathVariable("id") Integer id) {
        log.info("GET /api/katlar/{} çağrıldı", id);
        return katService.getKatById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    log.warn("Kat bulunamadı, ID: {}", id);
                    return new ResourceNotFoundException("Kat bulunamadı, ID: " + id);
                });
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createKat(@Valid @RequestBody KatDTO katDTO) {
        log.info("POST /api/katlar çağrıldı, DTO: {}", katDTO.getAd());
        // KatDTO'da ID alanı varsa ve dolu gelirse, yeni kayıt için uygun değil.
        if (katDTO.getId() != null) {
            log.warn("Yeni kat oluşturulurken ID belirtildi: {}", katDTO.getId());
            return ResponseEntity.badRequest().body("Yeni kat oluşturulurken ID belirtilmemelidir.");
        }
        try {
            KatDTO yeniKat = katService.createKat(katDTO);
            log.info("Kat başarıyla oluşturuldu: {}", yeniKat.getAd());
            return new ResponseEntity<>(yeniKat, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Kat oluşturma hatası: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateKat(@PathVariable("id") Integer id, @Valid @RequestBody KatDTO katDTO) {
        log.info("PUT /api/katlar/{} çağrıldı, DTO: {}", id, katDTO.getAd());
        try {
            KatDTO guncellenmisKat = katService.updateKat(id, katDTO);
            log.info("Kat başarıyla güncellendi: {}", guncellenmisKat.getAd());
            return ResponseEntity.ok(guncellenmisKat);
        } catch (ResourceNotFoundException e) {
            log.warn("Kat güncelleme hatası - bulunamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Kat güncelleme hatası - geçersiz argüman: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteKat(@PathVariable("id") Integer id) {
        log.info("DELETE /api/katlar/{} çağrıldı", id);
        try {
            katService.deleteKat(id);
            log.info("Kat başarıyla silindi, ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.warn("Kat silme hatası - bulunamadı: {}", e.getMessage());
            // Void dönen bir endpoint için body'ye mesaj yazmak yerine sadece status dönmek daha yaygın.
            return ResponseEntity.notFound().build();
        }
        // DataIntegrityViolationException gibi durumlar için global bir exception handler (ControllerAdvice)
        // daha merkezi bir çözüm sunar. Şimdilik bu şekilde bırakıyoruz.
    }
}