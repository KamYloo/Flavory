package com.flavory.deliveryservice.service.stuart;

import com.flavory.deliveryservice.dto.request.StuartJobRequest;
import com.flavory.deliveryservice.dto.external.StuartJobResponse;

public interface StuartApiService {
    StuartJobResponse createJob(StuartJobRequest request);
    StuartJobResponse getJob(Long jobId);
    void cancelJob(Long jobId);
    String getAccessToken();
}
