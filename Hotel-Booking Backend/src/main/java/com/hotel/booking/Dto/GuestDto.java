package com.hotel.booking.Dto;

import com.hotel.booking.Entity.User;
import com.hotel.booking.Entity.enums.Geneder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GuestDto {
    private Long id;
    private User user;
    private String name;
    private Geneder geneder;
    private Integer age;
}
