package com.irkdoska.irkdoska.entity;

import lombok.Data;

@Data
public class AdRequest {
    private String description;
    private Double price;
    private String city;
    private String phone;
    private Boolean isPaid;
}
