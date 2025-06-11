package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.entity.Rol;
import com.hastane.hastanebackend.service.RolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roller")
public class RolController {

    private final RolService rolService;
    private static final Logger logger = LoggerFactory.getLogger(RolController.class);

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @PostMapping // consumes hala yok
    public ResponseEntity<?> createRol(@RequestBody Rol rol) { // Parametre tekrar Rol oldu
        logger.info("Dönüştürülmüş Rol nesnesi: {}", rol); // Dönüşüm başarılıysa loglayalım
        if (rol == null || rol.getAd() == null) { // Basit bir null kontrolü
             logger.warn("Gelen Rol nesnesi veya 'ad' alanı null.");
             // Aslında burada 400 Bad Request dönmek daha uygun olurdu ama şimdilik log yeterli.
        }
        try {
            Rol yeniRol = rolService.createRol(rol);
            return new ResponseEntity<>(yeniRol, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) { // RolService'den gelen "zaten mevcut" hatası
            logger.warn("Rol oluşturma hatası (IllegalArgumentException): {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage()); // Hata mesajını gövdede döndür
        } catch (Exception e) {
            logger.error("createRol metodunda beklenmedik hata: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Rol oluşturulurken sunucu hatası.");
        }
    }

    @GetMapping
    public ResponseEntity<List<Rol>> getAllRoller() {
        List<Rol> roller = rolService.getAllRoller();
        return ResponseEntity.ok(roller);
    }
}