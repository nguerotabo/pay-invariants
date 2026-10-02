package com.payinvariants;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "charges")
public class Charges {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "amount", nullable = false)
    private int amount; 

    @Column(name = "card_token", nullable = false)
    private String cardToken;

    // Constructors

    public Charges(){}

    public Charges(UUID id, int amount){
        this.id = id;
        this.amount = amount;
    }

    // Getters & Setters

    public UUID getId(){
        return id;
    }

    public int getAmount(){
        return amount;
    }

    public String getCardToken(){
        return cardToken;
    }

    public void setId(UUID id){
        this.id = id;
    }

    public void setAmount(int amount){
        this.amount = amount;
    }

    public void setCardToken(String cardToken){
        this.cardToken = cardToken;
    }
}

