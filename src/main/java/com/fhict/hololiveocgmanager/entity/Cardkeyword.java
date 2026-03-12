package com.fhict.hololiveocgmanager.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "cardkeywords")
public class Cardkeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "cardid", nullable = false)
    private Card cardid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "keywordid", nullable = false)
    private Keyword keywordid;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Card getCardid() {
        return cardid;
    }

    public void setCardid(Card cardid) {
        this.cardid = cardid;
    }

    public Keyword getKeywordid() {
        return keywordid;
    }

    public void setKeywordid(Keyword keywordid) {
        this.keywordid = keywordid;
    }

}