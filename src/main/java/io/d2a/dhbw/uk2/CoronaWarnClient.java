package io.d2a.dhbw.uk2;

import java.util.List;

public interface CoronaWarnClient {

    Token getCurrentToken();
    List<Token> getAllTokens();
    List<Token> getAllSeenTokens();
    void tokenReceived(final Token token);

}
