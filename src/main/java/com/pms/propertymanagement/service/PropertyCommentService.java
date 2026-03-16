package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.response.PropertyCommentDTO;
import java.util.List;

public interface PropertyCommentService {
    PropertyCommentDTO createComment(PropertyCommentDTO commentDTO);
    void approveComment(Long commentId);
    void rejectComment(Long commentId, String reason);
    List<PropertyCommentDTO> getCommentsByProperty(Long propertyId);
    List<PropertyCommentDTO> getPendingComments();
    List<PropertyCommentDTO> getCommentsByUser(Long userId);
    void voteComment(Long commentId, Long userId, boolean helpful);
    void reportComment(Long commentId, Long userId, String reason);
}
