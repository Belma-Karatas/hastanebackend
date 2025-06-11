package com.hastane.hastanebackend.security;

import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import org.slf4j.Logger; // SLF4J Logger importu
import org.slf4j.LoggerFactory; // SLF4J LoggerFactory importu
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class); // Logger tanımlaması
    private final KullaniciRepository kullaniciRepository;

    @Autowired
    public UserDetailsServiceImpl(KullaniciRepository kullaniciRepository) {
        this.kullaniciRepository = kullaniciRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("UserDetailsServiceImpl: loadUserByUsername çağrıldı. Aranan email: {}", email); // Gelen email'i logla

        Kullanici kullanici = kullaniciRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("UserDetailsServiceImpl: Kullanıcı bulunamadı. Email: {}", email); // Kullanıcı bulunamazsa logla
                    return new UsernameNotFoundException("Bu email ile kullanıcı bulunamadı: " + email);
                });

        // Kullanıcı bulunduysa, bilgilerini loglayalım
        logger.info("UserDetailsServiceImpl: Kullanıcı bulundu. ID: {}, Email: {}", kullanici.getId(), kullanici.getEmail());
        // Güvenlik için tam şifreyi loglamayın, sadece var olduğunu teyit edin veya ilk birkaç karakterini.
        // logger.info("UserDetailsServiceImpl: Kullanıcının veritabanındaki hash'lenmiş şifresi (ilk 10 karakter): {}", kullanici.getSifre().substring(0, Math.min(kullanici.getSifre().length(), 10)) + "...");

        Collection<? extends GrantedAuthority> authorities = kullanici.getRoller().stream()
                .map(rol -> {
                    logger.info("UserDetailsServiceImpl: Kullanıcıya '{}' rolü atanıyor.", rol.getAd()); // Atanan rolleri logla
                    return new SimpleGrantedAuthority(rol.getAd());
                })
                .collect(Collectors.toSet());

        // Spring Security'nin User nesnesini oluşturup dönüyoruz
        // Bu User nesnesindeki şifre, veritabanındaki HASH'LENMİŞ şifre olmalı.
        // AuthenticationManager, kullanıcının girdiği şifreyi encode edip bu hash ile karşılaştırır.
        return new User(
                kullanici.getEmail(),
                kullanici.getSifre(), // Veritabanındaki hash'lenmiş şifre
                kullanici.isAktifMi(),
                true,                         // accountNonExpired
                true,                         // credentialsNonExpired
                true,                         // accountNonLocked
                authorities
        );
    }
}