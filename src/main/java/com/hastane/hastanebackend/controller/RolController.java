package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.entity.Rol;
import com.hastane.hastanebackend.service.RolService;
import org.springframework.http.HttpStatus; // HTTP_STATUS EKLE
import org.springframework.http.MediaType; // MEDIATYPE EKLE
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roller")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    // @PostMapping ANNOTASYONUNU GÜNCELLEYELİM
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Rol> createRol(@RequestBody Rol rol) {
        try {
            Rol yeniRol = rolService.createRol(rol);
            // Başarılı durumda 201 Created dönmek daha standarttır.
            return new ResponseEntity<>(yeniRol, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Bu hatayı Controller seviyesinde yakalamak daha iyi olabilir,
            // ama şimdilik bu şekilde bırakalım.
            // Daha sonra merkezi bir hata yönetimi (GlobalExceptionHandler) ekleyebiliriz.
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Rol>> getAllRoller() {
        List<Rol> roller = rolService.getAllRoller();
        return ResponseEntity.ok(roller);
    }
}