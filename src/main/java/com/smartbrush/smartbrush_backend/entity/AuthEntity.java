//package com.smartbrush.smartbrush_backend.entity;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
//@Entity
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class AuthEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(unique = true, nullable = false)
//    private String email;
//
//    @Column(nullable = false)
//    private String password;
//
//    @Column(nullable = false)
//    private String nickname;
//
//    private String profileImage;
//
//    @ManyToMany
//    @JoinTable(
//            name = "post_likes",
//            joinColumns = @JoinColumn(name = "user_id"),
//            inverseJoinColumns = @JoinColumn(name = "post_id")
//    )
//    @Builder.Default
//    @JsonIgnore
//    private List<Post> likedPosts = new ArrayList<>();
//
//
//    @ManyToMany
//    @JoinTable(
//            name = "post_scraps",
//            joinColumns = @JoinColumn(name = "user_id"),
//            inverseJoinColumns = @JoinColumn(name = "post_id")
//    )
//    @Builder.Default
//    @JsonIgnore
//    private List<Post> scrappedPosts  = new ArrayList<>();
//
//    // 제품 추천 위시리스트
//    @ManyToMany(fetch = FetchType.EAGER)
//    @JoinTable(
//            name = "product_wishlist",
//            joinColumns = @JoinColumn(name = "user_id"),
//            inverseJoinColumns = @JoinColumn(name = "product_id")
//    )
//    @Builder.Default
//    @JsonIgnore
//    private List<Product> wishlist = new ArrayList<>();
//
//}
//


package com.smartbrush.smartbrush_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    private String profileImage;

    /** 🔹 리프레시 토큰 저장용 컬럼 추가 */
    @Column(length = 512)
    private String refreshToken;



    // 제품 추천 위시리스트
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "product_wishlist",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @Builder.Default
    @JsonIgnore
    private List<Product> wishlist = new ArrayList<>();
}
