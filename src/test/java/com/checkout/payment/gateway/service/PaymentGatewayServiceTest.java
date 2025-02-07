package com.checkout.payment.gateway.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.client.BankSimulatorClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.model.BankResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private BankSimulatorClient bankSimulatorClient;

  @InjectMocks
  private PaymentGatewayService service;

  private PostPaymentRequest request;

  @BeforeEach
  void setUp() {
    request = new PostPaymentRequest();
    request.setCardNumber("1234567891234567");
    request.setAmount(1000);
    request.setCurrency("USD");
    request.setExpiryMonth(12);
    request.setExpiryYear(2030);
  }

  // ---------------------------
  // PROCESS PAYMENT - AUTHORIZED
  // ---------------------------
  @Test
  void shouldReturnAuthorizedPayment_whenBankApproves() {

    BankResponse bankResponse = new BankResponse();
    bankResponse.setAuthorized(true);

    when(bankSimulatorClient.authorize(request)).thenReturn(bankResponse);

    PostPaymentResponse result = service.processPayment(request);

    assertNotNull(result);
    assertEquals(PaymentStatus.AUTHORIZED, result.getStatus());
    assertEquals(request.getAmount(), result.getAmount());
    assertEquals(request.getCurrency(), result.getCurrency());

    verify(paymentRepository, times(1)).add(any(PostPaymentResponse.class));
  }

  // ---------------------------
  // PROCESS PAYMENT - DECLINED
  // ---------------------------
  @Test
  void shouldReturnDeclinedPayment_whenBankRejects() {

    BankResponse bankResponse = new BankResponse();
    bankResponse.setAuthorized(false);

    when(bankSimulatorClient.authorize(request)).thenReturn(bankResponse);

    PostPaymentResponse result = service.processPayment(request);

    assertNotNull(result);
    assertEquals(PaymentStatus.DECLINED, result.getStatus());

    verify(paymentRepository, times(1)).add(any(PostPaymentResponse.class));
  }

  // ---------------------------
  // PROCESS PAYMENT - REJECTED (NULL RESPONSE)
  // ---------------------------
  @Test
  void shouldReturnRejected_whenBankReturnsNull() {

    when(bankSimulatorClient.authorize(request)).thenReturn(null);

    PostPaymentResponse result = service.processPayment(request);

    assertNotNull(result);
    assertEquals(PaymentStatus.REJECTED, result.getStatus());

    verify(paymentRepository, times(1)).add(any(PostPaymentResponse.class));
  }

  // ---------------------------
  // GET PAYMENT BY ID - SUCCESS
  // ---------------------------
  @Test
  void shouldReturnPayment_whenIdExists() {

    UUID id = UUID.randomUUID();
    PostPaymentResponse response = new PostPaymentResponse();

    when(paymentRepository.get(id)).thenReturn(Optional.of(response));

    PostPaymentResponse result = service.getPaymentById(id);

    assertNotNull(result);
    verify(paymentRepository, times(1)).get(id);
  }

  // ---------------------------
  // GET PAYMENT BY ID - FAIL
  // ---------------------------
  @Test
  void shouldThrowException_whenPaymentNotFound() {

    UUID id = UUID.randomUUID();

    when(paymentRepository.get(id)).thenReturn(Optional.empty());

    assertThrows(EventProcessingException.class,
        () -> service.getPaymentById(id));
  }
}
