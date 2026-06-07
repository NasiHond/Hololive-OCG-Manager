package com.fhict.hololiveocgmanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reviews")
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @OnDelete(action =  OnDeleteAction.CASCADE)
    @JoinColumn(name = "deckId", nullable = false)
    private DeckEntity deck;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @OnDelete(action =  OnDeleteAction.CASCADE)
    @JoinColumn(name = "userId", nullable = false)
    private UserEntity user;

    @Column(name = "rating", nullable = false)
    @Builder.Default
    private Double rating = 2.5;

    @Column(name = "Comment")
    private String comment;

    public Boolean isValidForCreate()
    {
        return deck != null && user != null && rating != null && rating > 0;
    }

}
