package com.example.hostilecomplex;

public class GuestDB {
    private final Long idGuest;
    private final String fio;
    private final String phoneNumber;

    public GuestDB(Long id, String fio, String phoneNumber) {
        this.idGuest = id;
        this.fio = fio;
        this.phoneNumber = phoneNumber;
    }

    public Long getIdGuest() { return idGuest; }
    public String getFio() { return fio; }
    public String getPhoneNumber() { return phoneNumber; }
}
