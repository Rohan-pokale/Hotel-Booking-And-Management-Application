package com.hotel.booking.Service;
import com.hotel.booking.Dto.HotelDto;
import com.hotel.booking.Dto.RoomDto;
import com.hotel.booking.Entity.Hotel;
import org.springframework.stereotype.Service;

import java.util.List;


public interface RoomService {

    RoomDto createNewRoom(Long HotelId,RoomDto roomDto);

    List<RoomDto> getAllRoomsByHotelId(Long HotelId);

    RoomDto getRoomById(Long RoomId);

    void deleteRoomById(Long RoomId);


}
