package com.hotel.booking.Strategy;

import com.hotel.booking.Entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
public class UrgencyPricingStrategy implements PricingStrategy{

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {

        BigDecimal price=wrapped.calculatePrice(inventory);

        LocalDate tody=LocalDate.now();
        if(!inventory.getDate().isBefore(tody)&&inventory.getDate().isBefore(tody.plusDays(7))){
            price=price.multiply(BigDecimal.valueOf(1.15));
        }
        return price;
    }
}
