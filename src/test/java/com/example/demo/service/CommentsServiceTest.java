package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.CommentsRepository;
import com.example.demo.repository.CosmeticItemRepository;
import com.example.demo.repository.ProductStatusRepository;
import com.example.demo.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

class CommentsServiceTest {
    @Mock
    UsersRepository usersRepository;
    @Mock
    CosmeticItemRepository cosmeticItemRepository;
    @Mock
    CommentsRepository commentsRepository;
    @InjectMocks
    CommentsService commentsService;

    @Test
    void addOrUpdateCommentUserNotFound() {
        String username = "username";
        when(usersRepository.findByLogin(username)).thenReturn(Optional.empty());
        int cosmeticItemId = 1;
        String commentText = "Текст комментария";
        int rating = 5;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> commentsService.addOrUpdateComment(cosmeticItemId,username , commentText , rating));

        assertEquals("Пользователь не найден: " + username ,  exception.getMessage());


    }
    @Test
    void addOrUpdateCommentItemNotFound() {
        String username = "username";
        User user = new User();
        user.setUserId(1);


        String commentText = "Текст комментария";
        int rating = 5;
        when(usersRepository.findByLogin(username)).thenReturn(Optional.of(user));

        when(cosmeticItemRepository.findById(anyInt())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> commentsService.addOrUpdateComment(1 , username , commentText , rating));

        assertEquals("Товар не найден: id=" + 1 ,  ex.getMessage());


    }



    @Test
    void deleteCommentAsAdminCommentNotFound() {
        when(commentsRepository.existsById(anyInt())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> commentsService.deleteCommentAsAdmin(1));

        assertEquals("Комментарий не найден: id=" + 1 ,  exception.getMessage());
    }

}