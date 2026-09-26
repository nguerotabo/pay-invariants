package com.payinvariants;

import jakarta.persistence.*;

@Entity
@Table(name = "idempotency_key")
public class IdempotencyKey {

    @Id
    @Column(name = "idempotency_key", length = 255, nullable = false)
    private String idempotency_key;

    @Column(name = "body_hash", length = 64, nullable = false)
    private String body_hash;

    @Column(name = "http_status", nullable = false)
    private int http_status;

    @Column(name = "response_body", nullable = false)
    private String response_body;

    // Constructors

    public IdempotencyKey(){}

    public IdempotencyKey(String idem_key, String body_hash, int http_status, String response_body){
        this.idempotency_key = idem_key;
        this.body_hash = body_hash;
        this.http_status = http_status;
        this.response_body = response_body;
    }

    // Getters & Setters

    public String getIdempotencyKey(){
        return idempotency_key;
    }

    public String getBodyHash(){
        return body_hash;
    }

    public int getHttpStatus(){
        return http_status;
    }

    public String getResponseBody(){
        return response_body;
    }

    public void setIdempotencyKey(String idem_key){
        this.idempotency_key = idem_key;
    }

    public void setBodyHash(String body_hash){
        this.body_hash = body_hash;
    }

    public void setHttpStatus(int http_status){
        this.http_status = http_status;
    }

    public void setResponseBody(String response_body){
        this.response_body = response_body;
    }
}