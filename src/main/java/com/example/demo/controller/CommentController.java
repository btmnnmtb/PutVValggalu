package com.example.demo.controller;

import com.example.demo.repository.CommentsRepository;
import com.example.demo.service.CommentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class CommentController {
    private final CommentsService commentsService;
    @PostMapping("/comments")
    public String addOrUpdate(@RequestParam Integer cosmeticItemId,
                              @RequestParam String commentText,
                              @RequestParam Integer rating,
                              Principal principal,
                              @RequestParam(defaultValue="/Catalog") String redirect) {
        commentsService.addOrUpdateComment(cosmeticItemId, principal.getName(), commentText, rating);
        return "redirect:" + redirect;
    }

    @PostMapping("/comments/{id}/delete")
    public String delete(@PathVariable Integer id,
                         Authentication auth,
                         @RequestParam(defaultValue="/Catalog") String redirect) {

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "Администратор".equalsIgnoreCase(a.getAuthority()));

        if (isAdmin) {
            commentsService.deleteCommentAsAdmin(id);
        } else {
            commentsService.deleteComment(id, auth.getName());
        }
        return "redirect:" + redirect;
    }

}
