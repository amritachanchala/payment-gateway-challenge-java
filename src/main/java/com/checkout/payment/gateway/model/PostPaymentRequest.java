package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.validator.ValidExpiryDate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
@ValidExpiryDate
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostPaymentRequest implements Serializable {

  @JsonProperty("card_number")
  @NotBlank(message = "Card Number is required")
  @Pattern(regexp = "\\d{14,19}", message = "Card number must be 14-19 digits")
  private String cardNumber;
  @JsonProperty("expiry_month")
  @NotNull(message = "Expiry month is required")
  @Min(value = 1, message = "Expiry month must be between 1 and 12")
  @Max(value = 12, message = "Expiry month must be between 1 and 12")
  private Integer expiryMonth;
  @JsonProperty("expiry_year")
  @NotNull(message = "Expiry year is required")
  private Integer expiryYear;
  @NotBlank(message = "Currency is required")
  @Size(min = 3, max = 3, message = "Currency must be 3 characters")
  @Pattern(
      regexp = "USD|EUR|GBP",
      message = "Currency must be one of: USD, EUR, GBP"
  )
  private String currency;
  @NotNull(message = "Amount is required")
  @Positive(message = "Amount must be greater than 0")
  private Integer amount;
  @NotNull(message = "CVV is required")
  @Min(value = 100, message = "CVV must be 3-4 digits")
  @Max(value = 9999, message = "CVV must be 3-4 digits")
  private Integer cvv;

  @JsonProperty("expiry_date")
  public String getExpiryDate() {
    return String.format("%d/%d", expiryMonth, expiryYear);
  }


}
