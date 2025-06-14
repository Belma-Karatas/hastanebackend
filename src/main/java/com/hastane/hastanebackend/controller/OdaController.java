package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.OdaDTO;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.service.OdaService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/odalar")
// @CrossOrigin(origins = "http://localhost:5173") // Global ayar SecurityConfig'de yapıldıysa genellikle gerekmez
public class OdaController {

    private static final Logger log = LoggerFactory.getLogger(OdaController.class);
    private final OdaService odaService;

    public OdaController(OdaService odaService) {
        this.odaService = odaService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()") // Tüm giriş yapmış kullanıcılar odaları listeleyebilir
    public ResponseEntity<List<OdaDTO>> getAllOdalar(@RequestParam(required = false) Integer katId) {
        log.info("GET /api/odalar çağrıldı. Kat ID: {}", katId);
        List<OdaDTO> odalar;
        if (katId != null) {
            odalar = odaService.getOdalarByKatId(katId);
        } else {
            odalar = odaService.getAllOdalar();
        }
        return ResponseEntity.ok(odalar);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OdaDTO> getOdaById(@PathVariable("id") Integer id) {
        log.info("GET /api/odalar/{} çağrıldı", id);
        return odaService.getOdaById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    log.warn("Oda bulunamadı, ID: {}", id);
                    return new ResourceNotFoundException("Oda bulunamadı, ID: " + id);
                });
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Sadece ADMIN yeni oda oluşturabilir
    public ResponseEntity<?> createOda(@Valid @RequestBody OdaDTO odaDTO) {
        log.info("POST /api/odalar çağrıldı. Oda Numarası: {}, Kat ID: {}", odaDTO.getOdaNumarasi(), odaDTO.getKatId());
        if (odaDTO.getId() != null) {
            log.warn("Yeni oda oluşturulurken ID belirtildi: {}", odaDTO.getId());
            return ResponseEntity.badRequest().body("Yeni oda oluşturulurken ID belirtilmemelidir.");
        }
        try {
            OdaDTO yeniOda = odaService.createOda(odaDTO);
            log.info("Oda başarıyla oluşturuldu. ID: {}, Numara: {}", yeniOda.getId(), yeniOda.getOdaNumarasi());
            return new ResponseEntity<>(yeniOda, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            log.warn("Oda oluşturma hatası - kaynak bulunamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Oda oluşturma hatası - geçersiz argüman: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Sadece ADMIN oda güncelleyebilir
    public ResponseEntity<?> updateOda(@PathVariable("id") Integer id, @Valid @RequestBody OdaDTO odaDTO) {
        log.info("PUT /api/odalar/{} çağrıldı. Oda Numarası: {}, Kat ID: {}", id, odaDTO.getOdaNumarasi(), odaDTO.getKatId());
        try {
            OdaDTO guncellenmisOda = odaService.updateOda(id, odaDTO);
            log.info("Oda başarıyla güncellendi. ID: {}, Numara: {}", guncellenmisOda.getId(), guncellenmisOda.getOdaNumarasi());
            return ResponseEntity.ok(guncellenmisOda);
        } catch (ResourceNotFoundException e) {
            log.warn("Oda güncelleme hatası - kaynak bulunamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Oda güncelleme hatası - geçersiz argüman: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Sadece ADMIN oda silebilir
    public ResponseEntity<Void> deleteOda(@PathVariable("id") Integer id) {
        log.info("DELETE /api/odalar/{} çağrıldı", id);
        try {
            odaService.deleteOda(id);
            log.info("Oda başarıyla silindi. ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.warn("Oda silme hatası - bulunamadı: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
       
    }
}