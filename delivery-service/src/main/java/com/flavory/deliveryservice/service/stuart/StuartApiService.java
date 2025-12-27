package com.flavory.deliveryservice.service.stuart;

import com.flavory.deliveryservice.dto.request.StuartJobRequest;
import com.flavory.deliveryservice.dto.response.StuartJobResponse;

public interface StuartApiService {
    StuartJobResponse createJob(StuartJobRequest request);
    String getAccessToken();
}
