package com.example.hostilecomplex;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingDB {
    private final Long idBooking;
    private final String idGuest;
    private final int idRoom;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final BigDecimal totalAmount;

    public BookingDB(Long idBooking, String idGuest, int idRoom, LocalDate checkInDate, LocalDate checkOutDate, BigDecimal totalAmount) {
        this.idBooking = idBooking;
        this.idGuest = idGuest;
        this.idRoom = idRoom;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalAmount = totalAmount;
    }

    public Long getIdBooking()          { return idBooking; }
    public String getIdGuest()          { return idGuest; }
    public int getIdRoom()              { return idRoom; }
    public LocalDate getCheckInDate()   { return checkInDate; }
    public LocalDate getCheckOutDate()  { return checkOutDate; }
    public BigDecimal getTotalAmount()  { return totalAmount; }
}
