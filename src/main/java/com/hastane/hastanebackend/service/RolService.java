package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.entity.Rol;
import java.util.List;

public interface RolService {
    Rol createRol(Rol rol);
    List<Rol> getAllRoller();
}