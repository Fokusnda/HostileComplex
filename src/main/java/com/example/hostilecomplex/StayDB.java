package com.example.hostilecomplex;

import java.time.LocalDate;

public class StayDB {
    private final int           idStay;
    private final int           building;
    private final int           floorNumber;
    private final String        clientSurname;
    private final int           numberRoom;
    private final LocalDate     checkIn;
    private final LocalDate     checkOut;

    public StayDB(int idStay, int building, int floorNumber, String clientSurname, int numberRoom, LocalDate checkIn, LocalDate checkOut) {
        this.idStay = idStay;
        this.building = building;
        this.floorNumber = floorNumber;
        this.clientSurname = clientSurname;
        this.numberRoom = numberRoom;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public int getIdStay()                  { return idStay; }
    public int getBuilding()                { return building; }
    public int getFloorNumber()             { return floorNumber; }
    public int getNumberRoom()              { return numberRoom; }
    public String getClientSurname()        { return clientSurname; }
    public LocalDate getCheckIn()           { return checkIn; }
    public LocalDate getCheckOut()          { return checkOut; }
}
