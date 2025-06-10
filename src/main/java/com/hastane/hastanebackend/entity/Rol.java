package com.hastane.hastanebackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference; // YENİ EKLENEN IMPORT
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Rol")
@Getter
@Setter
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id")
    private Integer id;

    @Column(name = "ad", nullable = false, unique = true, length = 50)
    private String ad;

    @ManyToMany(mappedBy = "roller")
    @JsonBackReference // YENİ EKLENEN ANNOTASYON
    private Set<Kullanici> kullanicilar = new HashSet<>();
}