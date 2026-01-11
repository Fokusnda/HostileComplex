package com.example.hostilecomplex;

import java.time.LocalDate;

public class BookingDB {
    private final int           idBooking;
    private final String        orgName;
    private final String        hotelClass;
    private final int           floorNumber;
    private final int           roomsCount;
    private final int           peopleCount;
    private final LocalDate     bookingDate;
    private final LocalDate     arrivalDate;

    public BookingDB (int idBooking, String hotelClass, int floorNumber, int roomsCount,
                      int peopleCount, LocalDate bookingDate, LocalDate arrivalDate, String orgName) {
        this.idBooking = idBooking;
        this.orgName = orgName;
        this.hotelClass = hotelClass;
        this.floorNumber = floorNumber;
        this.roomsCount = roomsCount;
        this.peopleCount = peopleCount;
        this.bookingDate = bookingDate;
        this.arrivalDate = arrivalDate;
    }

    public int getIdBooking()           { return idBooking; }
    public String getOrgName()          { return orgName; }
    public String getHotelClass()       { return hotelClass; }
    public int getFloorNumber()         { return floorNumber; }
    public int getRoomsCount()          { return roomsCount; }
    public int getPeopleCount()         { return peopleCount; }
    public LocalDate getBookingDate()   { return bookingDate; }
    public LocalDate getArrivalDate()   { return arrivalDate; }
}
