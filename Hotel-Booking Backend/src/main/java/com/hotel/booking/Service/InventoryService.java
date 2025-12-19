package com.hotel.booking.Service;

import com.hotel.booking.Dto.HotelSearchRequest;
import com.hotel.booking.Entity.Room;
import org.hibernate.query.Page;

public interface InventoryService {

    void initializeRoomForAYear(Room room);
    void deleteInventory(Room room);

    Page searchHotels(HotelSearchRequest hotelSearchRequest);
}
