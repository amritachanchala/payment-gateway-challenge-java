package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.BankSimulatorClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.model.BankResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentRepository;
import com.checkout.payment.gateway.validator.ValidExpiryDate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@ValidExpiryDate
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentRepository paymentRepository;
  private final BankSimulatorClient bankSimulatorClient;



  public PaymentGatewayService(PaymentRepository paymentRepository,
      BankSimulatorClient bankSimulatorClient) {
    this.paymentRepository = paymentRepository;
    this.bankSimulatorClient = bankSimulatorClient;
  }

  public PostPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    return paymentRepository.get(id).orElseThrow(() -> new EventProcessingException("Invalid ID"));
  }

  /*public UUID processPayment(PostPaymentRequest paymentRequest) {
    return UUID.randomUUID();
  }*/

  public PostPaymentResponse processPayment(PostPaymentRequest paymentRequest) {

    BankResponse bankResponse = bankSimulatorClient.authorize(paymentRequest);
    PaymentStatus status;

    if (bankResponse == null) {
      status = PaymentStatus.REJECTED;
    } else if (Boolean.TRUE.equals(bankResponse.getAuthorized())) {
      status = PaymentStatus.AUTHORIZED;
    } else {
      status = PaymentStatus.DECLINED;
    }

    PostPaymentResponse paymentResponse = new PostPaymentResponse();
    paymentResponse.setId(UUID.randomUUID());
    paymentResponse.setStatus(status);
    paymentResponse.setAmount(paymentRequest.getAmount());
    paymentResponse.setExpiryMonth(paymentRequest.getExpiryMonth());
    paymentResponse.setExpiryYear(paymentRequest.getExpiryYear());
    paymentResponse.setCurrency(paymentRequest.getCurrency());
    String paymentCN = paymentRequest.getCardNumber();
    String lastFourDigits = paymentCN.substring(paymentCN.length() - 4);
    paymentResponse.setCardNumberLastFour(Integer.valueOf(lastFourDigits));
    paymentRepository.add(paymentResponse);

    return paymentResponse;
  }
}
