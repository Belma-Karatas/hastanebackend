package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.IzinTuruDTO;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.service.IzinTuruService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/izinturleri")
// @CrossOrigin(...) // Global ayar yeterli olmalı
public class IzinTuruController {

    private static final Logger log = LoggerFactory.getLogger(IzinTuruController.class);
    private final IzinTuruService izinTuruService;

    public IzinTuruController(IzinTuruService izinTuruService) {
        this.izinTuruService = izinTuruService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()") // Tüm personeller izin türlerini görebilmeli (izin talep ederken seçmek için)
    public ResponseEntity<List<IzinTuruDTO>> getAllIzinTurleri() {
        log.info("GET /api/izinturleri çağrıldı");
        List<IzinTuruDTO> izinTurleri = izinTuruService.getAllIzinTurleri();
        return ResponseEntity.ok(izinTurleri);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Belki sadece ADMIN veya YONETICI
    public ResponseEntity<IzinTuruDTO> getIzinTuruById(@PathVariable("id") Integer id) {
        log.info("GET /api/izinturleri/{} çağrıldı", id);
        return izinTuruService.getIzinTuruById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    log.warn("İzin Türü bulunamadı, ID: {}", id);
                    return new ResourceNotFoundException("İzin Türü bulunamadı, ID: " + id);
                });
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Sadece ADMIN yeni izin türü oluşturabilir
    public ResponseEntity<?> createIzinTuru(@Valid @RequestBody IzinTuruDTO izinTuruDTO) {
        log.info("POST /api/izinturleri çağrıldı, DTO: {}", izinTuruDTO.getAd());
        if (izinTuruDTO.getId() != null) {
            log.warn("Yeni izin türü oluşturulurken ID belirtildi: {}", izinTuruDTO.getId());
            return ResponseEntity.badRequest().body("Yeni izin türü oluşturulurken ID belirtilmemelidir.");
        }
        try {
            IzinTuruDTO yeniIzinTuru = izinTuruService.createIzinTuru(izinTuruDTO);
            log.info("İzin türü başarıyla oluşturuldu: {}", yeniIzinTuru.getAd());
            return new ResponseEntity<>(yeniIzinTuru, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("İzin türü oluşturma hatası: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Sadece ADMIN izin türü güncelleyebilir
    public ResponseEntity<?> updateIzinTuru(@PathVariable("id") Integer id, @Valid @RequestBody IzinTuruDTO izinTuruDTO) {
        log.info("PUT /api/izinturleri/{} çağrıldı, DTO: {}", id, izinTuruDTO.getAd());
        try {
            IzinTuruDTO guncellenmisIzinTuru = izinTuruService.updateIzinTuru(id, izinTuruDTO);
            log.info("İzin türü başarıyla güncellendi: {}", guncellenmisIzinTuru.getAd());
            return ResponseEntity.ok(guncellenmisIzinTuru);
        } catch (ResourceNotFoundException e) {
            log.warn("İzin türü güncelleme hatası - bulunamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("İzin türü güncelleme hatası - geçersiz argüman: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Sadece ADMIN izin türü silebilir
    public ResponseEntity<?> deleteIzinTuru(@PathVariable("id") Integer id) { // Dönüş tipi ResponseEntity<?> olarak değişti
        log.info("DELETE /api/izinturleri/{} çağrıldı", id);
        try {
            izinTuruService.deleteIzinTuru(id);
            log.info("İzin türü başarıyla silindi, ID: {}", id);
            return ResponseEntity.noContent().build(); // Başarılı silmede ResponseEntity<Void>
        } catch (ResourceNotFoundException e) {
            log.warn("İzin türü silme hatası - bulunamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // ResponseEntity<String>
        } catch (IllegalStateException e) { // Serviste tanımladığımız "kullanımda olduğu için silinemez" durumu
            log.warn("İzin türü silme hatası - kullanılıyor: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage()); // ResponseEntity<String>
        }
    }
}