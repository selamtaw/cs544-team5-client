package com.cs544.team5.controller;

import com.cs544.team5.domain.CheckInCreationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
class AttendanceController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${api.server.name}")
    private String serverName;

    @PostMapping(value = "/attendance/checkin", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> create(@RequestBody CheckInCreationDto checkIn, @RequestHeader Map<String, String> headers) {
        Map<String, String> map = new HashMap<>();
        map.put("studentBarcode", checkIn.getStudentBarcode());
        map.put("classSessionId", checkIn.getClassSessionId().toString());

        return WebClient.create(getServerUrl()).post().uri("/api/v1/record/checkin").headers(httpHeaders -> {
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            httpHeaders.add(HttpHeaders.AUTHORIZATION, headers.get("authorization"));
        }).body(Mono.just(map), Map.class).retrieve().bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(100)));
    }

    private String getServerUrl() {
        return this.discoveryClient.getInstances(serverName).get(0).getUri().toString();
    }
}
