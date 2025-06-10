package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.Kullanici;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KullaniciRepository extends JpaRepository<Kullanici, Integer> {

    // Kullanıcıyı email adresine göre bulmak için. Giriş (login) işleminde bu çok kritik olacak.
    Optional<Kullanici> findByEmail(String email);

    // Bir email adresinin veritabanında zaten kayıtlı olup olmadığını
    // daha verimli bir şekilde kontrol etmek için. Yeni kullanıcı kaydederken kullanacağız.
    Boolean existsByEmail(String email);
}