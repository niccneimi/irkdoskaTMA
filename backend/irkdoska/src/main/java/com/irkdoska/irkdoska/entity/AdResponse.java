package com.irkdoska.irkdoska.entity;

import lombok.Builder;
import lombok.Data;
import java.util.List;

import com.irkdoska.irkdoska.model.Ad;

@Data
@Builder
public class AdResponse {
    private List<Ad> ads;
}
