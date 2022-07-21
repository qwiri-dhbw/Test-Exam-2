package io.d2a.dhbw.uk2;

import java.io.File;

public record JPhone(String id, String owner) {

    public File getTokenFile() {
        return new File(this.id + "-tokens.txt");
    }

}
