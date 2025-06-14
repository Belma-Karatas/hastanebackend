package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.entity.Ilac;
import com.hastane.hastanebackend.service.IlacService;
import com.hastane.hastanebackend.exception.ResourceNotFoundException; // Kendi exception'ımız

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Yetkilendirme için
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ilaclar") // Bu controller'ın temel URL'i
// @CrossOrigin(origins = "http://localhost:5173") // Gerekirse, SecurityConfig'deki global ayar yeterli değilse
public class IlacController {

    private final IlacService ilacService;

    public IlacController(IlacService ilacService) {
        this.ilacService = ilacService;
    }

    // Tüm ilaçları listele
    // Herkesin erişebilmesi için @PreAuthorize eklenmedi veya @PreAuthorize("permitAll()") eklenebilir.
    // Ya da sadece yetkili kullanıcılar (örn: DOKTOR, ADMIN)
    @GetMapping
    @PreAuthorize("isAuthenticated()") // Sadece giriş yapmış kullanıcılar görebilir
    public ResponseEntity<List<Ilac>> getAllIlaclar(
            @RequestParam(required = false) String search) { // Arama parametresi
        List<Ilac> ilaclar;
        if (search != null && !search.trim().isEmpty()) {
            ilaclar = ilacService.searchIlacByAdKeyword(search);
        } else {
            ilaclar = ilacService.getAllIlaclar();
        }
        return ResponseEntity.ok(ilaclar);
    }

    // ID ile ilaç getir
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Sadece giriş yapmış kullanıcılar
    public ResponseEntity<Ilac> getIlacById(@PathVariable("id") Integer id) {
        Ilac ilac = ilacService.getIlacById(id)
                .orElseThrow(() -> new ResourceNotFoundException("İlaç bulunamadı, ID: " + id));
        return ResponseEntity.ok(ilac);
    }

    // Yeni ilaç oluştur
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Sadece ADMIN rolüne sahip kullanıcılar ilaç ekleyebilir
    public ResponseEntity<?> createIlac(@RequestBody Ilac ilac) {
        // Gelen 'ilac' nesnesinin validasyonları (örn: @Valid) eklenebilir.
        // Şimdilik DTO kullanmıyoruz, direkt entity alıyoruz.
        // ID alanı client tarafından gönderilmemeli, gönderilirse de null olmalı.
        if (ilac.getId() != null) {
            return ResponseEntity.badRequest().body("Yeni ilaç oluşturulurken ID belirtilmemelidir.");
        }
        try {
            Ilac yeniIlac = ilacService.createIlac(ilac);
            return new ResponseEntity<>(yeniIlac, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Servis katmanında fırlatılan "ilaç adı zaten mevcut" hatası
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // İlaç güncelle
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Sadece ADMIN rolüne sahip kullanıcılar ilaç güncelleyebilir
    public ResponseEntity<?> updateIlac(@PathVariable("id") Integer id, @RequestBody Ilac ilacDetails) {
        // Gelen 'ilacDetails' nesnesinin validasyonları eklenebilir.
        try {
            Ilac guncellenmisIlac = ilacService.updateIlac(id, ilacDetails);
            return ResponseEntity.ok(guncellenmisIlac);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Servis katmanında fırlatılan "güncellenmek istenen ilaç adı zaten kullanılıyor" hatası
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // İlaç sil
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Sadece ADMIN rolüne sahip kullanıcılar ilaç silebilir
    public ResponseEntity<Void> deleteIlac(@PathVariable("id") Integer id) {
        try {
            ilacService.deleteIlac(id);
            return ResponseEntity.noContent().build(); // Başarılı silme işleminde 204 No Content
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Veya e.getMessage() ile mesaj dönebiliriz
        }
        // DataIntegrityViolationException gibi durumlar için global exception handler daha iyi olabilir.
    }
}