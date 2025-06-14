package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Personel_Vardiya", uniqueConstraints = {
    // Bir personel aynı tarihte birden fazla vardiyada olamaz.
    @UniqueConstraint(columnNames = {"personel_id", "tarih"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonelVardiya {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "personel_vardiya_id")
    private Integer id; // SQL'deki gibi ayrı bir PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personel_id", nullable = false)
    private Personel personel;

    @ManyToOne(fetch = FetchType.EAGER) // Vardiya bilgisi genellikle hemen istenir
    @JoinColumn(name = "vardiya_id", nullable = false)
    private Vardiya vardiya;

    @Column(name = "tarih", nullable = false)
    private LocalDate tarih; // Personelin bu vardiyada çalıştığı gün

    @Column(name = "atama_tarihi", nullable = false, updatable = false)
    @CreationTimestamp // Bu kaydın ne zaman oluşturulduğu
    private LocalDateTime atamaTarihi;

    // İleride eklenebilecek alanlar:
    // private String notlar; // Vardiya ile ilgili özel notlar
    // private boolean geldiMi; // Personel o gün vardiyaya geldi mi?
}