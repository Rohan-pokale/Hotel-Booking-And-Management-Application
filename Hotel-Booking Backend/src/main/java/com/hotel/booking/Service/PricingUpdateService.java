package com.hotel.booking.Service;

import com.hotel.booking.Entity.Hotel;
import com.hotel.booking.Entity.HotelMinPrice;
import com.hotel.booking.Entity.Inventory;
import com.hotel.booking.Repository.HotelMinPriceRepositoy;
import com.hotel.booking.Repository.HotelRepository;
import com.hotel.booking.Repository.InventoryRepository;
import com.hotel.booking.Strategy.PricingService;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingUpdateService {

    // schedular to update the inventory and hotelMinPrice table every hour

    private final HotelRepository hotelRepository;
    private final HotelMinPriceRepositoy hotelMinPriceRepositoy;
    private final InventoryRepository inventoryRepository;
    private final PricingService pricingService;

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void updatePrice(){

        int page=0;
        int batchSize=100;

        while (true){
            Page<Hotel> hotelPage=hotelRepository.findAll(PageRequest.of(page,batchSize));

            if(hotelPage.isEmpty()){
                break;
            }
            hotelPage.getContent().forEach(this::updateHotelPrices);

            page++;
        }
    }

    @Transactional
    private void updateHotelPrices(Hotel hotel){
        LocalDate startDate=LocalDate.now();
        LocalDate endDate=LocalDate.now().plusYears(1);

        List<Inventory> inventoryList=inventoryRepository.findByHotelAndDateBetween(hotel,startDate,endDate);
        updateInventoryPrices(inventoryList);
        updateHotelMinPrice(hotel,inventoryList,startDate,endDate);

    }

    @Transactional
    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate) {

        Map<LocalDate,BigDecimal> dailyMinPrice=inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice,Collectors.minBy(Comparator.naturalOrder()))
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,e->e.getValue().orElse(BigDecimal.ZERO)));

        // Prepare HotelPrice entities in bulk
        List<HotelMinPrice> hotelPrices = new ArrayList<>();
        dailyMinPrice.forEach((date, price) -> {
            HotelMinPrice hotelPrice = hotelMinPriceRepositoy.findByHotelAndDate(hotel, date)
                    .orElse(new HotelMinPrice(hotel, date));
            hotelPrice.setPrice(price);
            hotelPrices.add(hotelPrice);
        });

        hotelMinPriceRepositoy.saveAll(hotelPrices);

    }

    @Transactional
    private void updateInventoryPrices(List<Inventory> inventoryList){
        inventoryList.forEach(inventory -> {
            BigDecimal dynamicPrice=pricingService.calculateDynamicPricing(inventory);
            inventory.setPrice(dynamicPrice);
        });
        inventoryRepository.saveAll(inventoryList);
    }
}
