package com.careerhub.identity.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(
                        name = "idx_users_email",
                        columnList = "email",
                        unique = true
                )
        }
)
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private UUID id;
    @Column(
            nullable = false,
            unique = true,
            length = 255
    )
    private String email;
    @Column(
            name = "password_hash",
            nullable = false
    )
    private String passwordHash;
    @Column(
            name = "first_name",
            nullable = false,
            length = 100
    )
    private String firstName;
    @Column(length = 100)
    private String lastName;
    @Column(nullable = false)
    private boolean enabled = true;
    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;
    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    @PrePersist
    void onCreate(){
        Instant now = Instant.now();

        if(id == null){
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;

    }
    @PreUpdate
    void onUpdate(){
        updatedAt = Instant.now();
    }
}
