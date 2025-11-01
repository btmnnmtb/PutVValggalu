package com.example.demo.repository;

import com.example.demo.model.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CommentsRepository extends JpaRepository<Comments, Integer> {
    Optional<Comments> findByUser_UserIdAndCosmeticItem_CosmeticItemId(Integer userId, Integer itemId);
    Integer deleteByCommentIdAndUser_Login(Integer commentId, String login);

}
