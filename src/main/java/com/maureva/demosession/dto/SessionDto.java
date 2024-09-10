package com.maureva.demosession.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SessionDto {

    private String username;
    private String sessionId;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime creationDate;
    private String remoteAddress;

}
