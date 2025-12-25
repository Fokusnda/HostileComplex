package com.example.hostilecomplex;

public class OrganizationDB {
    private final int       idOrganization;
    private final String    name;
    private final int       discountRate;

    public OrganizationDB(int idOrganization, String name, int discountRate) {
        this.idOrganization = idOrganization;
        this.name = name;
        this.discountRate = discountRate;
    }

    public int getIdOrganization()      {return idOrganization; }
    public String getName()             { return name; }
    public int getDiscountRate()        { return discountRate; }
}
