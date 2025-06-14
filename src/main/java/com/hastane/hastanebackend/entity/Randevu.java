package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp; // Oluşturulma tarihi için

import java.time.LocalDateTime;

@Entity
@Table(name = "Randevu")
@Getter
@Setter
@NoArgsConstructor
public class Randevu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "randevu_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "hasta_id", nullable = false)
    private Hasta hasta;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "personel_id", nullable = false)
    private Personel doktor; 

    @Column(name = "randevu_tarihi_saati", nullable = false)
    private LocalDateTime randevuTarihiSaati; 

    @Column(name = "durum", length = 50, nullable = false)
    private String durum = "PLANLANDI"; 

    

    @CreationTimestamp 
    @Column(name = "olusturulma_tarihi", updatable = false)
    private LocalDateTime olusturulmaTarihi;

    // Muayene ile bire-bir ilişki (opsiyonel, Muayene entity'si oluşturulunca eklenecek)
    // @OneToOne(mappedBy = "randevu", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private Muayene muayene;
}