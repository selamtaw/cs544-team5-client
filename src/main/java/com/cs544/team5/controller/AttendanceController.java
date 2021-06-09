package com.cs544.team5.controller;

import com.cs544.team5.domain.CheckIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;

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
        WebClient.UriSpec<WebClient.RequestBodySpec> uriSpec = webClientBuilder.build().post();
        WebClient.RequestBodySpec bodySpec = uriSpec.uri("/api/v1/course-offering/");
        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec.body(
                Mono.just(new CheckIn("barcode")), CheckIn.class);

        WebClient.ResponseSpec responseSpec = headersSpec.header(
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                .acceptCharset(StandardCharsets.UTF_8)
                .ifNoneMatch("*")
                .ifModifiedSince(ZonedDateTime.now())
                .retrieve();

        Mono<String> responseObj = headersSpec.exchangeToMono(response -> {
            if (response.statusCode()
                    .equals(HttpStatus.OK)) {
                return response.bodyToMono(String.class);
            } else if (response.statusCode()
                    .is4xxClientError()) {
                return Mono.just("Error response");
            } else {
                return response.createException()
                        .flatMap(Mono::error);
            }
        });
        return this.discoveryClient.getInstances(applicationName);
    }


    @PostMapping("/attendance/")
    public ResponseEntity<String> checkIn(@RequestBody CheckIn checkIn){
        return ResponseEntity.ok("Checkin Successful!");
    }
}