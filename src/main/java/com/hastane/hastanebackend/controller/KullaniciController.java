package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.service.KullaniciService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/kullanicilar")
public class KullaniciController {

    private final KullaniciService kullaniciService;

    public KullaniciController(KullaniciService kullaniciService) {
        this.kullaniciService = kullaniciService;
    }

    @GetMapping
    public ResponseEntity<List<Kullanici>> getAllKullanicilar() {
        List<Kullanici> kullanicilar = kullaniciService.getAllKullanicilar();
        return ResponseEntity.ok(kullanicilar);
    }
}