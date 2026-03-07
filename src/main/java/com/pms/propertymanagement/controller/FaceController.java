package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.request.FaceAddRequest;
import com.pms.propertymanagement.dto.request.FaceSearchKRequest;
import com.pms.propertymanagement.dto.request.FaceSearchRequest;
import com.pms.propertymanagement.dto.request.FaceVerifyRequest;
import com.pms.propertymanagement.dto.response.FaceAddResponse;
import com.pms.propertymanagement.dto.response.FaceSearchKResponse;
import com.pms.propertymanagement.dto.response.FaceSearchResponse;
import com.pms.propertymanagement.dto.response.FaceVerifyResponse;
import com.pms.propertymanagement.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/face-service/face")
@RequiredArgsConstructor
public class FaceController {

    private final AiService aiService;

    @PostMapping("/add")
    public ResponseEntity<FaceAddResponse> addFace(@RequestBody FaceAddRequest request) {
        return ResponseEntity.ok(aiService.faceAdd(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<FaceVerifyResponse> verifyFace(@RequestBody FaceVerifyRequest request) {
        return ResponseEntity.ok(aiService.faceVerify(request));
    }

    @PostMapping("/search")
    public ResponseEntity<FaceSearchResponse> searchFace(@RequestBody FaceSearchRequest request) {
        return ResponseEntity.ok(aiService.faceSearch(request));
    }

    @PostMapping("/search-k")
    public ResponseEntity<FaceSearchKResponse> searchKFace(@RequestBody FaceSearchKRequest request) {
        return ResponseEntity.ok(aiService.faceSearchK(request));
    }
}
