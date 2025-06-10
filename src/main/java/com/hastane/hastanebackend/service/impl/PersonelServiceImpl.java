package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.PersonelDTO;
import com.hastane.hastanebackend.entity.Departman;
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.entity.Personel;
import com.hastane.hastanebackend.entity.Rol;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.DepartmanRepository;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.repository.PersonelRepository;
import com.hastane.hastanebackend.repository.RolRepository;
import com.hastane.hastanebackend.service.PersonelService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PersonelServiceImpl implements PersonelService {

    private final PersonelRepository personelRepository;
    private final KullaniciRepository kullaniciRepository;
    private final RolRepository rolRepository;
    private final DepartmanRepository departmanRepository;
    private final PasswordEncoder passwordEncoder;

    public PersonelServiceImpl(PersonelRepository personelRepository,
                               KullaniciRepository kullaniciRepository,
                               RolRepository rolRepository,
                               DepartmanRepository departmanRepository,
                               PasswordEncoder passwordEncoder) {
        this.personelRepository = personelRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.rolRepository = rolRepository;
        this.departmanRepository = departmanRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Personel> getAllPersoneller() {
        return personelRepository.findAll();
    }

    @Override
    public Optional<Personel> getPersonelById(Integer id) {
        return personelRepository.findById(id);
    }

    @Override
    @Transactional
    public Personel createPersonel(PersonelDTO dto) {
        if (kullaniciRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Bu e-posta adresi zaten kullanılıyor: " + dto.getEmail());
        }

        Kullanici yeniKullanici = new Kullanici();
        yeniKullanici.setEmail(dto.getEmail());
        yeniKullanici.setSifre(passwordEncoder.encode(dto.getSifre()));
        yeniKullanici.setAktifMi(true);

        Set<Rol> roller = dto.getRoller().stream()
                .map(rolAdi -> rolRepository.findByAd(rolAdi)
                        .orElseThrow(() -> new ResourceNotFoundException("Rol bulunamadı: " + rolAdi)))
                .collect(Collectors.toSet());
        yeniKullanici.setRoller(roller);

        Departman departman = departmanRepository.findById(dto.getDepartmanId())
                .orElseThrow(() -> new ResourceNotFoundException("Departman bulunamadı, id: " + dto.getDepartmanId()));

        Personel yeniPersonel = new Personel();
        yeniPersonel.setAd(dto.getAd());
        yeniPersonel.setSoyad(dto.getSoyad());
        yeniPersonel.setTelefon(dto.getTelefon());
        yeniPersonel.setDepartman(departman);
        yeniPersonel.setKullanici(yeniKullanici);
        
        return personelRepository.save(yeniPersonel);
    }

    @Override
    @Transactional
    public Personel updatePersonel(Integer id, Personel personelDetails) {
        Personel personel = personelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personel bulunamadı, id: " + id));
        
        personel.setAd(personelDetails.getAd());
        personel.setSoyad(personelDetails.getSoyad());
        personel.setTelefon(personelDetails.getTelefon());

        return personelRepository.save(personel);
    }

    @Override
    public void deletePersonel(Integer id) {
        if (!personelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Silinecek personel bulunamadı, id: " + id);
        }
        personelRepository.deleteById(id);
    }

    @Override
    public List<Personel> getPersonellerByRol(String rolAdi) {
        return personelRepository.findByRolAdi(rolAdi);
    }
}