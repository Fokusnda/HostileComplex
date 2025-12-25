package com.example.hostilecomplex;

public class BookingClientDB {
    private final int       idBookingClient;
    private final int       idBooking;
    private final String    clientSurname;

    public BookingClientDB(int idBooking, int idBookingClient, String clientSurname) {
        this.idBooking = idBooking;
        this.idBookingClient = idBookingClient;
        this.clientSurname = clientSurname;
    }

    public int getIdBooking()               { return idBooking; }
    public int getIdBookingClient()         { return idBookingClient; }
    public String getClientSurname()        { return clientSurname; }
}
