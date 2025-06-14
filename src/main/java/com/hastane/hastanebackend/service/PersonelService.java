package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.PersonelDTO;
import com.hastane.hastanebackend.entity.Personel;

import java.util.List;
import java.util.Optional;

public interface PersonelService {

    List<Personel> getAllPersoneller();

    Optional<Personel> getPersonelById(Integer id);

    Personel createPersonel(PersonelDTO personelDTO);

    // Bu yeni metot, DTO kullanarak personel güncellemesi yapacak.
    // Eğer eski updatePersonel(Integer id, Personel personelDetails) metodu varsa
    // onu kaldırabilir veya yorum satırı yapabilirsin, çünkü DTO ile çalışmak daha esnek.
    Personel updatePersonelWithDTO(Integer id, PersonelDTO personelDTO);

    void deletePersonel(Integer id);
    
    List<Personel> getPersonellerByRol(String rolAdi);
}