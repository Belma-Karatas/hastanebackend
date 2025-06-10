package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.entity.Rol;
import com.hastane.hastanebackend.repository.RolRepository;
import com.hastane.hastanebackend.service.RolService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    public RolServiceImpl(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public Rol createRol(Rol rol) {
        return rolRepository.save(rol);
    }

    @Override
    public List<Rol> getAllRoller() {
        return rolRepository.findAll();
    }
}