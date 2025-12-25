package com.example.hostilecomplex;

public class BuildingDB {
    private final int       idBuilding;
    private final String    hotelClass;
    private final int       floorsCount;
    private final int       totalRooms;
    private final int       roomsPerFloor;
    private final boolean   hasCleaning;
    private final boolean   hasLaundry;
    private final boolean   hasDryCleaning;
    private final boolean   hasFood;
    private final boolean   hasEntertainment;

    public BuildingDB(int idBuilding, String hotelClass, int floorsCount, int totalRooms, int roomsPerFloor,
                      boolean hasCleaning, boolean hasLaundry, boolean hasDryCleaning, boolean hasFood, boolean hasEntertainment) {
        this.idBuilding = idBuilding;
        this.hotelClass = hotelClass;
        this.floorsCount = floorsCount;
        this.totalRooms = totalRooms;
        this.roomsPerFloor = roomsPerFloor;
        this.hasCleaning = hasCleaning;
        this.hasLaundry = hasLaundry;
        this.hasDryCleaning = hasDryCleaning;
        this.hasFood = hasFood;
        this.hasEntertainment = hasEntertainment;
    }

    public int getIdBuilding()              { return idBuilding; }
    public String getHotelClass()           { return hotelClass; }
    public int getFloorsCount()             { return floorsCount; }
    public int getTotalRooms()              { return totalRooms; }
    public int getRoomsPerFloor()           { return roomsPerFloor; }
    public boolean getHasCleaning()         { return hasCleaning; }
    public boolean getHasLaundry()          { return hasLaundry; }
    public boolean getHasDryCleaning()      { return hasDryCleaning; }
    public boolean getHasFood()             { return hasFood; }
    public boolean getHasEntertainment()    { return hasEntertainment; }
}
