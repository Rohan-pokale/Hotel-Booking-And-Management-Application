package com.hotel.booking.Service;

import com.hotel.booking.Dto.HotelDto;
import com.hotel.booking.Entity.Booking;
import com.hotel.booking.Entity.Hotel;
import com.hotel.booking.Entity.Room;
import com.hotel.booking.Exception.ResourceNotFoundException;
import com.hotel.booking.Repository.HotelRepository;
import com.hotel.booking.Repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.internal.bytebuddy.implementation.bytecode.Throw;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService{

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("creating a new hotel with name:{}"+hotelDto.getName());
        Hotel hotel=modelMapper.map(hotelDto,Hotel.class);
        hotel.setActive(false);

        hotel=hotelRepository.save(hotel);
        log.info("creating a new hotel with id:{}"+hotelDto.getId());
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("getting hotel ny id:{}"+id);
        Hotel hotel=hotelRepository
                .findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("hotel not found with id:"+id));
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updateHOtelByid(HotelDto hotelDto, Long id) {
        log.info("Updating hotel ny id:{}"+id);
        Hotel hotel=hotelRepository
                .findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("hotel not found with id:"+id));
        modelMapper.map(hotelDto,hotel);
        hotel.setId(id);
        hotel=hotelRepository.save(hotel);
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHOtelByid(Long id) {
        Hotel hotel=hotelRepository
                .findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("hotel not found with id:"+id));

        //delete the future inventory for this hotels room
        //delete the rooms from room_table also.
        for(Room room:hotel.getRooms()){
            inventoryService.deleteInventory(room);
            roomRepository.deleteById(room.getId());
        }

        hotelRepository.deleteById(id);


    }

    @Override
    @Transactional
    public void activateHotel(Long id) {
        log.info("Activating hotel ny id:{}"+id);
        Hotel hotel=hotelRepository
                .findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("hotel not found with id:"+id));
        hotel.setActive(true);

        // assuming only do it once(initializing rooms in inventory)
        for(Room room:hotel.getRooms()){
            inventoryService.initializeRoomForAYear(room);
        }
    }


}
