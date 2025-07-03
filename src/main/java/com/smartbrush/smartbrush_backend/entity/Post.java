package com.smartbrush.smartbrush_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnore;




@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String author;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imageUrl;

    private LocalDate createdDate;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    @JsonIgnore
    @ManyToMany(mappedBy = "likedPosts")
    private Set<AuthEntity> likedByUsers = new HashSet<>();

    @Builder.Default
    @JsonIgnore
    @ManyToMany(mappedBy = "scrappedPosts")
    private Set<AuthEntity> scrappedByUsers = new HashSet<>();





}
