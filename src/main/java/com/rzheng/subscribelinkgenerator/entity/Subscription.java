package com.rzheng.subscribelinkgenerator.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Date;

@Entity
@Table(name = "subscription")
public class Subscription {

    @Id
    private String subKey;

    private Date expiration;

    public String getSubKey() {
        return subKey;
    }

    public void setSubKey(String key) {
        this.subKey = key;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }
}