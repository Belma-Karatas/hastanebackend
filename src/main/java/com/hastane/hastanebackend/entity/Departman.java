package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Departman")
@Getter
@Setter
public class Departman {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "departman_id")
    private Integer id;

    @Column(name = "departman_adi", nullable = false, unique = true, length = 100)
    private String ad;

    // Not: Personel listesini buraya eklemiyoruz.
    // Departman tarafından Personel'e olan ilişkiyi genellikle yönetmeyiz.
    // İlişkiyi tek yönlü (Personel -> Departman) tutmak daha basit ve verimlidir.
}