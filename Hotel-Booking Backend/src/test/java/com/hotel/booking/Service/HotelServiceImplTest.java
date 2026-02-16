package com.hotel.booking.Service;

import com.hotel.booking.Dto.HotelDto;
import com.hotel.booking.Dto.HotelInfoDto;
import com.hotel.booking.Dto.RoomDto;
import com.hotel.booking.Entity.Hotel;
import com.hotel.booking.Entity.Room;
import com.hotel.booking.Entity.User;
import com.hotel.booking.Exception.ResourceNotFoundException;
import com.hotel.booking.Exception.unAuthorizedError;
import com.hotel.booking.Repository.HotelRepository;
import com.hotel.booking.Repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @InjectMocks
    private HotelServiceImpl hotelService;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User owner;

    @BeforeEach
    void setup() {
        owner = User.builder()
                .id(1L)
                .email("owner@mail.com")
                .build();

        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(owner);
    }

    // =========================
    // createNewHotel
    // =========================
    @Test
    void createNewHotel_success() {

        HotelDto hotelDto = HotelDto.builder()
                .name("Test Hotel")
                .build();

        Hotel hotel = Hotel.builder()
                .id(1L)
                .name("Test Hotel")
                .owner(owner)
                .build();

        when(modelMapper.map(hotelDto, Hotel.class)).thenReturn(hotel);
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);
        when(modelMapper.map(hotel, HotelDto.class)).thenReturn(hotelDto);

        HotelDto result = hotelService.createNewHotel(hotelDto);

        assertNotNull(result);
        verify(hotelRepository).save(any());
    }

    // =========================
    // getHotelById - SUCCESS
    // =========================
    @Test
    void getHotelById_success() {

        Hotel hotel = Hotel.builder()
                .id(1L)
                .owner(owner)
                .build();

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(modelMapper.map(hotel, HotelDto.class)).thenReturn(new HotelDto());

        HotelDto result = hotelService.getHotelById(1L);

        assertNotNull(result);
    }

    // =========================
    // getHotelById - UNAUTHORIZED
    // =========================
    @Test
    void getHotelById_unauthorized() {

        User anotherUser = User.builder().id(2L).build();

        Hotel hotel = Hotel.builder()
                .id(1L)
                .owner(anotherUser)
                .build();

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        assertThrows(unAuthorizedError.class,
                () -> hotelService.getHotelById(1L));
    }

    // =========================
    // updateHotelById
    // =========================
    @Test
    void updateHotelById_success() {

        HotelDto hotelDto = HotelDto.builder()
                .name("Updated Hotel")
                .build();

        Hotel hotel = Hotel.builder()
                .id(1L)
                .owner(owner)
                .build();

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        // ✅ FIX 1: void mapping (Dto → Entity)
        doNothing().when(modelMapper).map(hotelDto, hotel);

        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        // ✅ FIX 2: return mapping (Entity → Dto)
        when(modelMapper.map(hotel, HotelDto.class)).thenReturn(hotelDto);

        HotelDto result = hotelService.updateHOtelByid(hotelDto, 1L);

        assertNotNull(result);
        assertEquals("Updated Hotel", result.getName());
    }

    // =========================
    // deleteHotelById
    // =========================
    @Test
    void deleteHotelById_success() {

        Room room = Room.builder().id(1L).build();

        Hotel hotel = Hotel.builder()
                .id(1L)
                .owner(owner)
                .build();

        // ✅ Correct way
        hotel.getRooms().add(room);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        hotelService.deleteHOtelByid(1L);

        verify(inventoryService).deleteInventory(room);
        verify(roomRepository).deleteById(1L);
        verify(hotelRepository).deleteById(1L);
    }

    // =========================
    // activateHotel
    // =========================
    @Test
    void activateHotel_success() {

        Room room = Room.builder().id(1L).build();

        Hotel hotel = Hotel.builder()
                .id(1L)
                .owner(owner)
                .build();

        hotel.getRooms().add(room);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        hotelService.activateHotel(1L);

        assertTrue(hotel.isActive());
        verify(inventoryService).initializeRoomForAYear(room);
    }

    // =========================
    // getHotelInfoById
    // =========================
    @Test
    void getHotelInfoById_success() {

        Room room = Room.builder()
                .id(1L)
                .build();

        Hotel hotel = Hotel.builder()
                .id(1L)
                .rooms(new ArrayList<>())   // IMPORTANT
                .build();

        hotel.getRooms().add(room);

        when(hotelRepository.findById(1L))
                .thenReturn(Optional.of(hotel));

        when(modelMapper.map(any(Hotel.class), eq(HotelDto.class)))
                .thenReturn(HotelDto.builder().build());

        when(modelMapper.map(any(Room.class), eq(RoomDto.class)))
                .thenReturn(RoomDto.builder().build()); // ✅ FIXED

        HotelInfoDto result = hotelService.getHotelInfoById(1L);

        assertNotNull(result);
        assertEquals(1, result.getRooms().size());
    }


    // =========================
    // getHotelInfoById - NOT FOUND
    // =========================
    @Test
    void getHotelInfoById_notFound() {

        when(hotelRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> hotelService.getHotelInfoById(1L));
    }
}
