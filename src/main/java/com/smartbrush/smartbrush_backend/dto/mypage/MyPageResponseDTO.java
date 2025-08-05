package com.smartbrush.smartbrush_backend.dto.mypage;

import com.smartbrush.smartbrush_backend.dto.comment.CommentResponseDTO;
import com.smartbrush.smartbrush_backend.dto.community.CommunityResponseDTO;
import com.smartbrush.smartbrush_backend.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyPageResponseDTO {
    private String nickname;
    private String profileImage;
    private int attendanceDays;
    private String email;
}
