package com.trongtin.spabooking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "therapist_services",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_therapist_service",
                columnNames = {"therapist_id", "service_id"}
        )
)
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TherapistService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapist_id", nullable = false)
    private Therapist therapist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level", length = 20)
    @Builder.Default
    private SkillLevel skillLevel = SkillLevel.INTERMEDIATE;

    @Column(name = "years_experience")
    @Builder.Default
    private Integer yearsExperience = 0;

    @Column(name = "is_primary_service")
    @Builder.Default
    private Boolean isPrimaryService = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}