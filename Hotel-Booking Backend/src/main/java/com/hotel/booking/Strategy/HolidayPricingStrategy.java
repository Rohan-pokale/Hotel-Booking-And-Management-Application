package com.hotel.booking.Strategy;

import com.hotel.booking.Entity.Hotel;
import com.hotel.booking.Entity.Inventory;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy{

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {

        BigDecimal price=wrapped.calculatePrice(inventory);
        boolean isHoliday=true;

        if(isHoliday){
                price=price.multiply(BigDecimal.valueOf(1.25));
        }

        return price;
    }
}
