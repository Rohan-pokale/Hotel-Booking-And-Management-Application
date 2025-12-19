package com.hotel.booking.Repository;

import com.hotel.booking.Entity.Hotel;
import com.hotel.booking.Entity.Inventory;
import com.hotel.booking.Entity.Room;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    void deleteByRoom(Room room);
    Page<Hotel>  findHotelsByAvailabelInventory();
}