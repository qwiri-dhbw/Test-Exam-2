package io.d2a.dhbw.uk2;

import io.d2a.dhbw.uk2.lib.CoronaWarn;
import io.d2a.dhbw.uk2.lib.CoronaWarnAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class CoronaWarnTerm extends JFrame implements CoronaWarnClient {

    private final JPhone phone;
    private final List<Token> nearbyTokens;
    private final List<Token> ownTokens;

    private WarnStatus warnStatus;
    private Token currentToken;

    private final JLabel statusLabel;
    private final JButton newTokenButton;
    private final JButton checkButton;
    private final JButton clearButton;
    private final JButton reportButton;
    private final JLabel seenLabel;

    private void updateStatus(final WarnStatus newStatus) {
        this.statusLabel.setBackground((this.warnStatus = newStatus).color);
        this.statusLabel.setText(this.warnStatus.text);
    }

    private void updateSeenTokens() {
        this.seenLabel.setText("Seen Tokens: " + this.getAllSeenTokens().size());
    }

    private void generateNewSelfToken(final Object _ev) {
        // generate new token
        this.currentToken = new Token();
        // save generated token to token list
        this.ownTokens.add(this.currentToken);
        // save token to file
        CoronaWarn.saveToken(this.phone, this.currentToken);
        // send token to other clients
        CoronaWarnAPI.sendToken(this);
        // update 'seen tokens' label
        this.seenLabel.setToolTipText(this.currentToken.toString());
    }

    private void handleCheckButton(final ActionEvent event) {
        this.updateStatus(CoronaWarnAPI.checkInfection(this) ? WarnStatus.ALARM : WarnStatus.OK);
    }

    private void handleClearButton(final ActionEvent event) {
        this.ownTokens.clear();
        this.nearbyTokens.clear();
        CoronaWarn.clearTokenStore(this.phone);
        this.generateNewSelfToken(event);
        this.updateSeenTokens();
    }

    private void handleReportButton(final ActionEvent event) {
        this.updateStatus(WarnStatus.INFECTED);
        // disable any buttons
        this.newTokenButton.setEnabled(false);
        this.checkButton.setEnabled(false);
        this.clearButton.setEnabled(false);
        this.reportButton.setEnabled(false);
        // report infection to other clients
        CoronaWarnAPI.reportInfection(this);
    }

    public CoronaWarnTerm(final JPhone phone) {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.nearbyTokens = new ArrayList<>();
        this.phone = phone;
        this.ownTokens = new ArrayList<>(CoronaWarn.loadTokens(phone));

        // set title to owner's name
        this.setTitle(phone.owner());

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        final JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        statusPanel.setOpaque(true);

        this.statusLabel = new JLabel();
        this.statusLabel.setOpaque(true);
        this.statusLabel.setPreferredSize(new Dimension(0, 100));
        this.statusLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(this.statusLabel, BorderLayout.NORTH);
        this.updateStatus(WarnStatus.UNKNOWN);

        // buttons
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(4, 1));

        this.newTokenButton = new JButton("New Token");
        this.newTokenButton.addActionListener(this::generateNewSelfToken);
        buttonPanel.add(this.newTokenButton);

        this.checkButton = new JButton("Check for infections");
        this.checkButton.addActionListener(this::handleCheckButton);
        buttonPanel.add(this.checkButton);

        this.clearButton = new JButton("Clear tokens");
        this.clearButton.addActionListener(this::handleClearButton);
        buttonPanel.add(this.clearButton);

        this.reportButton = new JButton("Report infection");
        this.reportButton.addActionListener(this::handleReportButton);
        buttonPanel.add(this.reportButton, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.CENTER);

        this.seenLabel = new JLabel("Seen Tokens: 0");
        this.updateSeenTokens();
        panel.add(this.seenLabel, BorderLayout.SOUTH);

        // generate initial token
        this.generateNewSelfToken(null);

        this.add(panel);
        this.setVisible(true);

        // fit size to components
        this.pack();

        // auto generator
        (new Timer()).schedule(new TimerTask() {
            @Override
            public void run() {
                if (CoronaWarnTerm.this.warnStatus == WarnStatus.INFECTED) {
                    this.cancel();
                    return;
                }
                System.out.println("Auto renewing token for client " + CoronaWarnTerm.this.phone);
                CoronaWarnTerm.this.generateNewSelfToken(null);
            }
        }, TimeUnit.SECONDS.toMillis(5), TimeUnit.SECONDS.toMillis(5));
    }

    @Override
    public Token getCurrentToken() {
        return this.currentToken;
    }

    @Override
    public List<Token> getAllTokens() {
        return this.ownTokens;
    }

    @Override
    public List<Token> getAllSeenTokens() {
        return this.nearbyTokens;
    }

    @Override
    public void tokenReceived(Token token) {
        this.nearbyTokens.add(token);
        this.updateSeenTokens();
    }

}
