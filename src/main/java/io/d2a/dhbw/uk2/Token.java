package io.d2a.dhbw.uk2;

import java.util.Date;
import java.util.UUID;

public class Token {

    public final String value;
    public final Date date;

    public Token() {
        this.value = UUID.randomUUID().toString();
        this.date = new Date();
    }

    public Token(final String value, final Date date) {
        this.value = value;
        this.date = date;
    }

    @Override
    public String toString() {
        return String.format("%s @ %s", this.value, this.date.toString());
    }
}
