package com.hotel.booking.Controller;

import com.hotel.booking.Dto.HotelDto;
import com.hotel.booking.Entity.Booking;
import com.hotel.booking.Entity.Hotel;
import com.hotel.booking.Exception.ResourceNotFoundException;
import com.hotel.booking.Service.HotelService;
import com.hotel.booking.Service.HotelServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelService hotelService;

    @PostMapping
    public ResponseEntity<HotelDto> createNewHotel(@RequestBody HotelDto hotelDto) {
        HotelDto hotelDto1=hotelService.createNewHotel(hotelDto);
        return new ResponseEntity<>(hotelDto1, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long id) {
        HotelDto hotelDto=hotelService.getHotelById(id);
        return ResponseEntity.ok(hotelDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelDto> updateHOtelByid(@RequestBody HotelDto hotelDto,@PathVariable Long id) {
        HotelDto hotelDto1=hotelService.updateHOtelByid(hotelDto,id);
        return ResponseEntity.ok(hotelDto1);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHOtelByid(@PathVariable Long id) {
        hotelService.deleteHOtelByid(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> activateHotel(@PathVariable Long id) {
        hotelService.activateHotel(id);
        return ResponseEntity.noContent().build();
    }
}
