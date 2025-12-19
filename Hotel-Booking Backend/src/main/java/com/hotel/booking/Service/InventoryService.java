package com.hotel.booking.Service;
import com.hotel.booking.Dto.HotelDto;
import com.hotel.booking.Dto.HotelSearchRequest;
import com.hotel.booking.Entity.Room;
import org.springframework.data.domain.Page;


public interface InventoryService {

    void initializeRoomForAYear(Room room);
    void deleteInventory(Room room);

    Page<HotelDto> searchHotels(HotelSearchRequest hotelSearchRequest);
}
