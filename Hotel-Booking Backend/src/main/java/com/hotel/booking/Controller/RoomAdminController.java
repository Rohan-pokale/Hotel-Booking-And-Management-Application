package com.hotel.booking.Controller;

import com.hotel.booking.Dto.RoomDto;
import com.hotel.booking.Service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/hotels/{hotelId}/rooms")
public class RoomAdminController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomDto> createNewRoom(@PathVariable Long hotelId, @RequestBody RoomDto roomDto) {
        RoomDto roomDto1=roomService.createNewRoom(hotelId,roomDto);
        return new ResponseEntity<>(roomDto1, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRoomsByHotelId(@PathVariable Long hotelId) {
         return ResponseEntity.ok(roomService.getAllRoomsByHotelId(hotelId));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long hotelId,@PathVariable Long roomId) {
        RoomDto roomDto1=roomService.getRoomById(roomId);
        return new ResponseEntity<>(roomDto1, HttpStatus.OK);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoomById(@PathVariable Long hotelId,@PathVariable Long roomId) {
        roomService.deleteRoomById(roomId);
        return ResponseEntity.noContent().build();
    }
}
