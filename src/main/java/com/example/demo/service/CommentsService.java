package com.example.demo.service;

import com.example.demo.model.Comments;
import com.example.demo.model.Cosmetic_items;
import com.example.demo.model.User;
import com.example.demo.repository.CommentsRepository;
import com.example.demo.repository.CosmeticItemRepository;
import com.example.demo.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentsService {
     private final UsersRepository usersRepository;
     private final CosmeticItemRepository cosmeticItemRepository;
     private final CommentsRepository commentsRepository;


    @Transactional
    public Comments addOrUpdateComment(Integer cosmeticItemId, String username, String commentText, Integer rating) {
        User user = usersRepository.findByLogin(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + username));

        Cosmetic_items item = cosmeticItemRepository.findById(cosmeticItemId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден: id=" + cosmeticItemId));

        Comments entity = Comments.builder()
                .user(user)
                .cosmeticItem(item)
                .commentText(commentText)
                .rating(rating)
                .build();

        entity.setCommentText(commentText.trim());
        if (rating != null) {
            entity.setRating(rating);
        }

        return commentsRepository.save(entity);
    }
    @Transactional
    public boolean deleteComment(Integer commentId, String username) {
        Integer deleted = commentsRepository.deleteByCommentIdAndUser_Login(commentId, username);
        return deleted > 0;
    }


    @Transactional
    public void deleteCommentAsAdmin(Integer commentId) {
        if (!commentsRepository.existsById(commentId)) {
            throw new IllegalArgumentException("Комментарий не найден: id=" + commentId);
        }
        commentsRepository.deleteById(commentId);
    }
    public Map<Integer, List<Comments>> findAllGroupedByCosmeticItemId() {
        return commentsRepository.findAll().stream()
                .collect(Collectors.groupingBy(c -> c.getCosmeticItem().getCosmeticItemId(),
                        LinkedHashMap::new, Collectors.toList()));
    }

}
