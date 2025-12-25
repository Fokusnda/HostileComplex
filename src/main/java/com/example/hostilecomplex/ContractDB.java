package com.example.hostilecomplex;

import java.time.LocalDate;

public class ContractDB {
    private final int           idContract;
    private final String        nameOrganization;
    private final LocalDate     startDate;
    private final LocalDate     endDate;

    public ContractDB(int idContract, String nameOrganization, LocalDate startDate, LocalDate endDate) {
        this.idContract = idContract;
        this.nameOrganization = nameOrganization;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getIdContract()              { return idContract; }
    public String getNameOrganization()     { return nameOrganization; }
    public LocalDate getStartDate()         { return startDate; }
    public LocalDate getEndDate()           { return endDate; }
}
