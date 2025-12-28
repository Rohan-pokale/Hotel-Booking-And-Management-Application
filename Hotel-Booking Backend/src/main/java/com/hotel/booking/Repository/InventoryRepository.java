package com.hotel.booking.Repository;

import com.hotel.booking.Entity.Hotel;
import com.hotel.booking.Entity.Inventory;
import com.hotel.booking.Entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    void deleteByRoom(Room room);

    @Query("""
               select distinct i.hotel
               from Inventory i
               where i.city= :city
                   and i.date between :startDate and :endDate
                   and i.closed=false
                   and (i.totalCount-i.bookedCount-i.reserveCount>= :roomCount)
               group by i.hotel,i.room
               having count(i.date)=:dateCount
            """)
    Page<Hotel> findHotelsByAvailabelInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomCount") Integer roomCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );


    @Query("""
            select i from Inventory i
            where i.room.id=:room_id
                   and i.date between :startDate and :endDate
                   and i.closed=false
                   and (i.totalCount-i.bookedCount-i.reserveCount)>= :roomCount
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLocakAvailableInventory(
            @Param("room_id") Long room_id,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomCount") Integer roomCount
    );

    List<Inventory> findByHotelAndDateBetween(
            Hotel hotel,
            LocalDate startDate,
            LocalDate endDate
    );
}