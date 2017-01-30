package de.tu_chemnitz.sse.and2015.AwakenWalls;

/**
 * Created by mohammadasif on 16/01/2017.
 */

public class UserAddress {
    private String address;
    private String city;
    private String postCode;
    private String country;

    public UserAddress(String address, String city, String country, String postCode) {
        this.address = address;
        this.city = city;
        this.country = country;
        this.postCode = postCode;
    }

    public String getAddress() {
        return this.address;
    }

    public String getCity() {
        return this.city;
    }

    public String getCountry() {
        return this.country;
    }

    public String getPostCode() {
        return this.postCode;
    }
}
