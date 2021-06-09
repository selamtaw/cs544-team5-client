package com.cs544.team5.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class CheckInCreationDto {
    @Valid
    @NotNull
    private String studentBarcode;

    @Valid
    @NotNull
    private Integer classSessionId;
}
