package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.PersonelDTO;
import com.hastane.hastanebackend.entity.Personel;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.service.PersonelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/personeller")
public class PersonelController {

    private final PersonelService personelService;

    public PersonelController(PersonelService personelService) {
        this.personelService = personelService;
    }

    @PostMapping
    public ResponseEntity<?> createPersonel(@RequestBody PersonelDTO personelDTO) {
        try {
            Personel yeniPersonel = personelService.createPersonel(personelDTO);
            return new ResponseEntity<>(yeniPersonel, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Personel oluşturulurken beklenmedik bir hata oluştu.");
        }
    }

    @GetMapping
    public ResponseEntity<List<Personel>> getAllPersoneller() {
        return ResponseEntity.ok(personelService.getAllPersoneller());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Personel> getPersonelById(@PathVariable Integer id) {
        return personelService.getPersonelById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rol/{rolAdi}")
    public ResponseEntity<List<Personel>> getPersonellerByRol(@PathVariable String rolAdi) {
        return ResponseEntity.ok(personelService.getPersonellerByRol(rolAdi));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePersonel(@PathVariable Integer id) {
        try {
            personelService.deletePersonel(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}