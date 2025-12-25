package com.example.hostilecomplex;

import java.math.BigDecimal;

public class ServiceDB {
    private final int           idService;
    private final String        name;
    private final BigDecimal    price;

    public ServiceDB(int idService, String name, BigDecimal price) {
        this.idService = idService;
        this.name = name;
        this.price = price;
    }

    public int getIdService()       { return idService; }
    public String getName()         { return name; }
    public BigDecimal getPrice()    { return price; }
}
