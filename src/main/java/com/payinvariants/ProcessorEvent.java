package com.payinvariants;

import jakarta.persistence.*;

@Entity
@Table(name = "webhooks")
public class ProcessorEvent {

    @Id
    @Column(name = "message_id", updatable = false, nullable = false)
        private String message_id;

    @Column(name = "card_token", nullable = false)
        private String card_token;

    @Column(name = "last_four", nullable = false, length = 4)
        private String last_four;

    // Constructors

    public ProcessorEvent(){};

    public ProcessorEvent(String message_id, String card_token, String last_four){
        this.message_id = message_id;
        this.card_token = card_token;
        this.last_four = last_four;
    }

    // Getters & setters 

    public String getMessageId(){
        return message_id;
    }

    public String getCardToken(){
        return card_token;
    }

    public String getLastFour(){
        return last_four;
    }

    public void setMessageID(String message_id){
        this.message_id = message_id;
    }

    public void setCardToken(String card_token){
        this.card_token = card_token;
    }

    public void setLastFour(String last_four){
        this.last_four = last_four;
    }
}