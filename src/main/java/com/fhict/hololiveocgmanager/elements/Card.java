package com.fhict.hololiveocgmanager.elements;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "cardid", nullable = false)
    private Integer cardid;

    @Column(name = "cardset", nullable = false, length = Integer.MAX_VALUE)
    private String cardset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "cardtype", nullable = false, referencedColumnName = "name")
    private Cardtype cardtype;

    @Column(name = "cardcolour", nullable = false, length = Integer.MAX_VALUE)
    private String cardcolour;

    @Column(name = "batonpass", nullable = false, length = Integer.MAX_VALUE)
    private String batonpass;

    @Column(name = "holomem", nullable = false, length = Integer.MAX_VALUE)
    private String holomem;

    @Column(name = "bloomlvl", nullable = false, length = Integer.MAX_VALUE)
    private String bloomlvl;

    @Column(name = "hp", nullable = false)
    private Integer hp;

    @Column(name = "isbuzz", nullable = false)
    private Boolean isbuzz;

    @Column(name = "rarity", nullable = false, length = Integer.MAX_VALUE)
    private String rarity;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCardid() {
        return cardid;
    }

    public void setCardid(Integer cardid) {
        this.cardid = cardid;
    }

    public String getCardset() {
        return cardset;
    }

    public void setCardset(String cardset) {
        this.cardset = cardset;
    }

    public Cardtype getCardtype() {
        return cardtype;
    }

    public void setCardtype(Cardtype cardtype) {
        this.cardtype = cardtype;
    }

    public String getCardcolour() {
        return cardcolour;
    }

    public void setCardcolour(String cardcolour) {
        this.cardcolour = cardcolour;
    }

    public String getBatonpass() {
        return batonpass;
    }

    public void setBatonpass(String batonpass) {
        this.batonpass = batonpass;
    }

    public String getHolomem() {
        return holomem;
    }

    public void setHolomem(String holomem) {
        this.holomem = holomem;
    }

    public String getBloomlvl() {
        return bloomlvl;
    }

    public void setBloomlvl(String bloomlvl) {
        this.bloomlvl = bloomlvl;
    }

    public Integer getHp() {
        return hp;
    }

    public void setHp(Integer hp) {
        this.hp = hp;
    }

    public Boolean getIsbuzz() {
        return isbuzz;
    }

    public void setIsbuzz(Boolean isbuzz) {
        this.isbuzz = isbuzz;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

}