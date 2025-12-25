package com.example.hostilecomplex;

public class ClientDB {
    private final int       idClient;
    private final String    surname;
    private final String    name;
    private final String    secondName;
    private final String    phoneNumber;

    public ClientDB(int idClient, String surname, String name, String secondName, String phoneNumber) {
        this.idClient = idClient;
        this.surname = surname;
        this.name = name;
        this.secondName = secondName;
        this.phoneNumber = phoneNumber;
    }

    public int getIdClient()        { return idClient; }
    public String getSurname()      { return surname; }
    public String getName()         { return name; }
    public String getSecondName()   { return secondName; }
    public String getPhoneNumber()  { return phoneNumber; }
}
