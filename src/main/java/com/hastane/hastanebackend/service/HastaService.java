package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.HastaKayitDTO;
import com.hastane.hastanebackend.entity.Hasta;

import java.util.List;
import java.util.Optional;

public interface HastaService {

    Hasta createHasta(HastaKayitDTO hastaKayitDTO);

    Optional<Hasta> getHastaById(Integer id);

    List<Hasta> getAllHastalar();

    Optional<Hasta> getHastaByTcKimlikNo(String tcKimlikNo);

    // Güncelleme ve silme metotları daha sonra eklenebilir
    // Hasta updateHasta(Integer id, HastaKayitDTO hastaKayitDTO);
    // void deleteHasta(Integer id);
}