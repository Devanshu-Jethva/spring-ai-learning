package com.codingshuttle.learn_spring_ai.dto;

import com.codingshuttle.learn_spring_ai.entity.BookingStatus;

import java.time.Instant;

public record BookingResponse(Long id, String destination, Instant departureTime, BookingStatus status) {
}