package com.smartbrush.smartbrush_backend.dto.profile;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateRequest {
    private String nickname;
    private String email;
    private String profileImage;
}