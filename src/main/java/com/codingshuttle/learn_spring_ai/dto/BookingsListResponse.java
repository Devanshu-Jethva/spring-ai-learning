package com.codingshuttle.learn_spring_ai.dto;

import java.util.List;

public record BookingsListResponse(List<BookingResponse> bookings, String message) {
}
