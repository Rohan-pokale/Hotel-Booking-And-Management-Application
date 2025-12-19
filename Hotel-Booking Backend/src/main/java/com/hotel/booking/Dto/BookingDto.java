package com.hotel.booking.Dto;

import com.hotel.booking.Entity.Guest;
import com.hotel.booking.Entity.Hotel;
import com.hotel.booking.Entity.Room;
import com.hotel.booking.Entity.User;
import com.hotel.booking.Entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class BookingDto {

    private Long id;
//    private Hotel hotel;
//    private Room room;
//    private User user;
    private Integer roomsCount;
    private LocalDate checkInDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate checkOutDate;
    private BookingStatus bookingStatus;
    private Set<Guest> guests;
}
