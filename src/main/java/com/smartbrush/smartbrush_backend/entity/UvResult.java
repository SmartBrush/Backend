package com.smartbrush.smartbrush_backend.entity;



import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UvResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int uv;

    private String state;

    private String deviceId;

    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private AuthEntity user;

    public static UvResult create(int uv, String state, String deviceId, AuthEntity user) {
        return UvResult.builder()
                .uv(uv)
                .state(state)
                .deviceId(deviceId)
                .timestamp(LocalDateTime.now())
                .user(user)
                .build();
    }
}
