package com.hotel.booking.Service;

import com.hotel.booking.Dto.BookingDto;
import com.hotel.booking.Dto.BookingRequestDto;
import com.hotel.booking.Dto.GuestDto;
import com.hotel.booking.Entity.*;
import com.hotel.booking.Entity.enums.BookingStatus;
import com.hotel.booking.Exception.ResourceNotFoundException;
import com.hotel.booking.Exception.unAuthorizedError;
import com.hotel.booking.Repository.*;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User user;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .email("test@mail.com")
                .build();

        SecurityContextHolder.setContext(securityContext);

        lenient().when(securityContext.getAuthentication())
                .thenReturn(authentication);

        lenient().when(authentication.getPrincipal())
                .thenReturn(user);
    }

    // ===========================
    // initialiseBooking - SUCCESS
    // ===========================
    @Test
    void initialiseBooking_success() {

        BookingRequestDto request = BookingRequestDto.builder()
                .hotelId(1L)
                .roomId(1L)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(1))
                .roomsCount(1)
                .build();

        Hotel hotel = Hotel.builder().id(1L).build();
        Room room = Room.builder().id(1L).build();

        Inventory inventory = Inventory.builder()
                .reserveCount(0)
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .amount(BigDecimal.TEN)
                .bookingStatus(BookingStatus.RESERVED)
                .build();

        BookingDto bookingDto = new BookingDto();

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(inventoryRepository.findAndLocakAvailableInventory(
                anyLong(), any(), any(), anyInt()
        )).thenReturn(List.of(inventory, inventory));

        when(bookingRepository.save(any())).thenReturn(booking);
        when(modelMapper.map(any(Booking.class), eq(BookingDto.class)))
                .thenReturn(bookingDto);

        BookingDto result = bookingService.initialiseBooking(request);

        assertNotNull(result);
        verify(inventoryRepository).saveAll(anyList());
        verify(bookingRepository).save(any());
    }

    // ===========================
    // initialiseBooking - HOTEL NOT FOUND
    // ===========================
    @Test
    void initialiseBooking_hotelNotFound() {

        BookingRequestDto request = BookingRequestDto.builder()
                .hotelId(1L)
                .roomId(1L)
                .build();

        when(hotelRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.initialiseBooking(request));
    }

    // ===========================
    // addGuests - SUCCESS
    // ===========================
    @Test
    void addGuests_success() {

        Booking booking = Booking.builder()
                .id(1L)
                .user(user)
                .bookingStatus(BookingStatus.RESERVED)
                .createdAt(LocalDateTime.now())
                .guests(new HashSet<>())   // ✅ FIXED
                .build();

        GuestDto guestDto = GuestDto.builder()  // ✅ FIXED
                .name("John")
                .age(28)
                .build();

        Guest guest = Guest.builder().id(1L).build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(modelMapper.map(guestDto, Guest.class)).thenReturn(guest);
        when(guestRepository.save(any(Guest.class))).thenReturn(guest);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(modelMapper.map(any(Booking.class), eq(BookingDto.class)))
                .thenReturn(new BookingDto());

        BookingDto result = bookingService.addGuests(1L, List.of(guestDto));

        assertNotNull(result);
        assertEquals(BookingStatus.GUESTS_ADDED, booking.getBookingStatus());
        assertEquals(1, booking.getGuests().size());
    }

    // ===========================
    // addGuests - UNAUTHORIZED
    // ===========================
    @Test
    void addGuests_unauthorizedUser() {

        User anotherUser = User.builder().id(2L).build();

        Booking booking = Booking.builder()
                .id(1L)
                .user(anotherUser)
                .bookingStatus(BookingStatus.RESERVED)
                .createdAt(LocalDateTime.now())
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        GuestDto guestDto = GuestDto.builder()
                .name("Guest")
                .age(22)
                .build();

        assertThrows(unAuthorizedError.class,
                () -> bookingService.addGuests(1L, List.of(guestDto)));
    }

    // ===========================
    // addGuests - BOOKING EXPIRED
    // ===========================
    @Test
    void addGuests_bookingExpired() {

        Booking booking = Booking.builder()
                .id(1L)
                .user(user)
                .bookingStatus(BookingStatus.RESERVED)
                .createdAt(LocalDateTime.now().minusMinutes(15))
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        GuestDto guestDto = GuestDto.builder()
                .name("Expired Guest")
                .age(30)
                .build();

        assertThrows(IllegalStateException.class,
                () -> bookingService.addGuests(1L, List.of(guestDto)));
    }
}
