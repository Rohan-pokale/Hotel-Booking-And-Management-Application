package com.hotel.booking.Service;

import com.hotel.booking.Dto.BookingDto;
import com.hotel.booking.Dto.BookingRequestDto;
import com.hotel.booking.Dto.GuestDto;
import com.hotel.booking.Dto.HotelDto;
import com.hotel.booking.Entity.*;
import com.hotel.booking.Entity.enums.BookingStatus;
import com.hotel.booking.Exception.ResourceNotFoundException;
import com.hotel.booking.Exception.unAuthorizedError;
import com.hotel.booking.Repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookingServiceImpl implements BookingService{
    private final GuestRepository guestRepository;

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequestDto bookingRequestDto) {

        log.info("initialising booking for hotel: {}, room: {}, date:{}-{}",
                bookingRequestDto.getHotelId(),bookingRequestDto.getRoomId(),bookingRequestDto.getCheckInDate(),bookingRequestDto.getCheckOutDate());

        Hotel hotel=hotelRepository.findById(bookingRequestDto.getHotelId())
                .orElseThrow(()->new ResourceNotFoundException("Hotel Not Found for id:"+bookingRequestDto.getHotelId()));

        Room room=roomRepository.findById(bookingRequestDto.getRoomId())
                .orElseThrow(()->new ResourceNotFoundException("Room Not Found for id:"+bookingRequestDto.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository.findAndLocakAvailableInventory(
                bookingRequestDto.getRoomId(),
                bookingRequestDto.getCheckInDate(),
                bookingRequestDto.getCheckOutDate(),
                bookingRequestDto.getRoomsCount()
        );

        long daysCount= ChronoUnit.DAYS.between(bookingRequestDto.getCheckInDate(),bookingRequestDto.getCheckOutDate())+1;

        if(inventoryList.size()<daysCount){
            throw  new IllegalStateException("Room is not available anymore");
        }

        //reserve the rooms/update the booked count of inventory

        for(Inventory inventory:inventoryList){
            inventory.setReserveCount(inventory.getReserveCount()+bookingRequestDto.getRoomsCount());
        }

        inventoryRepository.saveAll(inventoryList);

        //create the booking



        // TODO: calculate the dynamic pricing.

        Booking booking=Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequestDto.getCheckInDate())
                .checkOutDate(bookingRequestDto.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequestDto.getRoomsCount())
                .amount(BigDecimal.TEN)
                .build();

        booking=bookingRepository.save(booking);

        return modelMapper.map(booking, BookingDto.class);

    }


    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("initialising booking for booing id: {}", bookingId);

        Booking booking=bookingRepository.findById(bookingId)
                .orElseThrow(()->new ResourceNotFoundException("Booking Not Found for id:"+bookingId));

        User user=getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new unAuthorizedError("Booking does not belong to this user with id:"+user.getId());
        }

        if(isBookingExpired(booking)){
            throw new IllegalStateException("Booking had expired.");
        }

        if(booking.getBookingStatus()!=BookingStatus.RESERVED){
            throw new IllegalStateException("Booking is not under reserved state.");
        }

        for(GuestDto guestDto:guestDtoList){
            Guest guest=modelMapper.map(guestDto, Guest.class);
            guest.setUser(user);
            guest=guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);

        booking=bookingRepository.save(booking);

        return modelMapper.map(booking,BookingDto.class);
    }

    public boolean isBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public  User getCurrentUser(){

        return (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
