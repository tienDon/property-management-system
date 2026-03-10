package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.request.ClassifyIdRequest;
import com.pms.propertymanagement.dto.request.LivenessRequest;
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
import com.pms.propertymanagement.dto.response.ClassifyIdResponse;
import com.pms.propertymanagement.dto.response.FaceAddResponse;
import com.pms.propertymanagement.dto.response.FaceCompareResponse;
import com.pms.propertymanagement.dto.response.FaceLivenessResponse;
import com.pms.propertymanagement.dto.response.FaceMaskResponse;
import com.pms.propertymanagement.dto.response.FaceSearchKResponse;
import com.pms.propertymanagement.dto.response.FaceSearchResponse;
import com.pms.propertymanagement.dto.response.FaceVerifyResponse;
import com.pms.propertymanagement.dto.response.LivenessResponse;
import com.pms.propertymanagement.dto.response.OcrBackResponse;
import com.pms.propertymanagement.dto.response.OcrFrontResponse;
import com.pms.propertymanagement.dto.response.OcrIdResponse;

public interface AiService {
    ClassifyIdResponse classifyId(ClassifyIdRequest request);
    LivenessResponse checkLiveness(LivenessRequest request);
    OcrFrontResponse ocrFront(OcrFrontRequest request);
    OcrBackResponse ocrBack(OcrBackRequest request);
    OcrIdResponse ocrId(OcrIdRequest request);
    
    FaceCompareResponse faceCompare(FaceCompareRequest request);
    FaceLivenessResponse faceLiveness(FaceLivenessRequest request);
    FaceMaskResponse faceMask(FaceMaskRequest request);
    
    FaceAddResponse faceAdd(FaceAddRequest request);
    FaceVerifyResponse faceVerify(FaceVerifyRequest request);
    FaceSearchResponse faceSearch(FaceSearchRequest request);
    FaceSearchKResponse faceSearchK(FaceSearchKRequest request);
}
