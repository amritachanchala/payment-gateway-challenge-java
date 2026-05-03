package com.checkout.payment.gateway.integrationtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentRepository;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentGatewayIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;
  @Autowired
  private PaymentRepository paymentRepository;
  private PostPaymentRequest request;

  @BeforeEach
  void setup() {
    request = new PostPaymentRequest();
    request.setCardNumber("1234567891234561");
    request.setExpiryMonth(12);
    request.setExpiryYear(2030);
    request.setCurrency("USD");
    request.setAmount(1050);
    request.setCvv(123);
  }

  // -----------------------------
  // AUTHORIZED FLOW (endsWith 1,3,5,7,9)
  // -----------------------------
  @Test
  void shouldAuthorizePayment_whenCardEndsWithOddNumber() {

    ResponseEntity<PostPaymentResponse> response =
        restTemplate.postForEntity("/payment", request, PostPaymentResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    PostPaymentResponse body = response.getBody();
    assertNotNull(body);

    assertEquals(PaymentStatus.AUTHORIZED, body.getStatus());
    assertEquals(4561, body.getCardNumberLastFour());
  }

  // -----------------------------
  // DECLINED FLOW (endsWith 2,4,6,8)
  // -----------------------------
  @Test
  void shouldDeclinePayment_whenCardEndsWithEvenNumber() {

    request.setCardNumber("1234567891234562"); // ends with 2

    ResponseEntity<PostPaymentResponse> response =
        restTemplate.postForEntity("/payment", request, PostPaymentResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    PostPaymentResponse body = response.getBody();
    assertNotNull(body);

    assertEquals(PaymentStatus.DECLINED, body.getStatus());
    assertEquals(4562, body.getCardNumberLastFour());
  }

  // -----------------------------
  // REJECTED FLOW (endsWith 0 → 503 from bank simulator)
  // -----------------------------
  @Test
  void shouldRejectPayment_whenBankReturns503() {

    request.setCardNumber("1234567891234560"); // ends with 0

    ResponseEntity<PostPaymentResponse> response =
        restTemplate.postForEntity("/payment", request, PostPaymentResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    PostPaymentResponse body = response.getBody();
    assertNotNull(body);

    assertEquals(PaymentStatus.REJECTED, body.getStatus());
  }

  // -----------------------------
  // RETRIEVE PAYMENT BY ID
  // -----------------------------
  @Test
  void shouldRetrievePayment_whenPaymentExists() {

    // first create payment
    ResponseEntity<PostPaymentResponse> createResponse =
        restTemplate.postForEntity("/payment", request, PostPaymentResponse.class);

    UUID id = createResponse.getBody().getId();

    ResponseEntity<PostPaymentResponse> getResponse =
        restTemplate.getForEntity("/payment/" + id, PostPaymentResponse.class);

    assertEquals(HttpStatus.OK, getResponse.getStatusCode());
    assertNotNull(getResponse.getBody());
    assertEquals(id, getResponse.getBody().getId());
  }

  // -----------------------------
  // RETRIEVE PAYMENT - NOT FOUND
  // -----------------------------
  @Test
  void shouldReturn404_whenPaymentDoesNotExist() {

    UUID randomId = UUID.randomUUID();

    ResponseEntity<String> response =
        restTemplate.getForEntity("/payment/" + randomId, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  // -----------------------------
  // VALID PAYMENT - AUTHORIZED
  // -----------------------------
  @Test
  void shouldAuthorizePayment_whenValidCard() {

    PostPaymentRequest request = validRequest("1234567891234561"); // ends with 1

    ResponseEntity<PostPaymentResponse> response =
        restTemplate.postForEntity("/payment", request, PostPaymentResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("AUTHORIZED", response.getBody().getStatus().name());
  }

  // -----------------------------
  // INVALID CARD NUMBER
  // -----------------------------
  @Test
  void shouldReturn400_whenCardNumberInvalid() {

    PostPaymentRequest request = validRequest("12"); // invalid length

    ResponseEntity<String> response =
        restTemplate.postForEntity("/payment", request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  // -----------------------------
  // INVALID EXPIRY MONTH
  // -----------------------------
  @Test
  void shouldReturn400_whenExpiryMonthInvalid() {

    PostPaymentRequest request = validRequest("1234567891234561");
    request.setExpiryMonth(18); // invalid

    ResponseEntity<String> response =
        restTemplate.postForEntity("/payment", request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  // -----------------------------
  // INVALID CVV
  // -----------------------------
  @Test
  void shouldReturn400_whenCVVInvalid() {

    PostPaymentRequest request = validRequest("1234567891234561");
    request.setCvv(12); // invalid (too short)

    ResponseEntity<String> response =
        restTemplate.postForEntity("/payment", request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  // -----------------------------
  // INVALID CURRENCY
  // -----------------------------
  @Test
  void shouldReturn400_whenCurrencyInvalid() {

    PostPaymentRequest request = validRequest("1234567891234561");
    request.setCurrency("ABC"); // not allowed

    ResponseEntity<String> response =
        restTemplate.postForEntity("/payment", request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  // -----------------------------
  // REJECTED (BANK DOWN - 503)
  // -----------------------------
  @Test
  void shouldReturnRejected_whenBankReturns503() {

    PostPaymentRequest request = validRequest("1234567891234560"); // ends with 0

    ResponseEntity<PostPaymentResponse> response =
        restTemplate.postForEntity("/payment", request, PostPaymentResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("REJECTED", response.getBody().getStatus().name());
  }

  private PostPaymentRequest validRequest(String cardNumber) {
    PostPaymentRequest request = new PostPaymentRequest();
    request.setCardNumber(cardNumber);
    request.setExpiryMonth(12);
    request.setExpiryYear(2030);
    request.setCurrency("USD");
    request.setAmount(1050);
    request.setCvv(123);
    return request;
  }

  @AfterEach
  void tearDown() {
    paymentRepository.clear();
  }
}
