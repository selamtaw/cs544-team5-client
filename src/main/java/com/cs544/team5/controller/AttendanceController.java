package com.cs544.team5.controller;

import com.cs544.team5.domain.CheckInCreationDto;
import com.cs544.team5.domain.CourseReadDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
class AttendanceController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebClient.Builder webClientBuilder;


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


    @RequestMapping("/service-instances/{applicationName}")
    public List<ServiceInstance> serviceInstancesByApplicationName(
            @PathVariable String applicationName) {

        return this.discoveryClient.getInstances(applicationName);
    }


    @PostMapping("/attendance/checkinX")
    public ResponseEntity<String> checkIn(@RequestBody CheckInCreationDto checkIn, @RequestHeader Map<String, String> headers) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("studentBarcode", "barcode");
            map.put("classSessionId", "1");

            final HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", headers.get("authorization"));
            final HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
            ResponseEntity<String> response = restTemplate.exchange("http://team5-server/api/v1/record/checkin", HttpMethod.POST, entity, String.class, map);

//            ResponseEntity<String> response = restTemplate.exchange("http://team5-server/record/checkin", map, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Checkin Successful");
            } else {
                System.out.println("Checkin Failed");
            }
            return response;

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/test/course")
    public ResponseEntity<CourseReadDto> findCourse() {
        try {
            CourseReadDto response = restTemplate.getForObject("http://team5-server/courses/1", CourseReadDto.class);
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}
