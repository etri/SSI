package com.iconloop.iitp.friends.core.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Setter
@Entity
public class Delegated {
    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 100)
    private String holder;

    @Column(length = 100)
    private String delegated;

    @Column(length = 3000)
    private String vcHolder;

    @Column(length = 3000)
    private String vcPoa;

    @Column(length = 100)
    private String token;

    public Delegated() {

    }

    public Delegated(String holder, String delegated, String vcHolder, String vcPoa, String token) {
        this.holder = holder;
        this.delegated = delegated;
        this.vcHolder = vcHolder;
        this.vcPoa = vcPoa;
        this.token = token;
    }
}
