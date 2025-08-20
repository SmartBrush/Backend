package com.smartbrush.smartbrush_backend.entity;

import jakarta.persistence.*;
        import lombok.*;

@Entity
@Table(
        name = "community_like",
        uniqueConstraints = @UniqueConstraint(name = "uk_member_community", columnNames = {"member_id", "community_id"})
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CommunityLikeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id", nullable = false)
    private AuthEntity member;

}
