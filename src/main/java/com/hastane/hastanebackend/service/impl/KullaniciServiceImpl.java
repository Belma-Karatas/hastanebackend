package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.service.KullaniciService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class KullaniciServiceImpl implements KullaniciService {

    private final KullaniciRepository kullaniciRepository;

    public KullaniciServiceImpl(KullaniciRepository kullaniciRepository) {
        this.kullaniciRepository = kullaniciRepository;
    }

    @Override
    public List<Kullanici> getAllKullanicilar() {
        return kullaniciRepository.findAll();
    }
}