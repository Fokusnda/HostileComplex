package com.example.hostilecomplex;

import java.math.BigDecimal;

public class RoomDB {
    private final int           idRoom;
    private final int           floor;
    private final int           room;
    private final String        roomType;
    private final BigDecimal    price;
    private final boolean       isBusy;

    public RoomDB(int idRoom, int floor, int room, String roomType, BigDecimal price, boolean isBusy) {
        this.idRoom = idRoom;
        this.floor = floor;
        this.room = room;
        this.roomType = roomType;
        this.price = price;
        this.isBusy = isBusy;
    }

    public int getIdRoom()          { return idRoom; }
    public int getFloor()           { return floor; }
    public int getRoom()            { return room; }
    public String getRoomType()     { return roomType; }
    public BigDecimal getPrice()    { return price; }
    public boolean getIsBusy()      { return isBusy; }
}
