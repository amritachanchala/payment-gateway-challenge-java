package com.checkout.payment.gateway.validator;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class PaymentValidator {

  private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "GBP", "EUR");

  public List<String> validate(PostPaymentRequest request) {
    List<String> errors = new ArrayList<>();

   // validateCardNumber(request.getCardNumberLastFour(), errors);
    validateExpiryMonth(request.getExpiryMonth(), errors);
    validateExpiryYear(request.getExpiryYear(), errors);
    validateExpiryDate(request.getExpiryMonth(), request.getExpiryYear(), errors);
    validateCurrency(request.getCurrency(), errors);
    validateAmount(request.getAmount(), errors);
    //validateCvv(request.getCvv(), errors);

    return errors;
  }

  private void validateCardNumber(String cardNumber, List<String> errors) {
    if (cardNumber == null || cardNumber.isBlank()) {
      errors.add("Card number is required");
      return;
    }
    if (!cardNumber.matches("\\d+")) {
      errors.add("Card number must contain only numeric characters");
    }
    if (cardNumber.length() < 14 || cardNumber.length() > 19) {
      errors.add("Card number must be between 14 and 19 digits");
    }
  }

  private void validateExpiryMonth(Integer expiryMonth, List<String> errors) {
    if (expiryMonth == null) {
      errors.add("Expiry month is required");
      return;
    }
    if (expiryMonth < 1 || expiryMonth > 12) {
      errors.add("Expiry month must be between 1 and 12");
    }
  }

  private void validateExpiryYear(Integer expiryYear, List<String> errors) {
    if (expiryYear == null) {
      errors.add("Expiry year is required");
    }
  }

  private void validateExpiryDate(Integer expiryMonth, Integer expiryYear, List<String> errors) {
    if (expiryMonth == null || expiryYear == null) {
      return; // already caught by individual validations
    }
    YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
    if (!expiry.isAfter(YearMonth.now())) {
      errors.add("Card expiry date must be in the future");
    }
  }

  private void validateCurrency(String currency, List<String> errors) {
    if (currency == null || currency.isBlank()) {
      errors.add("Currency is required");
      return;
    }
    if (currency.length() != 3) {
      errors.add("Currency must be 3 characters");
      return;
    }
    if (!SUPPORTED_CURRENCIES.contains(currency.toUpperCase())) {
      errors.add("Currency must be one of: " + String.join(", ", SUPPORTED_CURRENCIES));
    }
  }

  private void validateAmount(Integer amount, List<String> errors) {
    if (amount == null) {
      errors.add("Amount is required");
      return;
    }
    if (amount <= 0) {
      errors.add("Amount must be a positive integer");
    }
  }

  private void validateCvv(String cvv, List<String> errors) {
    if (cvv == null || cvv.isBlank()) {
      errors.add("CVV is required");
      return;
    }
    if (!cvv.matches("\\d+")) {
      errors.add("CVV must contain only numeric characters");
    }
    if (cvv.length() < 3 || cvv.length() > 4) {
      errors.add("CVV must be 3 or 4 digits");
    }
  }
}