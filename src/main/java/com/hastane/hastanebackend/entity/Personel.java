package com.hastane.hastanebackend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference; // BU IMPORTU EKLE
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Personel")
@Getter
@Setter
public class Personel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "personel_id")
    private Integer id;

    @Column(name = "ad", nullable = false, length = 100)
    private String ad;

    @Column(name = "soyad", nullable = false, length = 100)
    private String soyad;

    @Column(name = "telefon", length = 20)
    private String telefon;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "kullanici_id", referencedColumnName = "kullanici_id", unique = true, nullable = false) // 'nullable = false' eklendi, her personelin bir kullanıcısı olmalı.
    private Kullanici kullanici;

    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "departman_id") 
    private Departman departman;

    
    // cascade = CascadeType.ALL: Personel kaydedildiğinde/güncellendiğinde/silindiğinde DoktorDetay da etkilenir.
    // orphanRemoval = true: Personel'den DoktorDetay ilişkisi kaldırıldığında (doktorDetay = null yapıldığında) DoktorDetay kaydı DB'den silinir.
    @OneToOne(mappedBy = "personel", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference("personel-doktordetay") // JSON serileştirmesinde sonsuz döngüyü önlemek için. DoktorDetay'da @JsonBackReference olacak.
    private DoktorDetay doktorDetay;

    // İki yönlü ilişkiyi yönetmek için yardımcı metotlar (opsiyonel ama iyi pratiktir)
    public void setDoktorDetay(DoktorDetay doktorDetay) {
        if (doktorDetay == null) {
            if (this.doktorDetay != null) {
                this.doktorDetay.setPersonel(null);
            }
        } else {
            doktorDetay.setPersonel(this);
        }
        this.doktorDetay = doktorDetay;
    }
}