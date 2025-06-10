package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.entity.Departman;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.service.DepartmanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departmanlar")
public class DepartmanController {

    private final DepartmanService departmanService;

    public DepartmanController(DepartmanService departmanService) {
        this.departmanService = departmanService;
    }

    @PostMapping
    public ResponseEntity<?> createDepartman(@RequestBody Departman departman) {
        try {
            Departman yeniDepartman = departmanService.createDepartman(departman);
            return new ResponseEntity<>(yeniDepartman, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Departman>> getAllDepartmanlar() {
        List<Departman> departmanlar = departmanService.getAllDepartmanlar();
        return ResponseEntity.ok(departmanlar);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Departman> getDepartmanById(@PathVariable Integer id) {
        return departmanService.getDepartmanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Departman> updateDepartman(@PathVariable Integer id, @RequestBody Departman departmanDetails) {
        try {
            Departman guncellenmisDepartman = departmanService.updateDepartman(id, departmanDetails);
            return ResponseEntity.ok(guncellenmisDepartman);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartman(@PathVariable Integer id) {
        try {
            departmanService.deleteDepartman(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}