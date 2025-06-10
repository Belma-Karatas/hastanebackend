package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.entity.Departman;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.DepartmanRepository;
import com.hastane.hastanebackend.service.DepartmanService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmanServiceImpl implements DepartmanService {

    private final DepartmanRepository departmanRepository;

    public DepartmanServiceImpl(DepartmanRepository departmanRepository) {
        this.departmanRepository = departmanRepository;
    }

    @Override
    public List<Departman> getAllDepartmanlar() {
        return departmanRepository.findAll();
    }

    @Override
    public Optional<Departman> getDepartmanById(Integer id) {
        return departmanRepository.findById(id);
    }

    @Override
    public Departman createDepartman(Departman departman) {
        if (departmanRepository.existsByAd(departman.getAd())) {
            throw new IllegalArgumentException("Bu departman adı zaten mevcut: " + departman.getAd());
        }
        return departmanRepository.save(departman);
    }

    @Override
    public Departman updateDepartman(Integer id, Departman departmanDetails) {
        Departman departman = departmanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departman bulunamadı, id: " + id));

        departman.setAd(departmanDetails.getAd());
        return departmanRepository.save(departman);
    }

    @Override
    public void deleteDepartman(Integer id) {
        if (!departmanRepository.existsById(id)) {
            throw new ResourceNotFoundException("Silinecek departman bulunamadı, id: " + id);
        }
        departmanRepository.deleteById(id);
    }
}