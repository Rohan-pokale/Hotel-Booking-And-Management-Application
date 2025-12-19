package com.hotel.booking.Service;

import com.hotel.booking.Dto.BookingDto;
import com.hotel.booking.Dto.BookingRequestDto;
import com.hotel.booking.Dto.GuestDto;

import java.util.List;

public interface BookingService{
    BookingDto initialiseBooking(BookingRequestDto bookingRequestDto);


    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);
}
