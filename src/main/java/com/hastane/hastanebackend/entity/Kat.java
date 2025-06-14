package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Kat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Kat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kat_id")
    private Integer id;

    @Column(name = "kat_adi", nullable = false, length = 50, unique = true) // Kat adının benzersiz olduğunu varsayıyoruz
    private String ad;

    // Bir katta birden fazla oda olabilir.
    // Kat silindiğinde, ona bağlı odaların da silinmesini istiyoruz (cascade = CascadeType.ALL).
    // Bir oda kat ilişkisinden çıkarılırsa (orphanRemoval = true), oda kaydı da silinir.
    @OneToMany(mappedBy = "kat", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Oda> odalar = new HashSet<>();

    // Yardımcı metotlar (Oda ekleme/çıkarma için - iyi bir pratiktir)
    public void addOda(Oda oda) {
        odalar.add(oda);
        oda.setKat(this);
    }

    public void removeOda(Oda oda) {
        odalar.remove(oda);
        oda.setKat(null);
    }
}