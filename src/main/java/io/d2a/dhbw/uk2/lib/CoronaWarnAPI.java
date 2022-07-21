package io.d2a.dhbw.uk2.lib;

import io.d2a.dhbw.uk2.CoronaWarnClient;
import io.d2a.dhbw.uk2.Token;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The Corona Warn API by DHBW lecturers.
 * And contrary to Apple and (currently) Google: it's open source! ;-)
 *
 * Implemented with Java 1.7 features only
 *
 * The missing types (Token and CoronaWarnClient) will be implemented during exam!
 */
public class CoronaWarnAPI {

	/**
	 * All known tokens
	 */
	private static final Set<Token> tokens = new HashSet<>();

	/**
	 * Tokens of clients that send infection state!
	 */
	private static final List<Token> infectionTokens = new LinkedList<>();

	/**
	 * All registered clients will get information on received tokens
	 */
	private static final List<CoronaWarnClient> clients = new LinkedList<>();

	/**
	 * Add clients to be notified when the API received a token.
	 * <b>If multiple clients are provided, they will receive the others token instantaneously!</b>
	 * @param clients listener to add
	 */
	public static void registerClients(CoronaWarnClient... clients) {
		for (CoronaWarnClient client : clients ) {
			if ( !CoronaWarnAPI.clients.contains(client) ) {
				CoronaWarnAPI.clients.add(client);
			}
		}
		for (CoronaWarnClient client : clients ) {
			CoronaWarnAPI.sendToken(client);
		}
	}

	/**
	 * Send your token. Tokens that are sent multiple times <b>will only be broadcasted once!</b>
	 *
	 * @param client the client
	 */
	public static void sendToken(CoronaWarnClient client) {
		if ( !CoronaWarnAPI.clients.isEmpty() ) {
			final Token token = client.getCurrentToken();
			if ( CoronaWarnAPI.tokens.add(token) ) {
				for ( CoronaWarnClient c : CoronaWarnAPI.clients ) {
					if ( c != client ) {
						c.tokenReceived(token);
					}
				}
			}
		}
	}

	/**
	 * Report that the user of the provided client was diagnosed positive
	 *
	 * This will add all tokens that are <b>not older than 30 seconds</b> to list of infected tokens
	 *
	 * @param client the client
	 */
	public static void reportInfection(CoronaWarnClient client) {
		List<Token> tokens = new LinkedList<>();

		// add all client tokens
		List<Token> clientTokens = client.getAllTokens();
		if ( clientTokens != null ) {
			tokens.addAll(clientTokens);
		}

		// add current if not contained
		Token current = client.getCurrentToken();
		if ( current != null && !tokens.contains(current)) {
			tokens.add(current);
		}

		// add to infection tokens if in report range
		for ( Token token : tokens ) {
			if ( CoronaWarnAPI.isInReportRange(token) ) {
				CoronaWarnAPI.infectionTokens.add(token);
			}
		}
	}

	/**
	 * Check if the provided client has seen tokens that were marked as infected and <b>are were generated at most 30 seconds ago</b>.
	 *
	 * @param client the client
	 *
	 * @return <code>true</code> if possible infection detected, <code>false</code> otherwise
	 */
	public static boolean checkInfection(CoronaWarnClient client){
		List<Token> seenTokens = client.getAllSeenTokens();
		if ( seenTokens != null ) {
			for ( Token seenToken : seenTokens ) {
				if (CoronaWarnAPI.isInReportRange(seenToken) ) {
					for ( Token infectedToken : CoronaWarnAPI.infectionTokens ) {
						if ( CoronaWarnAPI.isInReportRange(infectedToken) && CoronaWarnAPI.isInInfectionRange(seenToken,infectedToken) ) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Check if token is in report range (means at most 90000ms old)
	 * @param token token to check
	 * @return <code>true</code> if token is in report range, <code>false</code> otherwise
	 */
	private static boolean isInReportRange(Token token) {
		if ( token != null ) {
			Date date = token.date;
			if ( date != null ) {
				return (System.currentTimeMillis() - token.date.getTime()) < 90000;
			}
		}
		return false;
	}

	/**
	 * Check if difference of time of both tokens is at most 30000ms
	 * @param token1 first token
	 * @param token2 second token
	 * @return <code>true</code> if time difference is below 30000ms, <code>false</code> otherwise
	 */
	private static boolean isInInfectionRange(Token token1, Token token2) {
		return Math.abs(token1.date.getTime()-token2.date.getTime()) < 30000;
	}

}
