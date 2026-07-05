package com.personal.shared.utility;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.personal.shared.exception.InvalidPhoneNumberException;

import java.util.Objects;

public final class PhoneNumberNormalizer {
  private static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

  private PhoneNumberNormalizer() {}

  public static String normalize(String rawPhoneNumber, String defaultRegion) {
    if (rawPhoneNumber == null || rawPhoneNumber.isBlank()) {
      return null;
    }
    if (Objects.isNull(defaultRegion) || defaultRegion.isBlank()) defaultRegion = "VN";
    try {
      Phonenumber.PhoneNumber parsed =
          PHONE_NUMBER_UTIL.parse(rawPhoneNumber.trim(), defaultRegion);

      if (!PHONE_NUMBER_UTIL.isValidNumber(parsed)) {
        throw new InvalidPhoneNumberException(rawPhoneNumber);
      }

      return PHONE_NUMBER_UTIL.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);

    } catch (NumberParseException exception) {
      throw new InvalidPhoneNumberException(rawPhoneNumber, exception);
    }
  }

  public static String normalize(String rawPhoneNumber) {
    if (rawPhoneNumber == null || rawPhoneNumber.isBlank()) {
      return null;
    }
    String defaultRegion = "VN";
    try {
      Phonenumber.PhoneNumber parsed =
          PHONE_NUMBER_UTIL.parse(rawPhoneNumber.trim(), defaultRegion);

      if (!PHONE_NUMBER_UTIL.isValidNumber(parsed)) {
        throw new InvalidPhoneNumberException(rawPhoneNumber);
      }

      return PHONE_NUMBER_UTIL.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);

    } catch (NumberParseException exception) {
      throw new InvalidPhoneNumberException(rawPhoneNumber, exception);
    }
  }
}
