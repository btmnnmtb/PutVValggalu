package com.example.demo.controller;

import com.example.demo.model.Comments;
import com.example.demo.model.CommentsDto;
import com.example.demo.model.Cosmetic_items;
import com.example.demo.model.User;
import com.example.demo.repository.CommentsRepository;
import com.example.demo.repository.CosmeticItemRepository;
import com.example.demo.repository.UsersRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentApiController {
    private final CommentsRepository commentsRepository;
    private final UsersRepository usersRepository;
    private final CosmeticItemRepository cosmeticItemRepository;
    @GetMapping
    public List<Comments> getComments(){
        return commentsRepository.findAll();
    }
    @GetMapping("/{id}")
    public Comments getCommentsById(@PathVariable Integer id){
        return commentsRepository.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
    }
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Comments createComment(@Valid @RequestBody CommentsDto dto) {

        User user = usersRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: id=" + dto.getUserId()));

        Cosmetic_items item = cosmeticItemRepository.findById(dto.getCosmeticItemId())
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден: id=" + dto.getCosmeticItemId()));

        Comments comment = Comments.builder()
                .user(user)
                .cosmeticItem(item)
                .commentText(dto.getCommentText().trim())
                .rating(dto.getRating())
                .build();

        return commentsRepository.save(comment);

    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable Integer id){
        commentsRepository.deleteById(id);
        return ResponseEntity.ok("Комемнтарий удален ");
    }
}

