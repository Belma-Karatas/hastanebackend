package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "Recete_Ilac") // SQL'deki tablo adıyla eşleşmeli
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReceteIlac {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recete_ilac_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recete_id", nullable = false)
    private Recete recete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ilac_id", nullable = false)
    private Ilac ilac;

    @Lob // Uzun metinler için
    @Column(name = "kullanim_sekli", columnDefinition = "TEXT")
    private String kullanimSekli;

    // İleride eklenebilecek diğer alanlar:
    // private String dozaj;
    // private Integer adet;
    // private String periyot; // (örn: "Günde 3 defa", "12 saatte bir")

    // equals ve hashCode metotları Lombok tarafından ID üzerinden otomatik yönetilebilir
    // veya manuel eklenebilir. Özellikle Set içinde kullanılacaksa önemlidir.
}