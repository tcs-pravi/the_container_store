package com.containerstore.common.base;

import org.apache.log4j.Logger;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;

public final class LocalDateTimes {

    private static final Logger LOGGER = Logger.getLogger(LocalDateTimes.class);

    private LocalDateTimes() {
        throw new UnsupportedOperationException();
    }

    public static LocalDateTime toLocalDateTime(String dateString) {
        try {
             return LocalDateTime.parse(
                        dateString,
                     ISODateTimeFormat.dateTime()); // e.g. 2016-09-01T23:11:32.000-05:00
            } catch (Exception e) {
                LOGGER.info(String.format("Invalid argument: %s", dateString), e);
                return null;
            }
    }
}
