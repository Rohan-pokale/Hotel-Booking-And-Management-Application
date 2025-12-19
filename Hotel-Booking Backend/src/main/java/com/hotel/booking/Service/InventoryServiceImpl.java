package com.hotel.booking.Service;

import com.hotel.booking.Dto.HotelSearchRequest;
import com.hotel.booking.Entity.Inventory;
import com.hotel.booking.Entity.Room;
import com.hotel.booking.Repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService{

    private final InventoryRepository inventoryRepository;

    @Override
    public void initializeRoomForAYear(Room room) {
        LocalDate today=LocalDate.now();
        LocalDate endDate=today.plusYears(1);

        for(;!today.isAfter(endDate);today=today.plusDays(1)){
            Inventory inventory=Inventory
                    .builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .city(room.getHotel().getCity())
                    .bookedCount(0)
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .closed(false)
                    .totalCount(room.getTotalCount())
                    .build();

            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deleteInventory(Room room) {
            inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page searchHotels(HotelSearchRequest hotelSearchRequest) {
        Pageable pageable= PageRequest.of(hotelSearchRequest.getPage(),hotelSearchRequest.getSize());

    }
}
