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
@Table(name = "collections")
public class CollectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action =  OnDeleteAction.CASCADE)
    @JoinColumn(name = "ownerId", nullable = false)
    private UserEntity ownerId;

    @Column(name = "visibility")
    private Integer visibility;
}
