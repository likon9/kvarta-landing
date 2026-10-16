package com.example.obzharka.dto;

public record LeadResponse(String status) {
    public static final LeadResponse CREATED = new LeadResponse("created");
    public static final LeadResponse DUPLICATE = new LeadResponse("duplicate");
}
