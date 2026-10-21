package com.careerhub.identity.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "roles"
)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Role {
    @Id
    private UUID id;
    @Column(
            nullable = false,
            unique = true,
            length = 255
    )
    private String name;
    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;
    @PrePersist
    void onCreate(){
        Instant now = Instant.now();
        if(id == null){
            id = UUID.randomUUID();
        }
        if(createdAt == null){
            createdAt = Instant.now();
        }
    }
}
