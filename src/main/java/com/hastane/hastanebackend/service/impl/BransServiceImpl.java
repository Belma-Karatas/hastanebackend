package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.entity.Brans;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.BransRepository;
import com.hastane.hastanebackend.service.BransService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BransServiceImpl implements BransService {

    private final BransRepository bransRepository;

    public BransServiceImpl(BransRepository bransRepository) {
        this.bransRepository = bransRepository;
    }

    @Override
    public List<Brans> getAllBranslar() {
        return bransRepository.findAll();
    }

    @Override
    public Optional<Brans> getBransById(Integer id) {
        return bransRepository.findById(id);
    }

    @Override
    public Brans createBrans(Brans brans) {
        if (bransRepository.existsByAd(brans.getAd())) {
            throw new IllegalArgumentException("Bu branş adı zaten mevcut: " + brans.getAd());
        }
        return bransRepository.save(brans);
    }

    @Override
    public Brans updateBrans(Integer id, Brans bransDetails) {
        Brans brans = bransRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branş bulunamadı, id: " + id));

        brans.setAd(bransDetails.getAd());
        return bransRepository.save(brans);
    }

    @Override
    public void deleteBrans(Integer id) {
        if (!bransRepository.existsById(id)) {
            throw new ResourceNotFoundException("Silinecek branş bulunamadı, id: " + id);
        }
        bransRepository.deleteById(id);
    }
}