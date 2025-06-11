package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.HastaKayitDTO;
import com.hastane.hastanebackend.entity.Hasta;
import com.hastane.hastanebackend.service.HastaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hastalar")
public class HastaController {

    private final HastaService hastaService;

    @Autowired
    public HastaController(HastaService hastaService) {
        this.hastaService = hastaService;
    }

    // Hasta Kayıt Endpoint'i
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerHasta(@RequestBody HastaKayitDTO hastaKayitDTO) {
        try {
            Hasta yeniHasta = hastaService.createHasta(hastaKayitDTO);
            // Başarılı kayıt sonrası sadece bir mesaj veya oluşturulan hastanın ID'si döndürülebilir.
            // Tam hasta nesnesini döndürmek hassas veri sızıntısına yol açabilir.
            // Şimdilik basit bir başarı mesajı veya sadece ID dönelim.
            // return new ResponseEntity<>(yeniHasta, HttpStatus.CREATED); // Ya da sadece ID
            return new ResponseEntity<>("Hasta başarıyla kaydedildi. ID: " + yeniHasta.getId(), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Daha detaylı hata loglaması servis katmanında yapılabilir.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Hasta kaydı sırasında beklenmedik bir hata oluştu: " + e.getMessage());
        }
    }

    // Tüm hastaları getirme (Yetkilendirme gerektirebilir)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Hasta>> getAllHastalar() {
        List<Hasta> hastalar = hastaService.getAllHastalar();
        return ResponseEntity.ok(hastalar);
    }

    // ID ile hasta getirme (Yetkilendirme gerektirebilir)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Hasta> getHastaById(@PathVariable Integer id) {
        return hastaService.getHastaById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // TC Kimlik No ile hasta getirme (Yetkilendirme gerektirebilir)
    @GetMapping(value = "/tc/{tcKimlikNo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Hasta> getHastaByTcKimlikNo(@PathVariable String tcKimlikNo) {
        return hastaService.getHastaByTcKimlikNo(tcKimlikNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}