package org.example.expert.domain.comment.controller;

import org.example.expert.config.JwtUtil;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private CommentRepository commentRepository;

    @Test
    void 어드민_권한으로_댓글_삭제_API_요청_시_정상_동작한다() throws Exception {
        // given
        Comment comment = new Comment();
        String token = jwtUtil.createToken(1L, "aa", UserRole.ADMIN);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when & then
        mockMvc.perform(delete("/admin/comments/1")
                .header("Authorization", token)
                .header("User-Role", UserRole.ADMIN.name())
                .header("User-Id", 1L)).andExpect(status().isOk());
    }

    @Test
    void 일반_사용자가_댓글_삭제_API_요청_시_예외가_발생한다() throws Exception {
        //given
        Comment comment = new Comment();
        String token = jwtUtil.createToken(1L, "aa", UserRole.USER);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when & then
        mockMvc.perform(delete("/admin/comments/1")
                .header("Authorization", token))
                .andExpect(status().is(403));
    }
}
