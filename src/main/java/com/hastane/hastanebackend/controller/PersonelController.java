package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.PersonelDTO;
import com.hastane.hastanebackend.entity.Personel;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.service.PersonelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // BU IMPORTU EKLE
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personeller")
public class PersonelController {

    private final PersonelService personelService;

    public PersonelController(PersonelService personelService) {
        this.personelService = personelService;
    }

    // YENİ PERSONEL OLUŞTURMA
    // SecurityConfig'de HttpMethod.POST, "/api/personeller" için permitAll() demiştik.
    // Bu, özellikle ilk admin/kullanıcı kaydı gibi senaryolar için veya
    // bazı sistemlerde personelin kendi kendini (kısıtlı bilgilerle) kaydedebilmesi için bırakılabilir.
    // Eğer tüm personel oluşturma işlemlerinin sadece ADMIN tarafından yapılması isteniyorsa,
    // SecurityConfig'deki o izni kaldırıp aşağıdaki @PreAuthorize aktif edilmelidir.
    // Şimdilik, @PreAuthorize('hasRole("ADMIN")') ekleyelim, SecurityConfig'deki permitAll'u daha sonra
    // ihtiyaca göre kaldırabilir veya daha spesifik hale getirebiliriz.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPersonel(@RequestBody PersonelDTO personelDTO) {
        try {
            Personel yeniPersonel = personelService.createPersonel(personelDTO);
            return new ResponseEntity<>(yeniPersonel, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Genel hatalar için loglama da eklenebilir.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Personel oluşturulurken beklenmedik bir hata oluştu: " + e.getMessage());
        }
    }

    // TÜM PERSONELLERİ GETİR (Sadece Admin)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Personel>> getAllPersoneller() {
        List<Personel> personeller = personelService.getAllPersoneller();
        return ResponseEntity.ok(personeller);
    }

    // ID İLE PERSONEL GETİR (Sadece Admin)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Personel> getPersonelById(@PathVariable Integer id) {
        return personelService.getPersonelById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PERSONEL GÜNCELLEME (Sadece Admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePersonel(@PathVariable Integer id, @RequestBody PersonelDTO personelDTO) {
        try {
            // PersonelService'deki update metodunun PersonelDTO alacak şekilde olması gerekiyor.
            // Bu metodu PersonelService ve implementasyonunda oluşturacağız/güncelleyeceğiz.
            Personel guncellenmisPersonel = personelService.updatePersonelWithDTO(id, personelDTO);
            return ResponseEntity.ok(guncellenmisPersonel);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Personel güncellenirken beklenmedik bir hata oluştu: " + e.getMessage());
        }
    }

    // PERSONEL SİLME (Sadece Admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePersonel(@PathVariable Integer id) {
        try {
            personelService.deletePersonel(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            // İsteğe bağlı: Silinmek istenen kaynak bulunamadığında daha detaylı bir mesaj loglanabilir.
            return ResponseEntity.notFound().build();
        }
    }

    // ROL ADINA GÖRE PERSONEL GETİRME (Sadece Admin)
    @GetMapping("/rol/{rolAdi}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Personel>> getPersonellerByRol(@PathVariable String rolAdi) {
        List<Personel> personeller = personelService.getPersonellerByRol(rolAdi);
        return ResponseEntity.ok(personeller);
    }
}