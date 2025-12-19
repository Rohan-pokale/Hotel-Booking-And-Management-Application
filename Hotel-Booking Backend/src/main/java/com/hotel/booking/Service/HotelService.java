package com.hotel.booking.Service;


import com.hotel.booking.Dto.HotelDto;
import com.hotel.booking.Entity.Booking;
import com.hotel.booking.Entity.Hotel;

public interface HotelService {

    HotelDto createNewHotel(HotelDto hotelDto);

    HotelDto getHotelById(Long id);

    HotelDto updateHOtelByid(HotelDto hotelDto,Long id);

    void deleteHOtelByid(Long id);

    void activateHotel(Long id);
}
