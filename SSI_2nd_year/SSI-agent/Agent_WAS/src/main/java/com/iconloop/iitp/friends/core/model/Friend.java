package com.iconloop.iitp.friends.core.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


@Getter
@Setter
@Entity
public class Friend {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 100)
    private String did;

    @Column(length = 20)
    private String name;

    @Column(length = 100, name = "ms_token", nullable = true)    // messaging server token
    private String msToken;

    public Friend() {

    }

    @Builder
    public Friend(String did, String name) {
        this.name = name;
        this.did = did;
    }

}
