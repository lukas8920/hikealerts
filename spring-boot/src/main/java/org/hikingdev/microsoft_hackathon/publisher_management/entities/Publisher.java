package org.hikingdev.microsoft_hackathon.publisher_management.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Publisher {
    @Id
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private Status status;
    private String copyright;
    private String license;
}
