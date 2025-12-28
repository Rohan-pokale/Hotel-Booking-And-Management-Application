package com.hotel.booking.Repository;


import com.hotel.booking.Dto.HotelPriceDto;
import com.hotel.booking.Entity.Hotel;
import com.hotel.booking.Entity.HotelMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;

public interface HotelMinPriceRepositoy extends JpaRepository<HotelMinPrice,Long> {

    @Query("""
               select new com.hotel.booking.Dto.HotelPriceDto(i.hotel,AVG(i.price))
               from HotelMinPrice i
               where i.hotel.city= :city
                   and i.date between :startDate and :endDate
                   and i.hotel.isActive=true
               group by i.hotel
            """)
    Page<HotelPriceDto> findHotelsByAvailabelInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomCount") Integer roomCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );

    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);
}
