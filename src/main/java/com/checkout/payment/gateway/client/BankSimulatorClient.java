package com.checkout.payment.gateway.client;


import com.checkout.payment.gateway.model.BankRequest;
import com.checkout.payment.gateway.model.BankResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 *
 */
@Component
public class BankSimulatorClient {

  private final RestTemplate restTemplate;

  public BankSimulatorClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public BankResponse authorize(PostPaymentRequest request) {

    BankRequest bankRequest = BankRequest.from(request);

    try {
      ResponseEntity<BankResponse> response =
          restTemplate.postForEntity(
              "http://localhost:8080/payments",
              bankRequest,
              BankResponse.class
          );

      return response.getBody();

    } catch (Exception ex) {
      return null;
    }
  }
}