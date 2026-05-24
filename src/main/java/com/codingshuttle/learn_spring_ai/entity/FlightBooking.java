package com.codingshuttle.learn_spring_ai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FlightBooking {

    String destination;

    Instant departureTime;

    @Enumerated(EnumType.STRING)
    BookingStatus bookingStatus;

    @CreationTimestamp
    Instant bookedAt;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

}


