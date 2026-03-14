package com.webbandoan.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Simple shop status service. Controls whether site accepts new orders.
 * Controlled by property `shop.open` (default true).
 */
@Service
public class ShopService {

    private volatile boolean open;

    public ShopService(@Value("${shop.open:true}") boolean open) {
        this.open = open;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
