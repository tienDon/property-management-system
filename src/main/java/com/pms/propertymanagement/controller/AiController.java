package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.request.ClassifyIdRequest;
import com.pms.propertymanagement.dto.request.FaceCompareRequest;
import com.pms.propertymanagement.dto.request.FaceLivenessRequest;
import com.pms.propertymanagement.dto.request.FaceMaskRequest;
import com.pms.propertymanagement.dto.request.LivenessRequest;
import com.pms.propertymanagement.dto.request.OcrBackRequest;
import com.pms.propertymanagement.dto.request.OcrFrontRequest;
import com.pms.propertymanagement.dto.request.OcrIdRequest;
import com.pms.propertymanagement.dto.response.ClassifyIdResponse;
import com.pms.propertymanagement.dto.response.FaceCompareResponse;
import com.pms.propertymanagement.dto.response.FaceLivenessResponse;
import com.pms.propertymanagement.dto.response.FaceMaskResponse;
import com.pms.propertymanagement.dto.response.LivenessResponse;
import com.pms.propertymanagement.dto.response.OcrBackResponse;
import com.pms.propertymanagement.dto.response.OcrFrontResponse;
import com.pms.propertymanagement.dto.response.OcrIdResponse;
import com.pms.propertymanagement.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/v1")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/classify/id")
    public ResponseEntity<ClassifyIdResponse> classifyId(@RequestBody ClassifyIdRequest request) {
        return ResponseEntity.ok(aiService.classifyId(request));
    }

    @PostMapping("/card/liveness")
    public ResponseEntity<LivenessResponse> checkLiveness(@RequestBody LivenessRequest request) {
        return ResponseEntity.ok(aiService.checkLiveness(request));
    }

    @PostMapping("/ocr/id/front")
    public ResponseEntity<OcrFrontResponse> ocrFront(@RequestBody OcrFrontRequest request) {
        return ResponseEntity.ok(aiService.ocrFront(request));
    }

    @PostMapping("/ocr/id/back")
    public ResponseEntity<OcrBackResponse> ocrBack(@RequestBody OcrBackRequest request) {
        return ResponseEntity.ok(aiService.ocrBack(request));
    }

    @PostMapping("/ocr/id")
    public ResponseEntity<OcrIdResponse> ocrId(@RequestBody OcrIdRequest request) {
        return ResponseEntity.ok(aiService.ocrId(request));
    }
    
    @PostMapping("/face/compare")
    public ResponseEntity<FaceCompareResponse> faceCompare(@RequestBody FaceCompareRequest request) {
        return ResponseEntity.ok(aiService.faceCompare(request));
    }
    
    @PostMapping("/face/liveness")
    public ResponseEntity<FaceLivenessResponse> faceLiveness(@RequestBody FaceLivenessRequest request) {
        return ResponseEntity.ok(aiService.faceLiveness(request));
    }
    
    @PostMapping("/face/mask")
    public ResponseEntity<FaceMaskResponse> faceMask(@RequestBody FaceMaskRequest request) {
        return ResponseEntity.ok(aiService.faceMask(request));
    }
}
