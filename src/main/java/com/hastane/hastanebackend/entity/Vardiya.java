package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "Vardiya")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vardiya {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vardiya_id")
    private Integer id;

    // Örnek adlar: "Doktor Gündüz (09-17)", "Hemşire Gündüz (08-17)", "Hemşire Gece (17-08)"
    @Column(name = "ad", nullable = false, length = 100, unique = true) // Vardiya adı daha açıklayıcı olabilir
    private String ad;

    @Column(name = "baslangic_saati", nullable = false)
    private LocalTime baslangicSaati;

    @Column(name = "bitis_saati", nullable = false)
    private LocalTime bitisSaati;

    // PersonelVardiya ile olan ilişkiyi burada tutmuyoruz, PersonelVardiya tarafından yönetilecek.
}
