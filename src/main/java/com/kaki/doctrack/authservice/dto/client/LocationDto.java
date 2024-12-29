package com.kaki.doctrack.authservice.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LocationDto(Long id, String name, String description, String phone, String address1, String address2,
                          String city, String state, String zip, String country) implements Serializable {
    public LocationDto withId(Long id) {
        return new LocationDto(id, name, description, phone, address1, address2, city, state, zip, country);
    }
}
