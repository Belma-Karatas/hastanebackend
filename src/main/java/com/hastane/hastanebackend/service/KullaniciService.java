package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.entity.Kullanici;
import java.util.List;

public interface KullaniciService {
    // Şimdilik sadece tüm kullanıcıları getiren bir metot ekleyelim.
    // Detayları (kayıt, giriş vb.) daha sonra ekleyeceğiz.
    List<Kullanici> getAllKullanicilar();
}