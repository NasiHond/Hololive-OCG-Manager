package com.fhict.hololiveocgmanager.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "creatorId", nullable = false)
    private Integer creatorId;

    @Column(name = "deckName", nullable = false)
    private String deckName;

    @Column(name = "deckDescription")
    private String deckDescription;

    @Column(name = "visibility", nullable = false)
    private Integer visibility;
}
