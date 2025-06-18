package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.PersonelDTO;
import com.hastane.hastanebackend.entity.Personel;

import java.util.List;
import java.util.Optional;

public interface PersonelService {

    List<Personel> getAllPersoneller();
    Optional<Personel> getPersonelById(Integer id);
    Personel createPersonel(PersonelDTO personelDTO);
    Personel updatePersonelWithDTO(Integer id, PersonelDTO personelDTO);
    void deletePersonel(Integer id);
    List<Personel> getPersonellerByRol(String rolAdi);
    List<Personel> getDoktorlarByBrans(Integer bransId); // BU SATIRI EKLEYİN VEYA KONTROL EDİN
}