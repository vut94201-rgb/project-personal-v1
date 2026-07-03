package com.personal.identity.jpa.support.converter;

import jakarta.persistence.AttributeConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractStringCodeEnumConverter<E extends Enum<E> & StringCodeEnum>
    implements AttributeConverter<E, String> {
  private final Class<E> enumType;
  private final Map<String, E> valuesByCode;

  protected AbstractStringCodeEnumConverter(Class<E> enumType) {
    this.enumType = Objects.requireNonNull(enumType, "enumType must not be null");
    this.valuesByCode = buildValuesByCode(enumType);
  }

  @Override
  public String convertToDatabaseColumn(E attribute) {
    return Objects.isNull(attribute) ? null : attribute.getCode();
  }

  @Override
  public E convertToEntityAttribute(String dbData) {
    if (Objects.isNull(dbData)) return null;
    if (dbData.isBlank())
      throw new IllegalArgumentException(
          "Blank code is not valid for enum " + enumType.getSimpleName());
    E enumValue = valuesByCode.get(dbData);
    if (Objects.isNull(enumValue))
      throw new IllegalArgumentException(
          "Unknown code '%s' for enum %s".formatted(dbData, enumType.getSimpleName()));
    return enumValue;
  }

  private static <E extends Enum<E> & StringCodeEnum> Map<String, E> buildValuesByCode(
      Class<E> enumType) {
    Map<String, E> result = new HashMap<>();

    for (E enumValue : enumType.getEnumConstants()) {
      String code =
          Objects.requireNonNull(
              enumValue.getCode(),
              () -> "Code must not be null: " + enumType.getSimpleName() + "." + enumValue.name());
      if (code.isBlank()) {
        throw new IllegalStateException(
            "Code must not be blank: " + enumType.getSimpleName() + "." + enumValue.name());
      }
      E existingValue = result.putIfAbsent(code, enumValue);
      if (Objects.nonNull(existingValue)) {
        throw new IllegalStateException(
            "Duplicate code '%s' in enum %s: %s and %s"
                .formatted(code, enumType.getSimpleName(), existingValue.name(), enumValue.name()));
      }
    }
    return Map.copyOf(result);
  }
}
