package com.example.hostilecomplex;

import java.time.LocalDate;

public class StayDB {
    private final int           idStay;
    private final String        clientSurname;
    private final int           numberRoom;
    private final LocalDate     checkIn;
    private final LocalDate     checkOut;

    public StayDB(int idStay, String clientSurname, int numberRoom, LocalDate checkIn, LocalDate checkOut) {
        this.idStay = idStay;
        this.clientSurname = clientSurname;
        this.numberRoom = numberRoom;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public int getIdStay()                  { return idStay; }
    public int getNumberRoom()                  { return numberRoom; }
    public String getClientSurname()        { return clientSurname; }
    public LocalDate getCheckIn()           { return checkIn; }
    public LocalDate getCheckOut()          { return checkOut; }
}
