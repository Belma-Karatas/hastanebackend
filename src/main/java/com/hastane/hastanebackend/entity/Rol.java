package com.hastane.hastanebackend.entity;

// import com.fasterxml.jackson.annotation.JsonBackReference; // BU SATIRI SİLİN VEYA YORUM SATIRI YAPIN
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
// import com.fasterxml.jackson.annotation.JsonIdentityReference; // İsteğe bağlı

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Rol")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Rol.class) // EKLENDİ
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id")
    private Integer id;

    @Column(name = "ad", nullable = false, unique = true, length = 50)
    private String ad;

    @ManyToMany(mappedBy = "roller", fetch = FetchType.LAZY)
    // @JsonBackReference("kullanici-rol-referansi") // BU SATIRI SİLİN VEYA YORUM SATIRI YAPIN
    // @JsonIdentityReference(alwaysAsId = true) // İsteğe bağlı, kullanıcıların sadece ID'leri olarak serileştirilmesi için
    private Set<Kullanici> kullanicilar = new HashSet<>();
}