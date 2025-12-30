package com.hotel.booking.Service;

import com.hotel.booking.Dto.RoomDto;
import com.hotel.booking.Entity.Hotel;
import com.hotel.booking.Entity.Room;
import com.hotel.booking.Entity.User;
import com.hotel.booking.Exception.ResourceNotFoundException;
import com.hotel.booking.Exception.unAuthorizedError;
import com.hotel.booking.Repository.HotelRepository;
import com.hotel.booking.Repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;

    @Override
    public RoomDto createNewRoom(Long hotelId,RoomDto roomDto) {
        log.info("creating new Room by hotel id:"+ hotelId);
        Hotel hotel=hotelRepository
                .findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("hotel not found with id:"+hotelId));

        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if( !user.equals(hotel.getOwner())){
            throw new unAuthorizedError("this user is not owned this hotel with id:"+hotelId);
        }

        Room room=modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room=roomRepository.save(room);

        if(hotel.isActive()){
            inventoryService.initializeRoomForAYear(room); //adding room to inventory
        }

        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsByHotelId(Long hotelId) {
        log.info("getting all Rooms by hotel id:"+ hotelId);
        Hotel hotel=hotelRepository
                .findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("hotel not found with id:"+hotelId));

        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if( !user.equals(hotel.getOwner())){
            throw new unAuthorizedError("this user is not owned this hotel with id:"+hotelId);
        }

        return hotel.getRooms()
                .stream()
                .map((element)->modelMapper.map(element,RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("getting the Rooms by Room id:"+ roomId);
        Room room=roomRepository
                .findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found with id:"+roomId));
        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    @Transactional
    public void deleteRoomById(Long roomId) {
        log.info("getting the Rooms by Room id:"+ roomId);

        Room room=roomRepository
                .findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found with id:"+roomId));

        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if( !user.equals(room.getHotel().getOwner())){
            throw new unAuthorizedError("this user is not owned this room with id:"+room.getHotel().getId());
        }
        //delete all future inventory for this room.
        inventoryService.deleteInventory(room);

        roomRepository.deleteById(roomId);

    }
}
