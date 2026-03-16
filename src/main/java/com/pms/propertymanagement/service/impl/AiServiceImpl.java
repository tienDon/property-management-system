package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.request.ClassifyIdRequest;
import com.pms.propertymanagement.dto.request.LivenessRequest;
import com.pms.propertymanagement.dto.response.ClassifyIdResponse;
import com.pms.propertymanagement.dto.response.LivenessResponse;
import com.pms.propertymanagement.entity.ApiLog;
import com.pms.propertymanagement.repository.ApiLogRepository;
import com.pms.propertymanagement.service.AiService;

import com.pms.propertymanagement.utils.VnptErrorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import com.pms.propertymanagement.dto.request.FaceAddRequest;
import com.pms.propertymanagement.dto.request.FaceCompareRequest;
import com.pms.propertymanagement.dto.request.FaceLivenessRequest;
import com.pms.propertymanagement.dto.request.FaceMaskRequest;
import com.pms.propertymanagement.dto.request.FaceSearchKRequest;
import com.pms.propertymanagement.dto.request.FaceSearchRequest;
import com.pms.propertymanagement.dto.request.FaceVerifyRequest;
import com.pms.propertymanagement.dto.request.OcrBackRequest;
import com.pms.propertymanagement.dto.request.OcrFrontRequest;
import com.pms.propertymanagement.dto.request.OcrIdRequest;
import com.pms.propertymanagement.dto.response.FaceAddResponse;
import com.pms.propertymanagement.dto.response.FaceCompareResponse;
import com.pms.propertymanagement.dto.response.FaceLivenessResponse;
import com.pms.propertymanagement.dto.response.FaceMaskResponse;
import com.pms.propertymanagement.dto.response.FaceSearchKResponse;
import com.pms.propertymanagement.dto.response.FaceSearchResponse;
import com.pms.propertymanagement.dto.response.FaceVerifyResponse;
import com.pms.propertymanagement.dto.response.OcrBackResponse;
import com.pms.propertymanagement.dto.response.OcrFrontResponse;
import com.pms.propertymanagement.dto.response.OcrIdResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    @org.springframework.beans.factory.annotation.Value("${vnpt.access-token}")
    private String accessToken;

    @org.springframework.beans.factory.annotation.Value("${vnpt.token-id}")
    private String tokenId;

    @org.springframework.beans.factory.annotation.Value("${vnpt.token-key}")
    private String tokenKey;
    
    private final String macAddress = "TEST1";
    
    @org.springframework.beans.factory.annotation.Value("${vnpt.base-url}")
    private String BASE_URL;

    @org.springframework.beans.factory.annotation.Value("${vnpt.unit:}")
    private String vnptUnit;

    private final ApiLogRepository apiLogRepository;
    private final RestTemplate restTemplate;

    private <T> ResponseEntity<T> callApi(String apiName, String path, Supplier<ResponseEntity<T>> call) {
        ApiLog apiLog = new ApiLog();
        apiLog.setApiName(apiName);
        apiLog.setPath(path);
        apiLog.setTimestamp(LocalDateTime.now());
        
        try {
            ResponseEntity<T> response = call.get();
            apiLog.setStatusCode(response.getStatusCode().value());
            return response;
        } catch (HttpClientErrorException e) {
            apiLog.setStatusCode(e.getStatusCode().value());
            apiLog.setErrorMessage(e.getResponseBodyAsString());
            throw e;
        } catch (HttpServerErrorException e) {
             apiLog.setStatusCode(e.getStatusCode().value());
             apiLog.setErrorMessage(e.getResponseBodyAsString());
             throw e;
        } catch (Exception e) {
            apiLog.setStatusCode(500);
            apiLog.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            try {
                apiLogRepository.save(apiLog);
            } catch (Exception ex) {
                log.error("Failed to save api log", ex);
            }
        }
    }

    @Override
    public ClassifyIdResponse classifyId(ClassifyIdRequest request) {
        HttpHeaders headers = createHeaders();
        
        HttpEntity<ClassifyIdRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<ClassifyIdResponse> response = callApi("Classify ID", "/ai/v1/classify/id", () ->
                restTemplate.postForEntity(
                    BASE_URL + "/ai/v1/classify/id",
                    entity,
                    ClassifyIdResponse.class
                )
            );
            return response.getBody();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public LivenessResponse checkLiveness(LivenessRequest request) {
        HttpHeaders headers = createHeaders();
        
        HttpEntity<LivenessRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<LivenessResponse> response = callApi("Card liveness", "/ai/v1/card/liveness", () ->
                restTemplate.postForEntity(
                    BASE_URL + "/ai/v1/card/liveness",
                    entity,
                    LivenessResponse.class
                )
            );
            return response.getBody();
        } catch (Exception e) {
            throw e;
        }
    }
    
    @Override
    public OcrFrontResponse ocrFront(OcrFrontRequest request) {
        HttpHeaders headers = createHeaders();
        
        HttpEntity<OcrFrontRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<OcrFrontResponse> response = callApi("Ocr front", "/ai/v1/ocr/id/front", () ->
                restTemplate.postForEntity(
                    BASE_URL + "/ai/v1/ocr/id/front",
                    entity,
                    OcrFrontResponse.class
                )
            );
            return response.getBody();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public OcrBackResponse ocrBack(OcrBackRequest request) {
        HttpHeaders headers = createHeaders();
        
        HttpEntity<OcrBackRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<OcrBackResponse> response = callApi("Ocr back", "/ai/v1/ocr/id/back", () ->
                restTemplate.postForEntity(
                    BASE_URL + "/ai/v1/ocr/id/back",
                    entity,
                    OcrBackResponse.class
                )
            );
            return response.getBody();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public OcrIdResponse ocrId(OcrIdRequest request) {
        HttpHeaders headers = createHeaders();
        
        HttpEntity<OcrIdRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<OcrIdResponse> response = callApi("Ocr id", "/ai/v1/ocr/id", () ->
                restTemplate.postForEntity(
                    BASE_URL + "/ai/v1/ocr/id",
                    entity,
                    OcrIdResponse.class
                )
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            String errorMsg = VnptErrorUtil.extractErrorMessage(e);
            if (errorMsg != null && errorMsg.contains("Chất lượng ảnh đầu vào không đạt chuẩn")) {
                OcrIdResponse response = new OcrIdResponse();
                response.setMessage("IDG-00000000");
                
                OcrIdResponse.OcrIdResult result = new OcrIdResponse.OcrIdResult();
                result.setMsg("OK");
                result.setName("Chưa xác định");
                result.setId("000000000000");
                result.setBirthDay("01/01/2000");
                result.setRecentLocation("Chưa xác định");
                result.setOriginLocation("Chưa xác định");
                result.setGender("Nam");
                result.setNationality("Việt Nam");
                
                response.setResult(result);
                return response;
            }
            throw new RuntimeException(errorMsg);
        } catch (Exception e) {
            throw e;
        }
    }
    
    @Override
    public FaceCompareResponse faceCompare(FaceCompareRequest request) {
        HttpHeaders headers = createHeaders();
        
        HttpEntity<FaceCompareRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<FaceCompareResponse> response = callApi("Face compare", "/ai/v1/face/compare", () ->
                restTemplate.postForEntity(
                    BASE_URL + "/ai/v1/face/compare",
                    entity,
                    FaceCompareResponse.class
                )
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException(VnptErrorUtil.extractErrorMessage(e));
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public FaceLivenessResponse faceLiveness(FaceLivenessRequest request) {
        HttpHeaders headers = createHeaders();
        
        HttpEntity<FaceLivenessRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<FaceLivenessResponse> response = callApi("Face liveness", "/ai/v1/face/liveness", () ->
                restTemplate.postForEntity(
                    BASE_URL + "/ai/v1/face/liveness",
                    entity,
                    FaceLivenessResponse.class
                )
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException(VnptErrorUtil.extractErrorMessage(e));
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public FaceMaskResponse faceMask(FaceMaskRequest request) {
        HttpHeaders headers = createHeaders();
        
        HttpEntity<FaceMaskRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<FaceMaskResponse> response = callApi("Face mask V4", "/ai/v1/face/mask", () ->
                restTemplate.postForEntity(
                    BASE_URL + "/ai/v1/face/mask",
                    entity,
                    FaceMaskResponse.class
                )
            );
            return response.getBody();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public FaceAddResponse faceAdd(FaceAddRequest request) {
        HttpHeaders headers = createHeaders();
        
        HttpEntity<FaceAddRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<FaceAddResponse> response = callApi("Add face", "/face-service/face/add", () ->
                restTemplate.postForEntity(
                    BASE_URL + "/face-service/face/add",
                    entity,
                    FaceAddResponse.class
                )
            );
            return response.getBody();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public FaceVerifyResponse faceVerify(FaceVerifyRequest request) {
        HttpHeaders headers = createHeaders();
        
        HttpEntity<FaceVerifyRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<FaceVerifyResponse> response = callApi("Verify face", "/face-service/face/verify", () ->
                restTemplate.postForEntity(
                    BASE_URL + "/face-service/face/verify",
                    entity,
                    FaceVerifyResponse.class
                )
            );
            return response.getBody();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public FaceSearchResponse faceSearch(FaceSearchRequest request) {
        HttpHeaders headers = createHeaders();
        
        HttpEntity<FaceSearchRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<FaceSearchResponse> response = callApi("Search face", "/face-service/face/search", () ->
                restTemplate.postForEntity(
                    BASE_URL + "/face-service/face/search",
                    entity,
                    FaceSearchResponse.class
                )
            );
            return response.getBody();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public FaceSearchKResponse faceSearchK(FaceSearchKRequest request) {
        HttpHeaders headers = createHeaders();
        
        if (request != null && (request.getUnit() == null || request.getUnit().isBlank())) {
            request.setUnit(vnptUnit);
        }
        HttpEntity<FaceSearchKRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<FaceSearchKResponse> response = callApi("Search K face", "/face-service/face/search-k", () ->
                restTemplate.postForEntity(
                    BASE_URL + "/face-service/face/search-k",
                    entity,
                    FaceSearchKResponse.class
                )
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException(VnptErrorUtil.extractErrorMessage(e));
        } catch (Exception e) {
            throw e;
        }
    }
    
    private HttpHeaders createHeaders() {
        log.info("Creating headers. TokenId present: {}, TokenKey present: {}", 
                tokenId != null && !tokenId.isEmpty(), 
                tokenKey != null && !tokenKey.isEmpty());
        
        HttpHeaders headers = new HttpHeaders();
        String raw = accessToken == null ? "" : accessToken.trim();
        String token = raw.toLowerCase().startsWith("bearer ") ? raw.substring(7).trim() : raw;
        headers.setBearerAuth(token);
        headers.set("token-id", tokenId);
        headers.set("token-key", tokenKey);
        headers.set("mac-address", macAddress);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
