package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.dto.post.PostRequestDto;
import com.smartbrush.smartbrush_backend.dto.post.PostResponseDto;
import com.smartbrush.smartbrush_backend.entity.AuthEntity;
import com.smartbrush.smartbrush_backend.entity.Post;
import com.smartbrush.smartbrush_backend.repository.AuthRepository;
import com.smartbrush.smartbrush_backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AuthRepository authRepository;

    public List<PostResponseDto> getAllPosts() {
        List<Post> posts = postRepository.findAllWithTags();
        return posts.stream()
                .map(post -> PostResponseDto.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .author(post.getAuthor())
                        .content(post.getContent())
                        .imageUrl(post.getImageUrl())
                        .createdDate(post.getCreatedDate())
                        .tags(post.getTags() != null ? post.getTags() : new ArrayList<>())
                        .build())
                .toList();
    }





    public List<Post> getLikedPosts(String email) {
        AuthEntity user = authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        return new ArrayList<>(user.getLikedPosts());
    }

    public List<Post> getScrappedPosts(String email) {
        AuthEntity user = authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        return new ArrayList<>(user.getScrappedPosts());

    }

    public void likePost(Long postId, String email) {
        AuthEntity user = authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));
        user.getLikedPosts().add(post);
        authRepository.save(user);
    }

    public void scrapPost(Long postId, String email) {
        System.out.println("scrapPost 요청: postId=" + postId + ", email=" + email);

        AuthEntity user = authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음: id=" + postId));

        System.out.println("user.getScrapPosts(): " + user.getScrappedPosts());

        user.getScrappedPosts().add(post);
        authRepository.save(user);
    }


    public void createPost(PostRequestDto dto) {
        Post post = Post.builder()
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .tags(dto.getTags())
                .createdDate(LocalDate.now())
                .build();
        postRepository.save(post);
    }

}
