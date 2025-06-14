package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.YatakDTO;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.service.YatakService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/yataklar")
// @CrossOrigin(origins = "http://localhost:5173") // Global ayar SecurityConfig'de yapıldıysa genellikle gerekmez
public class YatakController {

    private static final Logger log = LoggerFactory.getLogger(YatakController.class);
    private final YatakService yatakService;

    public YatakController(YatakService yatakService) {
        this.yatakService = yatakService;
    }

    // Tüm yatakları veya belirli bir odaya ait yatakları listele
    @GetMapping
    @PreAuthorize("isAuthenticated()") // Tüm giriş yapmış kullanıcılar yatakları listeleyebilir
    public ResponseEntity<List<YatakDTO>> getAllYataklar(@RequestParam(required = false) Integer odaId) {
        log.info("GET /api/yataklar çağrıldı. Oda ID: {}", odaId);
        List<YatakDTO> yataklar;
        if (odaId != null) {
            yataklar = yatakService.getYataklarByOdaId(odaId);
        } else {
            yataklar = yatakService.getAllYataklar();
        }
        return ResponseEntity.ok(yataklar);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<YatakDTO> getYatakById(@PathVariable("id") Integer id) {
        log.info("GET /api/yataklar/{} çağrıldı", id);
        return yatakService.getYatakById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    log.warn("Yatak bulunamadı, ID: {}", id);
                    return new ResourceNotFoundException("Yatak bulunamadı, ID: " + id);
                });
    }

    // Boş yatakları listele (genel veya oda bazlı)
    @GetMapping("/bos")
    @PreAuthorize("isAuthenticated()") // Genellikle doktor, hemşire, kabul görevlisi gibi roller erişir
    public ResponseEntity<List<YatakDTO>> getBosYataklar(@RequestParam(required = false) Integer odaId) {
        log.info("GET /api/yataklar/bos çağrıldı. Oda ID: {}", odaId);
        List<YatakDTO> bosYataklar;
        if (odaId != null) {
            bosYataklar = yatakService.getBosYataklarByOdaId(odaId);
        } else {
            bosYataklar = yatakService.getTumBosYataklar();
        }
        return ResponseEntity.ok(bosYataklar);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Sadece ADMIN yeni yatak oluşturabilir
    public ResponseEntity<?> createYatak(@Valid @RequestBody YatakDTO yatakDTO) {
        log.info("POST /api/yataklar çağrıldı. Yatak No: {}, Oda ID: {}", yatakDTO.getYatakNumarasi(), yatakDTO.getOdaId());
        if (yatakDTO.getId() != null) {
            log.warn("Yeni yatak oluşturulurken ID belirtildi: {}", yatakDTO.getId());
            return ResponseEntity.badRequest().body("Yeni yatak oluşturulurken ID belirtilmemelidir.");
        }
        try {
            YatakDTO yeniYatak = yatakService.createYatak(yatakDTO);
            log.info("Yatak başarıyla oluşturuldu. ID: {}, Numara: {}", yeniYatak.getId(), yeniYatak.getYatakNumarasi());
            return new ResponseEntity<>(yeniYatak, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            log.warn("Yatak oluşturma hatası - kaynak bulunamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Yatak oluşturma hatası - geçersiz argüman: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Sadece ADMIN yatak güncelleyebilir (temel bilgiler)
    public ResponseEntity<?> updateYatak(@PathVariable("id") Integer id, @Valid @RequestBody YatakDTO yatakDTO) {
        log.info("PUT /api/yataklar/{} çağrıldı. Yatak No: {}, Oda ID: {}", id, yatakDTO.getYatakNumarasi(), yatakDTO.getOdaId());
        try {
            YatakDTO guncellenmisYatak = yatakService.updateYatak(id, yatakDTO);
            log.info("Yatak başarıyla güncellendi. ID: {}, Numara: {}", guncellenmisYatak.getId(), guncellenmisYatak.getYatakNumarasi());
            return ResponseEntity.ok(guncellenmisYatak);
        } catch (ResourceNotFoundException e) {
            log.warn("Yatak güncelleme hatası - kaynak bulunamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) { // IllegalStateException'ı da yakala
            log.warn("Yatak güncelleme hatası - geçersiz argüman/durum: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Yatağın doluluk durumunu güncellemek için ayrı bir endpoint
    // Bu işlem genellikle ADMIN veya Yatis işlemini yapan bir servis (örn: HEMSIRE, DOKTOR) tarafından tetiklenir.
    @PutMapping("/{id}/doluluk")
    @PreAuthorize("hasAnyRole('ADMIN', 'HEMSIRE', 'DOKTOR')") // Rolleri ihtiyaca göre düzenle
    public ResponseEntity<?> updateYatakDolulukDurumu(@PathVariable("id") Integer id, @RequestParam boolean doluMu) {
        log.info("PUT /api/yataklar/{}/doluluk çağrıldı. Yeni durum: {}", id, doluMu);
        try {
            YatakDTO guncellenmisYatak = yatakService.updateYatakDolulukDurumu(id, doluMu);
            return ResponseEntity.ok(guncellenmisYatak);
        } catch (ResourceNotFoundException e) {
            log.warn("Yatak doluluk güncelleme hatası - bulunamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Yatak doluluk güncelleme hatası - geçersiz durum: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

        @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteYatak(@PathVariable("id") Integer id) { // Dönüş tipi ResponseEntity<?> olarak değişti
        log.info("DELETE /api/yataklar/{} çağrıldı", id);
        try {
            yatakService.deleteYatak(id);
            log.info("Yatak başarıyla silindi. ID: {}", id);
            return ResponseEntity.noContent().build(); // Başarılı silmede ResponseEntity<Void>
        } catch (ResourceNotFoundException e) {
            log.warn("Yatak silme hatası - bulunamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // ResponseEntity<String>
        } catch (IllegalStateException e) { // Dolu yatak silme hatası
            log.warn("Yatak silme hatası - geçersiz durum: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage()); // ResponseEntity<String>
     }
    } 

} 