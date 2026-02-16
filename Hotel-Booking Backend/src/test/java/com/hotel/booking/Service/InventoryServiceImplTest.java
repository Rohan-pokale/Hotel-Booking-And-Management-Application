package com.hotel.booking.Service;

import com.hotel.booking.Dto.HotelPriceDto;
import com.hotel.booking.Dto.HotelSearchRequest;
import com.hotel.booking.Entity.Hotel;
import com.hotel.booking.Entity.Inventory;
import com.hotel.booking.Entity.Room;
import com.hotel.booking.Repository.HotelMinPriceRepositoy;
import com.hotel.booking.Repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private HotelMinPriceRepositoy hotelMinPriceRepositoy;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    // ------------------------------------------------
    // initializeRoomForAYear
    // ------------------------------------------------
    @Test
    void initializeRoomForAYear_success() {

        Hotel hotel = Hotel.builder()
                .id(1L)
                .city("Pune")
                .build();

        Room room = Room.builder()
                .id(101L)
                .hotel(hotel)
                .basePrice(BigDecimal.valueOf(3000))
                .totalCount(5)
                .build();

        inventoryService.initializeRoomForAYear(room);

        // roughly 365–366 entries should be saved
        verify(inventoryRepository, atLeast(365))
                .save(any(Inventory.class));
    }

    // ------------------------------------------------
    // deleteInventory
    // ------------------------------------------------
    @Test
    void deleteInventory_success() {

        Room room = Room.builder()
                .id(101L)
                .build();

        inventoryService.deleteInventory(room);

        verify(inventoryRepository).deleteByRoom(room);
    }

    // ------------------------------------------------
    // searchHotels
    // ------------------------------------------------
    @Test
    void searchHotels_success() {

        HotelSearchRequest request = HotelSearchRequest.builder()
                .city("Pune")
                .startDate(LocalDate.of(2026, 1, 10))
                .endDate(LocalDate.of(2026, 1, 12))
                .roomCount(2)
                .page(0)
                .size(5)
                .build();

        Hotel hotel = Hotel.builder()
                .id(1L)
                .name("Taj Hotel")
                .city("Pune")
                .build();

        HotelPriceDto hotelPriceDto = HotelPriceDto.builder()
                .hotel(hotel)
                .price(4500.0)
                .build();

        Page<HotelPriceDto> mockPage =
                new PageImpl<>(List.of(hotelPriceDto));

        when(hotelMinPriceRepositoy.findHotelsByAvailabelInventory(
                eq("Pune"),
                eq(request.getStartDate()),
                eq(request.getEndDate()),
                eq(2),
                eq(3L), // inclusive days (10,11,12)
                any(Pageable.class)
        )).thenReturn(mockPage);

        Page<HotelPriceDto> result =
                inventoryService.searchHotels(request);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(
                4500.0,
                result.getContent().get(0).getPrice()
        );
        assertEquals(
                "Taj Hotel",
                result.getContent().get(0).getHotel().getName()
        );
    }
}
