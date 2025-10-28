package com.example.chatservice.service;

public class NotFoundException extends RuntimeException {
  public NotFoundException(String code) {
    super(code);
  }
}
