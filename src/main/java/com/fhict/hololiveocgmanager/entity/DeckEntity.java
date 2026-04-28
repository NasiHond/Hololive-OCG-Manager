package com.fhict.hololiveocgmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "decks")
public class DeckEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action =  OnDeleteAction.CASCADE)
    @JoinColumn(name = "creatorId", nullable = false)
    private UserEntity creatorId;

    @Column(name = "deckName", nullable = false)
    private String deckName;

    @Column(name = "deckDescription")
    private String deckDescription;

    @Column(name = "visibility", nullable = false)
    private Integer visibility;
}
