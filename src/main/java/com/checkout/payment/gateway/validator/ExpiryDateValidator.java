package com.checkout.payment.gateway.validator;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.YearMonth;

public class ExpiryDateValidator implements ConstraintValidator<ValidExpiryDate, PostPaymentRequest> {

  @Override
  public boolean isValid(PostPaymentRequest request, ConstraintValidatorContext context) {

    if (request == null) {
      return true;
    }

    if (request.getExpiryMonth() == null || request.getExpiryYear() == null) {
      return true; // handled by @NotNull
    }

    YearMonth now = YearMonth.now();

    YearMonth expiry;
    try {
      expiry = YearMonth.of(request.getExpiryYear(), request.getExpiryMonth());
    } catch (Exception e) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Invalid expiry date")
          .addConstraintViolation();
      return false;
    }

    if (expiry.isBefore(now) || expiry.equals(now)) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Card is expired")
          .addPropertyNode("expiryMonth")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
