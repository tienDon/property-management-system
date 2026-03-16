package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.response.PropertyCommentDTO;
import com.pms.propertymanagement.service.PropertyCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class PropertyCommentController {

    private final PropertyCommentService commentService;

    @PostMapping
    public ResponseEntity<PropertyCommentDTO> createComment(@ModelAttribute PropertyCommentDTO commentDTO) {
        return ResponseEntity.ok(commentService.createComment(commentDTO));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<PropertyCommentDTO>> getPropertyComments(@PathVariable Long propertyId) {
        return ResponseEntity.ok(commentService.getCommentsByProperty(propertyId));
    }

    @PostMapping("/{commentId}/vote")
    public ResponseEntity<Void> voteComment(@PathVariable Long commentId, @RequestParam Long userId, @RequestParam boolean helpful) {
        commentService.voteComment(commentId, userId, helpful);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{commentId}/report")
    public ResponseEntity<Void> reportComment(@PathVariable Long commentId, @RequestParam Long userId, @RequestParam String reason) {
        commentService.reportComment(commentId, userId, reason);
        return ResponseEntity.ok().build();
    }
}
