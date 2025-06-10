package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.entity.Brans;
import java.util.List;
import java.util.Optional;

public interface BransService {

    List<Brans> getAllBranslar();

    Optional<Brans> getBransById(Integer id);

    Brans createBrans(Brans brans);

    Brans updateBrans(Integer id, Brans bransDetails);

    void deleteBrans(Integer id);
}