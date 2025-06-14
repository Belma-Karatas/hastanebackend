package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "Izin_Turu") // SQL'deki tablo adıyla eşleşmeli
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IzinTuru {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "izin_turu_id")
    private Integer id;

    @Column(name = "ad", nullable = false, length = 100, unique = true) // İzin türü adı benzersiz olmalı
    private String ad;
}