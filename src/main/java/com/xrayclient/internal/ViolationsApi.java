package com.xrayclient.internal;

import com.xrayclient.model.violations.ViolationFilter;
import com.xrayclient.model.violations.ViolationsResponse;
import lombok.RequiredArgsConstructor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public final class ViolationsApi {

    private static final String PATH = "/xray/api/v1/violations";

    private final XrayHttpClient http;

    public ViolationsResponse getViolations(
            ViolationFilter filter,
            String projectKey,
            int pageNum,
            int pageSize
    ) {
        String query = buildQuery(projectKey, pageNum, pageSize);
        ViolationsRequest body = new ViolationsRequest(filter);
        return http.post(PATH + query, body, ViolationsResponse.class);
    }

    private String buildQuery(String projectKey, int pageNum, int pageSize) {
        StringBuilder sb = new StringBuilder("?");
        sb.append("num_of_rows=").append(pageSize);
        sb.append("&page_num=").append(pageNum);
        sb.append("&direction=asc");
        sb.append("&order_by=updated");
        if (projectKey != null && !projectKey.isBlank()) {
            sb.append("&projectKey=").append(URLEncoder.encode(projectKey, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
