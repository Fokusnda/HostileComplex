package com.example.hostilecomplex;

public class FloorDB {
    private final int   idFloor;
    private final int   idBuilding;
    private final int   floorNumber;

    public FloorDB(int idFloor, int idBuilding, int floorNumber) {
        this.idFloor = idFloor;
        this.idBuilding = idBuilding;
        this.floorNumber = floorNumber;
    }

    public int getIdFloor()         { return idFloor; }
    public int getIdBuilding()      { return idBuilding; }
    public int getFloorNumber()     { return floorNumber; }

    @Override
    public String toString() { return String.valueOf(floorNumber); }
}
