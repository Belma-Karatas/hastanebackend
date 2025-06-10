package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Brans")
@Getter
@Setter
public class Brans {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brans_id")
    private Integer id;

    @Column(name = "brans_adi", nullable = false, unique = true, length = 100)
    private String ad;
}