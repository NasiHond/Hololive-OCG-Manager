package com.fhict.hololiveocgmanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "cards")
public class CardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer id;

    @Column(name = "cardid", nullable = false)
    private String cardid;

    @Column(name = "cardset", nullable = false, length = Integer.MAX_VALUE)
    private String cardset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "cardtype", nullable = false)
    private CardtypeEntity cardtype;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action =  OnDeleteAction.RESTRICT)
    @JoinColumn(name = "card_colour_id", nullable = false, referencedColumnName = "id")
    private ColourEntity cardcolour;

    @Column(name = "batonpass", nullable = false, length = Integer.MAX_VALUE)
    private String batonpass;

    @Column(name = "holomem", nullable = false, length = Integer.MAX_VALUE)
    private String holomem;

    @Column(name = "bloomlvl", nullable = false, length = Integer.MAX_VALUE)
    private String bloomlvl;

    @Column(name = "hp", nullable = false)
    private Integer hp;

    @Column(name = "rarity", nullable = false, length = Integer.MAX_VALUE)
    private String rarity;

    @Column(name = "image", length = Integer.MAX_VALUE)
    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extra_id")
    private ExtraEntity extra;
    @OneToMany(mappedBy = "cardid")
    @Builder.Default
    private Set<CardartEntity> cardarts = new LinkedHashSet<>();
    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<KeywordEntity> keywords = new LinkedHashSet<>();
    @OneToMany(mappedBy = "cardid")
    @Builder.Default
    private Set<CardtagEntity> cardtags = new LinkedHashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCardid() {
        return cardid;
    }

    public void setCardid(String cardid) {
        this.cardid = cardid;
    }

    public String getCardset() {
        return cardset;
    }

    public void setCardset(String cardset) {
        this.cardset = cardset;
    }

    public CardtypeEntity getCardtype() {
        return cardtype;
    }

    public void setCardtype(CardtypeEntity cardtype) {
        this.cardtype = cardtype;
    }

    public Integer getCardcolourID() {
        return cardcolour == null ? null : cardcolour.getId();
    }

    public void setCardcolour(ColourEntity cardcolour) {
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

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public Set<KeywordEntity> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<KeywordEntity> keywords) {
        this.keywords = keywords;
    }

}