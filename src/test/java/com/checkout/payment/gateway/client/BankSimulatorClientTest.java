package com.checkout.payment.gateway.client;
import com.checkout.payment.gateway.model.BankRequest;
import com.checkout.payment.gateway.model.BankResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankSimulatorClientTest {
  @Mock
  private RestTemplate restTemplate;
  @InjectMocks
  private BankSimulatorClient client;

  @Test
  void shouldReturnBankResponse_whenBankCallIsSuccessful() {

    PostPaymentRequest request = new PostPaymentRequest();

    BankResponse mockResponse = new BankResponse();

    ResponseEntity<BankResponse> responseEntity =
        ResponseEntity.ok(mockResponse);

    when(restTemplate.postForEntity(
        anyString(),
        any(BankRequest.class),
        eq(BankResponse.class)
    )).thenReturn(responseEntity);

    BankResponse result = client.authorize(request);

    assertNotNull(result);
    assertEquals(mockResponse, result);

    verify(restTemplate, times(1))
        .postForEntity(anyString(), any(BankRequest.class), eq(BankResponse.class));
  }

  @Test
  void shouldReturnNull_whenRestTemplateThrowsException() {

    PostPaymentRequest request = new PostPaymentRequest();

    when(restTemplate.postForEntity(
        anyString(),
        any(),
        eq(BankResponse.class)
    )).thenThrow(new RuntimeException("Bank down"));

    BankResponse result = client.authorize(request);

    assertNull(result);
  }
}
