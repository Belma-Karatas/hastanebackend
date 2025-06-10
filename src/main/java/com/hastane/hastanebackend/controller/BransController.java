package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.entity.Brans;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.service.BransService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/branslar")
public class BransController {

    private final BransService bransService;

    public BransController(BransService bransService) {
        this.bransService = bransService;
    }

    @PostMapping
    public ResponseEntity<?> createBrans(@RequestBody Brans brans) {
        try {
            Brans yeniBrans = bransService.createBrans(brans);
            return new ResponseEntity<>(yeniBrans, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Brans>> getAllBranslar() {
        return ResponseEntity.ok(bransService.getAllBranslar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Brans> getBransById(@PathVariable Integer id) {
        return bransService.getBransById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBrans(@PathVariable Integer id, @RequestBody Brans bransDetails) {
        try {
            Brans guncellenmisBrans = bransService.updateBrans(id, bransDetails);
            return ResponseEntity.ok(guncellenmisBrans);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrans(@PathVariable Integer id) {
        try {
            bransService.deleteBrans(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}