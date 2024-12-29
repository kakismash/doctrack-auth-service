package com.kaki.doctrack.authservice.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrganizationDto(Long id,
                              String name, String description, String phone, String address1, String address2,
                              String city, String state, String zip, String country, String email, String contactName,
                              String website, String logo, String type, String status,
                              String stripeCustomerId,
                              Set<LocationDto> locations,
                              String createdBy, Long createdDate, String lastModifiedBy, Long lastModifiedDate) implements Serializable {
}
