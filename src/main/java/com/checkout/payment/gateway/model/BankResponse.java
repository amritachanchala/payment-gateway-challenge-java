package com.checkout.payment.gateway.model;

import lombok.Data;

@Data
public class BankResponse {
  private Boolean authorized;
  private String authorization_code;
}