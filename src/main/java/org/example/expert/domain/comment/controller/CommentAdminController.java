package org.example.expert.domain.comment.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.EntityResponser;
import org.example.expert.domain.comment.service.CommentAdminService;
import org.example.expert.domain.common.annotation.AdminLogging;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentAdminController {

    private final CommentAdminService commentAdminService;

    @DeleteMapping("/admin/comments/{commentId}")
    @AdminLogging
    public ResponseEntity<Void> deleteComment(@PathVariable long commentId) {
        commentAdminService.deleteComment(commentId);
        return EntityResponser.voidResponser(HttpStatus.OK);
    }
}
