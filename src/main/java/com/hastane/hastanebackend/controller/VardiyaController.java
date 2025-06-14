package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.VardiyaDTO;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.service.VardiyaService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vardiyalar")
// @CrossOrigin(...) // Global ayar yeterli olmalı
public class VardiyaController {

    private static final Logger log = LoggerFactory.getLogger(VardiyaController.class);
    private final VardiyaService vardiyaService;

    public VardiyaController(VardiyaService vardiyaService) {
        this.vardiyaService = vardiyaService;
    }

    // Tüm vardiya tanımlarını listele (Personel vardiya ataması yapılırken kullanılabilir)
    @GetMapping
    @PreAuthorize("isAuthenticated()") // Veya "hasRole('ADMIN')" eğer sadece admin görebilecekse
    public ResponseEntity<List<VardiyaDTO>> getAllVardiyalar() {
        log.info("GET /api/vardiyalar çağrıldı");
        List<VardiyaDTO> vardiyalar = vardiyaService.getAllVardiyalar();
        return ResponseEntity.ok(vardiyalar);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Veya "hasRole('ADMIN')"
    public ResponseEntity<VardiyaDTO> getVardiyaById(@PathVariable("id") Integer id) {
        log.info("GET /api/vardiyalar/{} çağrıldı", id);
        return vardiyaService.getVardiyaById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    log.warn("Vardiya bulunamadı, ID: {}", id);
                    return new ResourceNotFoundException("Vardiya tanımı bulunamadı, ID: " + id);
                });
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Sadece ADMIN yeni vardiya tanımı oluşturabilir
    public ResponseEntity<?> createVardiya(@Valid @RequestBody VardiyaDTO vardiyaDTO) {
        log.info("POST /api/vardiyalar çağrıldı, DTO: {}", vardiyaDTO.getAd());
        if (vardiyaDTO.getId() != null) {
            log.warn("Yeni vardiya oluşturulurken ID belirtildi: {}", vardiyaDTO.getId());
            return ResponseEntity.badRequest().body("Yeni vardiya oluşturulurken ID belirtilmemelidir.");
        }
        try {
            VardiyaDTO yeniVardiya = vardiyaService.createVardiya(vardiyaDTO);
            log.info("Vardiya başarıyla oluşturuldu: {}", yeniVardiya.getAd());
            return new ResponseEntity<>(yeniVardiya, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Vardiya oluşturma hatası: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Sadece ADMIN vardiya tanımı güncelleyebilir
    public ResponseEntity<?> updateVardiya(@PathVariable("id") Integer id, @Valid @RequestBody VardiyaDTO vardiyaDTO) {
        log.info("PUT /api/vardiyalar/{} çağrıldı, DTO: {}", id, vardiyaDTO.getAd());
        try {
            VardiyaDTO guncellenmisVardiya = vardiyaService.updateVardiya(id, vardiyaDTO);
            log.info("Vardiya başarıyla güncellendi: {}", guncellenmisVardiya.getAd());
            return ResponseEntity.ok(guncellenmisVardiya);
        } catch (ResourceNotFoundException e) {
            log.warn("Vardiya güncelleme hatası - bulunamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Vardiya güncelleme hatası - geçersiz argüman: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Sadece ADMIN vardiya tanımı silebilir
    public ResponseEntity<?> deleteVardiya(@PathVariable("id") Integer id) { // ResponseEntity<?> olarak güncellendi
        log.info("DELETE /api/vardiyalar/{} çağrıldı", id);
        try {
            vardiyaService.deleteVardiya(id);
            log.info("Vardiya başarıyla silindi, ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.warn("Vardiya silme hatası - bulunamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // Mesaj döndürmek için
        } catch (IllegalStateException e) { // Serviste tanımladığımız "kullanımda olduğu için silinemez" durumu
            log.warn("Vardiya silme hatası - kullanılıyor: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}