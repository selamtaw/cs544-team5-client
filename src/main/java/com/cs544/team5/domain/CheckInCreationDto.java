package com.cs544.team5.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.Valid;

@Data
@AllArgsConstructor
public class CheckInCreationDto {
    @Valid
    private String studentBarcode;

    @Valid
    private Integer classSessionId;
}
