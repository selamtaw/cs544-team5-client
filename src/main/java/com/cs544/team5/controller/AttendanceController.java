package com.cs544.team5.controller;

import com.cs544.team5.domain.CheckInCreationDto;
import com.cs544.team5.domain.CourseReadDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

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

    @RequestMapping("/service-instances/{applicationName}")
    public List<ServiceInstance> serviceInstancesByApplicationName(
            @PathVariable String applicationName) {

        return this.discoveryClient.getInstances(applicationName);
    }


    @PostMapping("/attendance/checkin")
    public ResponseEntity<String> checkIn(@RequestBody CheckInCreationDto checkIn, @RequestHeader Map<String, String> headers) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("studentBarcode", "barcode");
            map.put("classSessionId", "1");

            final HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", headers.get("authorization"));
            final HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
            ResponseEntity<String> response = restTemplate.exchange("http://team5-server/api/v1//record/checkin", HttpMethod.POST, entity, String.class, map);

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
