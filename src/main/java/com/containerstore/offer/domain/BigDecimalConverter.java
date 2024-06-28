package com.containerstore.offer.domain;

import com.containerstore.common.base.money.Money;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

public class BigDecimalConverter {
    private static final Logger LOGGER = Logger.getLogger(BigDecimalConverter.class);

    private BigDecimalConverter() {
        throw new UnsupportedOperationException();
    }

    public static Optional<BigDecimal> fromObject(Object objectToConvert) {
        try {
            if (objectToConvert instanceof BigDecimal) {
                return Optional.of((BigDecimal) objectToConvert);
            } else if (objectToConvert instanceof Money) {
                return Optional.of(((Money) objectToConvert).get());
            } else if (objectToConvert instanceof Integer) {
                return Optional.of(new BigDecimal((int) objectToConvert));
            } else if (objectToConvert instanceof Float) {
                return Optional.of(BigDecimal.valueOf((float) objectToConvert));
            } else if (objectToConvert instanceof Double) {
                return Optional.of(BigDecimal.valueOf((double) objectToConvert));
            } else if (objectToConvert instanceof Long) {
                return Optional.of(BigDecimal.valueOf((long) objectToConvert));
            } else if (objectToConvert instanceof String) {
                return Optional.of(new BigDecimal((String) objectToConvert));
            } else if (objectToConvert instanceof BigInteger) {
                return Optional.of(new BigDecimal((BigInteger) objectToConvert));
            }
            LOGGER.info(String.format("No suitable constructor found to convert %s (%s) into a BigDecimal",
                    Objects.toString(objectToConvert), nullSafeClassName(objectToConvert)));
            return Optional.empty();
        } catch (NumberFormatException nfe) {
            LOGGER.error(nfe);
            return Optional.empty();
        }
    }

    private static String nullSafeClassName(Object object) {
        return object == null ? "Null" : object.getClass().getName();
    }
}
