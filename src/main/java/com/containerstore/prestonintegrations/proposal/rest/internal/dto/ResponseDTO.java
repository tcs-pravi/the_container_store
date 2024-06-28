package com.containerstore.prestonintegrations.proposal.rest.internal.dto;

import org.springframework.http.HttpStatus;

public record ResponseDTO<T>(HttpStatus status,int code,String message,T data) {
}
