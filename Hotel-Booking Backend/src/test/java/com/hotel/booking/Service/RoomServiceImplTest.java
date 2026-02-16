package com.hotel.booking.Service;

import com.hotel.booking.Dto.RoomDto;
import com.hotel.booking.Entity.Hotel;
import com.hotel.booking.Entity.Room;
import com.hotel.booking.Entity.User;
import com.hotel.booking.Exception.ResourceNotFoundException;
import com.hotel.booking.Exception.unAuthorizedError;
import com.hotel.booking.Repository.HotelRepository;
import com.hotel.booking.Repository.RoomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private RoomServiceImpl roomService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ------------------------------------------------
    // createNewRoom
    // ------------------------------------------------
    @Test
    void createNewRoom_success_whenHotelActive() {

        User owner = User.builder().id(1L).build();
        setAuthenticatedUser(owner);

        Hotel hotel = Hotel.builder()
                .id(10L)
                .owner(owner)
                .isActive(true)
                .build();

        RoomDto roomDto = RoomDto.builder()
                .type("DELUXE")
                .basePrice(BigDecimal.valueOf(3000))
                .totalCount(5)
                .capacity(2)
                .build();


        Room roomEntity = Room.builder().id(100L).hotel(hotel).build();

        when(hotelRepository.findById(10L)).thenReturn(Optional.of(hotel));
        when(modelMapper.map(roomDto, Room.class)).thenReturn(roomEntity);
        when(roomRepository.save(roomEntity)).thenReturn(roomEntity);
        when(modelMapper.map(roomEntity, RoomDto.class)).thenReturn(roomDto);

        RoomDto result = roomService.createNewRoom(10L, roomDto);

        assertNotNull(result);
        verify(inventoryService).initializeRoomForAYear(roomEntity);
        verify(roomRepository).save(roomEntity);
    }

    @Test
    void createNewRoom_unauthorized() {

        User owner = User.builder().id(1L).build();
        User otherUser = User.builder().id(2L).build();
        setAuthenticatedUser(otherUser);

        Hotel hotel = Hotel.builder()
                .id(10L)
                .owner(owner)
                .build();

        when(hotelRepository.findById(10L)).thenReturn(Optional.of(hotel));

        assertThrows(
                unAuthorizedError.class,
                () -> roomService.createNewRoom(10L, RoomDto.builder().build())
        );
    }

    // ------------------------------------------------
    // getAllRoomsByHotelId
    // ------------------------------------------------
    @Test
    void getAllRoomsByHotelId_success() {

        User owner = User.builder().id(1L).build();
        setAuthenticatedUser(owner);

        Room room = Room.builder().id(1L).build();
        Hotel hotel = Hotel.builder()
                .id(10L)
                .owner(owner)
                .rooms(List.of(room))
                .build();

        when(hotelRepository.findById(10L)).thenReturn(Optional.of(hotel));
        when(modelMapper.map(room, RoomDto.class))
                .thenReturn(RoomDto.builder().id(1L).build());

        List<RoomDto> result = roomService.getAllRoomsByHotelId(10L);

        assertEquals(1, result.size());
        verify(hotelRepository).findById(10L);
    }

    // ------------------------------------------------
    // getRoomById
    // ------------------------------------------------
    @Test
    void getRoomById_success() {

        Room room = Room.builder().id(5L).build();
        RoomDto roomDto = RoomDto.builder().id(5L).build();

        when(roomRepository.findById(5L)).thenReturn(Optional.of(room));
        when(modelMapper.map(room, RoomDto.class)).thenReturn(roomDto);

        RoomDto result = roomService.getRoomById(5L);

        assertEquals(5L, result.getId());
    }

    @Test
    void getRoomById_notFound() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roomService.getRoomById(99L)
        );
    }

    // ------------------------------------------------
    // deleteRoomById
    // ------------------------------------------------
    @Test
    void deleteRoomById_success() {

        User owner = User.builder().id(1L).build();
        setAuthenticatedUser(owner);

        Hotel hotel = Hotel.builder().id(10L).owner(owner).build();
        Room room = Room.builder().id(5L).hotel(hotel).build();

        when(roomRepository.findById(5L)).thenReturn(Optional.of(room));

        roomService.deleteRoomById(5L);

        verify(inventoryService).deleteInventory(room);
        verify(roomRepository).deleteById(5L);
    }

    @Test
    void deleteRoomById_unauthorized() {

        User owner = User.builder().id(1L).build();
        User otherUser = User.builder().id(2L).build();
        setAuthenticatedUser(otherUser);

        Hotel hotel = Hotel.builder().id(10L).owner(owner).build();
        Room room = Room.builder().id(5L).hotel(hotel).build();

        when(roomRepository.findById(5L)).thenReturn(Optional.of(room));

        assertThrows(
                unAuthorizedError.class,
                () -> roomService.deleteRoomById(5L)
        );
    }

    // ------------------------------------------------
    // Helper
    // ------------------------------------------------
    private void setAuthenticatedUser(User user) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
