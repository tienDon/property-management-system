package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.request.ClassifyIdRequest;
import com.pms.propertymanagement.dto.request.LivenessRequest;
import com.pms.propertymanagement.dto.response.ClassifyIdResponse;
import com.pms.propertymanagement.dto.response.LivenessResponse;
import com.pms.propertymanagement.service.AiService;

import com.pms.propertymanagement.utils.VnptErrorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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
public class AiServiceImpl implements AiService {

    // Hardcoded credentials as per FileUploadService usage, ideally should be in properties
    private final String accessToken = "bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0cmFuc2FjdGlvbl9pZCI6ImQyODEwOGU5LTQ1NDctNDUzMC1hMWIxLTY3N2NiMGQ3Yzc1YyIsInN1YiI6ImM4MDgyODNiLTE4OTQtMTFmMS05ZGUwLWQ1NmJlYjFhODE2NyIsImF1ZCI6WyJyZXN0c2VydmljZSJdLCJ1c2VyX25hbWUiOiJsa2lldDI0MDQuMjAwNUBnbWFpbC5jb20iLCJzY29wZSI6WyJyZWFkIl0sImlzcyI6Imh0dHBzOi8vbG9jYWxob3N0IiwibmFtZSI6ImxraWV0MjQwNC4yMDA1QGdtYWlsLmNvbSIsImV4cCI6MTc3Mjg1MzI5MywidXVpZF9hY2NvdW50IjoiYzgwODI4M2ItMTg5NC0xMWYxLTlkZTAtZDU2YmViMWE4MTY3IiwiYXV0aG9yaXRpZXMiOlsiVVNFUiJdLCJqdGkiOiIxY2Q5NTRmYi00YzE2LTRlMzUtYWNmYi03MTkxMzM4YjcwNDIiLCJjbGllbnRfaWQiOiJjbGllbnRhcHAifQ.d2VM9OVIJyRDi66i1ft8Q8vEopOebaPEt0Vw8h_QdaOh3IrPsCK1REQnsBCMx7tscuAfYL3Gt3ilv_IMZRsjQusab2EyP1wCLiAI6Al2t02IdsA-kP3YevtaNBRR3YByKNpgxRM__D-WMiDcQiXsBvinFnGh3u1FR6c2F12feq5hFFAk6ZHJ46zBlvNJi3-cpw5loUq30lAxYqCkspfWQtdAI4n55O26BmayYaCL6p2ZSBa4tBNLdPvyVkqg88VzPkqdZR2zTBdiHxNLdpxzauGYQsEcph0yUFAw579a3OBThLgWH1lvpdMPbRi2PuMRUqYtWyzffZaZWY8EtrpOaQ";
    private final String tokenId ="4c47abab-e520-5bd3-e063-62199f0a4340";
    private final String tokenKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIe1ZktuE0lsKFahFXh7h41LAdo3sXKOh2UO8YVynJer8zDM+FRsat0skgJUqRIzuzoRAWCYrFk0/5nKfsqVsFUCAwEAAQ==";
    private final String macAddress = "TEST1";
    
    private final String BASE_URL = "https://api.idg.vnpt.vn";

    @Override
    public ClassifyIdResponse classifyId(ClassifyIdRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHeaders();
        
        HttpEntity<ClassifyIdRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<ClassifyIdResponse> response = restTemplate.postForEntity(
                BASE_URL + "/ai/v1/classify/id",
                entity,
                ClassifyIdResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            // Log error and return null or throw custom exception
            // For now, rethrow or let global handler handle it
            throw e;
        }
    }

    @Override
    public LivenessResponse checkLiveness(LivenessRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHeaders();
        
        HttpEntity<LivenessRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<LivenessResponse> response = restTemplate.postForEntity(
                BASE_URL + "/ai/v1/card/liveness",
                entity,
                LivenessResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw e;
        }
    }
    
    @Override
    public OcrFrontResponse ocrFront(OcrFrontRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHeaders();
        
        HttpEntity<OcrFrontRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<OcrFrontResponse> response = restTemplate.postForEntity(
                BASE_URL + "/ai/v1/ocr/id/front",
                entity,
                OcrFrontResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public OcrBackResponse ocrBack(OcrBackRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHeaders();
        
        HttpEntity<OcrBackRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<OcrBackResponse> response = restTemplate.postForEntity(
                BASE_URL + "/ai/v1/ocr/id/back",
                entity,
                OcrBackResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public OcrIdResponse ocrId(OcrIdRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHeaders();
        
        HttpEntity<OcrIdRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<OcrIdResponse> response = restTemplate.postForEntity(
                BASE_URL + "/ai/v1/ocr/id",
                entity,
                OcrIdResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException(VnptErrorUtil.extractErrorMessage(e));
        } catch (Exception e) {
            throw e;
        }
    }
    
    @Override
    public FaceCompareResponse faceCompare(FaceCompareRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHeaders();
        
        HttpEntity<FaceCompareRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<FaceCompareResponse> response = restTemplate.postForEntity(
                BASE_URL + "/ai/v1/face/compare",
                entity,
                FaceCompareResponse.class
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
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHeaders();
        
        HttpEntity<FaceLivenessRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<FaceLivenessResponse> response = restTemplate.postForEntity(
                BASE_URL + "/ai/v1/face/liveness",
                entity,
                FaceLivenessResponse.class
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
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHeaders();
        
        HttpEntity<FaceMaskRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<FaceMaskResponse> response = restTemplate.postForEntity(
                BASE_URL + "/ai/v1/face/mask",
                entity,
                FaceMaskResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public FaceAddResponse faceAdd(FaceAddRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHeaders();
        
        HttpEntity<FaceAddRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<FaceAddResponse> response = restTemplate.postForEntity(
                BASE_URL + "/face-service/face/add",
                entity,
                FaceAddResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public FaceVerifyResponse faceVerify(FaceVerifyRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHeaders();
        
        HttpEntity<FaceVerifyRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<FaceVerifyResponse> response = restTemplate.postForEntity(
                BASE_URL + "/face-service/face/verify",
                entity,
                FaceVerifyResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public FaceSearchResponse faceSearch(FaceSearchRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHeaders();
        
        HttpEntity<FaceSearchRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<FaceSearchResponse> response = restTemplate.postForEntity(
                BASE_URL + "/face-service/face/search",
                entity,
                FaceSearchResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public FaceSearchKResponse faceSearchK(FaceSearchKRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHeaders();
        
        HttpEntity<FaceSearchKRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<FaceSearchKResponse> response = restTemplate.postForEntity(
                BASE_URL + "/face-service/face/search-k",
                entity,
                FaceSearchKResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw e;
        }
    }
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String raw = accessToken == null ? "" : accessToken.trim();
        String token = raw.toLowerCase().startsWith("bearer ") ? raw.substring(7).trim() : raw;
        headers.setBearerAuth(token);
        headers.set("Token-id", tokenId);
        headers.set("Token-key", tokenKey);
        headers.set("mac-address", macAddress);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
