package com.containerstore.offer.domain;

import java.util.EnumSet;
import java.util.Set;

public enum CpcSpaceSource {

    // Design Center Online
    DC_DOOR_WALL_RACK("DC_WEB"),
    DC_CLOSET_TOOL("DC_WDC"),
    DC_ELFA_DESIGN_CENTER("DC_EDC"),
    DC_AVERA("DC_ADC"),

    // The Stow Company. Third party Laren design tool.
    STOW_LAREN("STOW"),

    // Legacy Sources
    LEGACY_DC_MOBILE("EODC"),
    NEXT_GEN_CDC_SOURCE("NEXTGENCDC");

    private static final Set<CpcSpaceSource> EMPLOYEE_DESIGNED_SOURCES
            = EnumSet.of(DC_ELFA_DESIGN_CENTER, DC_AVERA, NEXT_GEN_CDC_SOURCE, STOW_LAREN);

    private static final Set<CpcSpaceSource> DESIGNED_ELFA_SOURCES
            = EnumSet.of(DC_DOOR_WALL_RACK, DC_CLOSET_TOOL, DC_ELFA_DESIGN_CENTER,
                         LEGACY_DC_MOBILE, NEXT_GEN_CDC_SOURCE);

    private final String source;

    CpcSpaceSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return this.source;
    }

    public static boolean isEmployeeDesignedSource(String source) {
        return EMPLOYEE_DESIGNED_SOURCES
                .stream()
                .map(CpcSpaceSource::getSource)
                .anyMatch(s -> s.equals(source));
    }

    public static boolean isDesignedElfaSource(String source) {
        return DESIGNED_ELFA_SOURCES
                .stream()
                .map(CpcSpaceSource::getSource)
                .anyMatch(s -> s.equals(source));
    }
}
