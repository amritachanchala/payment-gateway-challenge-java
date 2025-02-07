package com.checkout.payment.gateway.model;

import lombok.Data;

@Data
public class BankRequest {
  private String card_number;
  private String expiry_date;
  private String currency;
  private Integer amount;
  private String cvv;

  public static BankRequest from(PostPaymentRequest req) {
    BankRequest br = new BankRequest();
    br.card_number = String.valueOf(req.getCardNumber());
    br.expiry_date = String.format("%02d/%d",
        req.getExpiryMonth(),
        req.getExpiryYear());
    br.currency = req.getCurrency();
    br.amount = req.getAmount();
    br.cvv = String.valueOf(req.getCvv());
    return br;
  }
}