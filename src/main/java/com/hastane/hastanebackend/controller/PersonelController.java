package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.PersonelDTO;
import com.hastane.hastanebackend.entity.Personel;
import com.hastane.hastanebackend.entity.Rol;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.service.PersonelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/personeller")
public class PersonelController {

    private static final Logger logger = LoggerFactory.getLogger(PersonelController.class);
    private final PersonelService personelService;

    public PersonelController(PersonelService personelService) {
        this.personelService = personelService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPersonel(@RequestBody PersonelDTO personelDTO) {
        logger.info("POST /api/personeller çağrıldı. DTO Email: {}", personelDTO.getEmail());
        try {
            Personel yeniPersonel = personelService.createPersonel(personelDTO);
            return new ResponseEntity<>(convertToPersonelDTO(yeniPersonel), HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            logger.warn("Personel oluşturma hatası (kaynak bulunamadı): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Personel oluşturma hatası (geçersiz argüman): {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Personel oluşturulurken beklenmedik bir hata oluştu:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Personel oluşturulurken beklenmedik bir hata oluştu.");
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HASTA', 'DOKTOR')")
    public ResponseEntity<List<PersonelDTO>> getAllPersoneller() {
        logger.info("GET /api/personeller çağrıldı.");
        List<Personel> personeller = personelService.getAllPersoneller();
        List<PersonelDTO> personelDTOlari = personeller.stream()
            .map(this::convertToPersonelDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(personelDTOlari);
    }

    @GetMapping("/doktorlar")
    @PreAuthorize("hasAnyRole('HASTA', 'ADMIN', 'DOKTOR')")
    public ResponseEntity<List<PersonelDTO>> getDoktorlar(
            @RequestParam(required = false) Integer bransId) {
        logger.info("GET /api/personeller/doktorlar çağrıldı. Brans ID: {}", bransId);
        List<Personel> doktorlar;
        if (bransId != null && bransId > 0) { // bransId 0 ise tüm doktorları getirebiliriz veya hata verebiliriz
            doktorlar = personelService.getDoktorlarByBrans(bransId);
        } else {
            doktorlar = personelService.getPersonellerByRol("ROLE_DOKTOR");
        }
        List<PersonelDTO> doktorDTOlari = doktorlar.stream()
            .map(this::convertToPersonelDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(doktorDTOlari);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PersonelDTO> getPersonelById(@PathVariable Integer id) {
        logger.debug("GET /api/personeller/{} çağrıldı.", id);
        return personelService.getPersonelById(id)
                .map(this::convertToPersonelDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePersonel(@PathVariable Integer id, @RequestBody PersonelDTO personelDTO) {
        logger.info("PUT /api/personeller/{} çağrıldı. DTO Email: {}", id, personelDTO.getEmail());
        try {
            Personel guncellenmisPersonel = personelService.updatePersonelWithDTO(id, personelDTO);
            return ResponseEntity.ok(convertToPersonelDTO(guncellenmisPersonel));
        } catch (ResourceNotFoundException e) {
            logger.warn("Personel güncelleme hatası (kaynak bulunamadı), ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Personel güncelleme hatası (geçersiz argüman), ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Personel (ID: {}) güncellenirken beklenmedik bir hata oluştu:", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Personel güncellenirken beklenmedik bir hata oluştu.");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePersonel(@PathVariable Integer id) {
        logger.info("DELETE /api/personeller/{} çağrıldı.", id);
        try {
            personelService.deletePersonel(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Silinecek personel bulunamadı, ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Personel (ID: {}) silinirken beklenmedik bir hata oluştu:", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private PersonelDTO convertToPersonelDTO(Personel personel) {
        if (personel == null) return null;
        PersonelDTO dto = new PersonelDTO();
        dto.setId(personel.getId());
        dto.setAd(personel.getAd());
        dto.setSoyad(personel.getSoyad());
        dto.setTelefon(personel.getTelefon());

        if (personel.getDepartman() != null) {
            dto.setDepartmanId(personel.getDepartman().getId());
            dto.setDepartmanAdi(personel.getDepartman().getAd()); // DEPARTMAN ADI EKLENDİ
        } else {
            dto.setDepartmanAdi("-"); // Veya null bırakabilirsiniz, frontend'de kontrol edilir
        }

        if (personel.getKullanici() != null) {
            dto.setEmail(personel.getKullanici().getEmail());
            if (personel.getKullanici().getRoller() != null) {
                dto.setRoller(personel.getKullanici().getRoller().stream()
                    .map(rol -> rol.getAd().replace("ROLE_", "")) // "ROLE_" ön ekini kaldır
                    .collect(Collectors.toSet()));
            }
        }

        if (personel.getDoktorDetay() != null && personel.getDoktorDetay().getBrans() != null) {
            dto.setBransId(personel.getDoktorDetay().getBrans().getId());
            dto.setBransAdi(personel.getDoktorDetay().getBrans().getAd()); // BRANŞ ADI EKLENDİ
        } else if (dto.getRoller() != null && dto.getRoller().contains("DOKTOR")) { // Eğer rol doktor ama detay yoksa
            dto.setBransAdi("Branş Atanmamış");
        } else {
            dto.setBransAdi("-"); // Veya null bırakabilirsiniz
        }
        
        // Şifre DTO'ya eklenmez.
        return dto;
    }
}