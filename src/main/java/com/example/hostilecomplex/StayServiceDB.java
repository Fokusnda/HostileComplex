package com.example.hostilecomplex;

import java.math.BigDecimal;

public class StayServiceDB {
    private final int           idStayService;
    private final int           building;
    private final int           idStay;
    private final String        nameService;
    private final int           quantity;
    private final BigDecimal    totalPrice;

    public StayServiceDB(int idStayService, int building, int idStay, String nameService, int quantity, BigDecimal totalPrice) {
        this.idStayService = idStayService;
        this.building = building;
        this.idStay = idStay;
        this.nameService = nameService;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public int getIdStayService()       { return idStayService; }
    public int getBuilding()            { return building; }
    public int getIdStay()              { return idStay; }
    public String getNameService()      { return nameService; }
    public int getQuantity()            { return quantity; }
    public BigDecimal getTotalPrice()   { return totalPrice; }
}
