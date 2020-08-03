package edu.sharif.survey.domain;


import lombok.Data;

@Data
public class BidRequest {

    private String price;
    private int auctionId;

}
