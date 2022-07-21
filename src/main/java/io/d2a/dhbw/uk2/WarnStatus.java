package io.d2a.dhbw.uk2;

import java.awt.*;

public enum WarnStatus {
    UNKNOWN("Unknown", new Color(175, 175, 175)),
    OK("Ok", new Color(100, 200, 100)),
    ALARM("Possible encounter", new Color(255, 100, 100)),
    INFECTED("In quarantine", new Color(150, 150, 255));

    public final String text;
    public final Color color;

    WarnStatus(final String text, final Color color) {
        this.text = text;
        this.color = color;
    }
}