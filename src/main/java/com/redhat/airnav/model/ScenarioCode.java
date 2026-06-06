package com.redhat.airnav.model;

public enum ScenarioCode {
    AD_CLS("AD.CLS"),
    AD_LIM("AD.LIM"),
    RWY_CLS("RWY.CLS"),
    RWY_LIM("RWY.LIM"),
    TWY_CLS("TWY.CLS"),
    APR_CLS("APR.CLS"),
    ASP_ACT("ASP.ACT"),
    NAV_TEMP("NAV.TEMP"),
    NAV_UNS("NAV.UNS"),
    OBS_NEW("OBS.NEW"),
    OBST_CHG("OBST.CHG"),
    SVC_CLS("SVC.CLS"),
    ATS_CHG("ATS.CHG"),
    FREQ_CHG("FREQ.CHG"),
    SAA_ACT("SAA.ACT"),
    SAA_NEW("SAA.NEW"),
    RCP_CHG("RCP.CHG"),
    APN_CLS("APN.CLS"),
    SFC_CON("SFC.CON"),
    STAND_CLS("STAND.CLS"),
    WLD_HZD("WLD.HZD"),
    ;

    private final String code;

    ScenarioCode(String code) {
        this.code = code;
    }

    public static ScenarioCode fromCode(String code) {
        for (ScenarioCode value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown scenario code: " + code);
    }

    public String getCode() {
        return code;
    }
}
