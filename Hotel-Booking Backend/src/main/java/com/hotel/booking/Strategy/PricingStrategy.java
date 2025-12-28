package com.hotel.booking.Strategy;

import com.hotel.booking.Entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);
}
