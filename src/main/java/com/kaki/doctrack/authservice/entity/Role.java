package com.kaki.doctrack.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@org.springframework.data.relational.core.mapping.Table("roles")
@Table(name="roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

}
