package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet; // Bu importu ekle
import java.util.Set;     // Bu importu ekle

@Entity
@Table(name = "Muayene")
@Getter
@Setter
@NoArgsConstructor
public class Muayene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "muayene_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "randevu_id", unique = true, nullable = true)
    private Randevu randevu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hasta_id", nullable = false)
    private Hasta hasta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doktor_personel_id", nullable = false)
    private Personel doktor;

    @Column(name = "muayene_tarihi_saati", nullable = false)
    private LocalDateTime muayeneTarihiSaati;

    @Column(name = "hikaye", columnDefinition = "TEXT")
    private String hikaye;
    
    @Column(name = "tani", columnDefinition = "TEXT")
    private String tani;

    @Column(name = "tedavi_notlari", columnDefinition = "TEXT")
    private String tedaviNotlari;

    @CreationTimestamp
    @Column(name = "olusturulma_tarihi", updatable = false)
    private LocalDateTime olusturulmaTarihi;

    // YENİ EKLENEN KISIM: Muayene'den Recete'ye OneToMany ilişki
    // Bir muayenenin sıfır veya daha fazla reçetesi olabilir.
    // `mappedBy = "muayene"`: Recete entity'sindeki 'muayene' alanı bu ilişkinin sahibidir.
    // `cascade = CascadeType.ALL`: Muayene silinirse ilişkili reçeteler de silinir.
    // `orphanRemoval = true`: Muayeneden bir reçete çıkarılırsa (ve başka muayeneye atanmazsa) DB'den silinir.
    @OneToMany(mappedBy = "muayene", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Recete> receteler = new HashSet<>();

    // Yardımcı metotlar (opsiyonel ama iyi pratiktir)
    public void addRecete(Recete recete) {
        receteler.add(recete);
        recete.setMuayene(this);
    }

    public void removeRecete(Recete recete) {
        receteler.remove(recete);
        recete.setMuayene(null);
    }
}