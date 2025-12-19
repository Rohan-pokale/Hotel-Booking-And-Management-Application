package com.hotel.booking.Repository;

import com.hotel.booking.Entity.Hotel;
import com.hotel.booking.Entity.Inventory;
import com.hotel.booking.Entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    void deleteByRoom(Room room);

    @Query("""
               select distinct i.hotel
               from Inventory i
               where i.city= :city
                   and i.date between :startDate and :endDate
                   and i.closed=false
                   and (i.totalCount-i.bookedCount>= :roomCount)
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
}