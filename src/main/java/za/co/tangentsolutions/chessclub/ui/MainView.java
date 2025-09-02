package za.co.tangentsolutions.chessclub.ui;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import za.co.tangentsolutions.chessclub.models.Game;
import za.co.tangentsolutions.chessclub.models.Member;
import za.co.tangentsolutions.chessclub.services.MemberService;
import za.co.tangentsolutions.chessclub.services.RankingService;

import java.util.List;

@Route("")
public class MainView extends VerticalLayout {

    private final MemberService memberService;
    private final RankingService rankingService;

    private Grid<Member> membersGrid = new Grid<>(Member.class);
    private Grid<Game> gamesGrid = new Grid<>();
    private VerticalLayout contentLayout = new VerticalLayout();

    private static final Logger logger = LogManager.getLogger(MainView.class);

    @Autowired
    public MainView(MemberService memberService, RankingService rankingService) {
        this.memberService = memberService;
        this.rankingService = rankingService;

        setSizeFull();
        setPadding(true);

        add(new H1("Chess Club Administration"));

        // Tabs for different views
        Tab membersTab = new Tab("Members");
        Tab gamesTab = new Tab("Match History");
        Tabs tabs = new Tabs(membersTab, gamesTab);

        tabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab() == membersTab) {
                showMembersView();
            } else {
                showGamesView();
            }
        });

        // Action buttons (always visible)
        Button addMemberBtn = new Button("Add Member", e -> showAddMemberDialog());
        Button recordMatchBtn = new Button("Record Match", e -> showRecordMatchDialog());
        Button refreshBtn = new Button("Refresh", e -> refreshAll());

        HorizontalLayout buttonLayout = new HorizontalLayout(addMemberBtn, recordMatchBtn, refreshBtn);
        buttonLayout.setSpacing(true);

        // Configure grids
        configureMembersGrid();
        configureGamesGrid();

        add(tabs, buttonLayout, contentLayout);
        showMembersView();
    }

    private void configureMembersGrid() {
        membersGrid.setColumns("rank", "name", "surname", "email", "birthday", "gamesPlayed");
        membersGrid.getColumnByKey("rank").setHeader("Rank");
        membersGrid.getColumnByKey("gamesPlayed").setHeader("Games Played");
        membersGrid.getColumns().forEach(col -> col.setAutoWidth(true));

        // Add view history button
        membersGrid.addComponentColumn(member -> {
            Button historyBtn = new Button("History", e -> showPlayerHistory(member));
            return historyBtn;
        });
    }

    private void configureGamesGrid() {
        logger.info("Configuring games grid");
        // Clear any existing columns to ensure custom configuration
        gamesGrid.removeAllColumns();
        
        gamesGrid.addColumn(game -> game.getPlayedAt().toString()).setHeader("Date");
        gamesGrid.addColumn(game -> game.getPlayer1().getFullName()).setHeader("Player 1");
        gamesGrid.addColumn(game -> game.getPlayer1Score()).setHeader("P1 Score");
        gamesGrid.addColumn(game -> game.getPlayer2().getFullName()).setHeader("Player 2");
        gamesGrid.addColumn(game -> game.getPlayer2Score()).setHeader("P2 Score");
        gamesGrid.addColumn(game -> {
            if (game.isDraw()) return "Draw";
            Member winner = game.getWinner();
            return winner != null ? winner.getFullName() + " wins" : "Unknown";
        }).setHeader("Result");
        gamesGrid.addColumn(game -> game.getPlayer1RankBefore() + " → " + game.getPlayer1RankAfter())
                .setHeader("P1 Rank Change");
        gamesGrid.addColumn(game -> game.getPlayer2RankBefore() + " → " + game.getPlayer2RankAfter())
                .setHeader("P2 Rank Change");

        gamesGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        logger.info("Games grid configured with {} columns", gamesGrid.getColumns().size());
    }

    private void showMembersView() {
        contentLayout.removeAll();
        refreshMembersGrid();
        contentLayout.add(membersGrid);
    }

    private void showGamesView() {
        logger.info("Showing games view");
        contentLayout.removeAll();
        refreshGamesGrid();
        contentLayout.add(gamesGrid);
        logger.info("Games grid added to content layout");
    }

    private void refreshAll() {
        refreshMembersGrid();
        refreshGamesGrid();
    }

    private void refreshMembersGrid() {
        logger.info("Refreshing members grid");
        List<Member> members = memberService.getAllMembers();
        membersGrid.setItems(members);
    }

    private void refreshGamesGrid() {
        logger.info("Refreshing games grid");
        List<Game> games = rankingService.getGameHistory();
        logger.info("Found {} games", games.size());
        if (!games.isEmpty()) {
            Game firstGame = games.get(0);
            logger.info("First game - Player1: {}, Player2: {}", 
                firstGame.getPlayer1() != null ? firstGame.getPlayer1().getFullName() : "null",
                firstGame.getPlayer2() != null ? firstGame.getPlayer2().getFullName() : "null");
        }
        gamesGrid.setItems(games);
    }

    private void showPlayerHistory(Member member) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");

        Grid<Game> historyGrid = new Grid<>(Game.class);
        historyGrid.setItems(memberService.getPlayerGameHistory(member.getId()));

        historyGrid.addColumn(game -> game.getPlayedAt().toString()).setHeader("Date");
        historyGrid.addColumn(game -> {
            if (game.getPlayer1().equals(member)) return game.getPlayer2().getFullName();
            return game.getPlayer1().getFullName();
        }).setHeader("Opponent");
        historyGrid.addColumn(game -> {
            if (game.getPlayer1().equals(member)) return game.getPlayer1Score() + " - " + game.getPlayer2Score();
            return game.getPlayer2Score() + " - " + game.getPlayer1Score();
        }).setHeader("Score");
        historyGrid.addColumn(game -> {
            if (game.getPlayer1().equals(member)) {
                return game.getPlayer1RankBefore() + " → " + game.getPlayer1RankAfter();
            } else {
                return game.getPlayer2RankBefore() + " → " + game.getPlayer2RankAfter();
            }
        }).setHeader("Rank Change");
        historyGrid.addColumn(game -> {
            if (game.isDraw()) return "Draw";
            Member winner = game.getWinner();
            if (winner != null && winner.equals(member)) return "Won";
            return "Lost";
        }).setHeader("Result");

        dialog.add(new H3("Match History: " + member.getFullName()), historyGrid);
        dialog.open();
    }

    private void refreshGrid() {
        List<Member> members = memberService.getAllMembers();
        membersGrid.setItems(members);
    }

    private void showErrorNotification(String title, String message, Exception ex) {
        Dialog errorDialog = new Dialog();
        errorDialog.setWidth("500px");

        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("font-weight", "bold");
        titleSpan.getStyle().set("color", "red");

        Span messageSpan = new Span(message);

        Details details = new Details("Technical Details", new Span(ex.toString()));
        details.setOpened(false);

        Button closeBtn = new Button("Close", e -> errorDialog.close());

        errorDialog.add(titleSpan, messageSpan, details, closeBtn);
        errorDialog.open();
    }

    private void showAddMemberDialog() {
        Dialog dialog = new Dialog();
        FormLayout form = new FormLayout();

        TextField nameField = new TextField("Name");
        TextField surnameField = new TextField("Surname");
        TextField emailField = new TextField("Email");
        DatePicker birthdayPicker = new DatePicker("Birthday");

        Button saveBtn = new Button("Save", e -> {
            try {
                // Validate required fields
                if (nameField.getValue() == null || nameField.getValue().trim().isEmpty()) {
                    Notification.show("Name is required", 3000, Notification.Position.MIDDLE);
                    return;
                }
                if (surnameField.getValue() == null || surnameField.getValue().trim().isEmpty()) {
                    Notification.show("Surname is required", 3000, Notification.Position.MIDDLE);
                    return;
                }
                if (emailField.getValue() == null || emailField.getValue().trim().isEmpty()) {
                    Notification.show("Email is required", 3000, Notification.Position.MIDDLE);
                    return;
                }
                if (birthdayPicker.getValue() == null) {
                    Notification.show("Birthday is required", 3000, Notification.Position.MIDDLE);
                    return;
                }

                Member member = new Member();
                member.setName(nameField.getValue().trim());
                member.setSurname(surnameField.getValue().trim());
                member.setEmail(emailField.getValue().trim());
                member.setBirthday(birthdayPicker.getValue());

                Member savedMember = memberService.createMember(member);
                if (savedMember != null && savedMember.getId() != null) {
                    refreshGrid();
                    dialog.close();
                    Notification.show("Member added successfully!", 3000, Notification.Position.MIDDLE);
                } else {
                    Notification.show("Failed to create member. Please try again.", 5000, Notification.Position.MIDDLE);
                }
            } catch (Exception ex) {
                String errorMessage = "Error creating member: " + ex.getMessage();
                if (ex.getMessage() != null && ex.getMessage().contains("Email is already registered")) {
                    errorMessage = "Email is already registered. Please use a different email address.";
                    Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
                } else {
                    showErrorNotification("Error Creating Member", errorMessage, ex);
                }
                System.err.println("Error creating member: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        form.add(nameField, surnameField, emailField, birthdayPicker);
        dialog.add(form, new HorizontalLayout(saveBtn, cancelBtn));
        dialog.open();
    }

    private void showRecordMatchDialog() {
        Dialog dialog = new Dialog();
        FormLayout form = new FormLayout();

        List<Member> members = memberService.getAllMembers();

        ComboBox<Member> player1Combo = new ComboBox<>("Player 1");
        player1Combo.setItems(members);
        player1Combo.setItemLabelGenerator(Member::getFullName);

        ComboBox<Member> player2Combo = new ComboBox<>("Player 2");
        player2Combo.setItems(members);
        player2Combo.setItemLabelGenerator(Member::getFullName);

        TextField player1ScoreField = new TextField("Player 1 Score");
        TextField player2ScoreField = new TextField("Player 2 Score");

        Button saveBtn = new Button("Record Match", e -> {
            try {
                Member player1 = player1Combo.getValue();
                Member player2 = player2Combo.getValue();

                if (player1 == null || player2 == null || player1.equals(player2)) {
                    Notification.show("Please select two different players", 3000, Notification.Position.MIDDLE);
                    return;
                }

                if (player1ScoreField.getValue() == null || player1ScoreField.getValue().trim().isEmpty()) {
                    Notification.show("Player 1 score is required", 3000, Notification.Position.MIDDLE);
                    return;
                }
                if (player2ScoreField.getValue() == null || player2ScoreField.getValue().trim().isEmpty()) {
                    Notification.show("Player 2 score is required", 3000, Notification.Position.MIDDLE);
                    return;
                }

                int player1Score = Integer.parseInt(player1ScoreField.getValue());
                int player2Score = Integer.parseInt(player2ScoreField.getValue());

                if (player1Score < 0 || player2Score < 0) {
                    Notification.show("Scores must be non-negative", 3000, Notification.Position.MIDDLE);
                    return;
                }

                // Record the match and update rankings
                Game recordedGame = rankingService.recordMatch(player1.getId(), player2.getId(), player1Score, player2Score);
                
                if (recordedGame != null && recordedGame.getId() != null) {
                    refreshAll(); // Refresh both members and games grids
                    dialog.close();
                    Notification.show("Match recorded successfully! Rankings updated.", 3000, Notification.Position.MIDDLE);
                } else {
                    Notification.show("Failed to record match. Please try again.", 5000, Notification.Position.MIDDLE);
                }
            } catch (NumberFormatException ex) {
                Notification.show("Please enter valid numeric scores", 3000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show("Error recording match: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
                System.err.println("Error recording match: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        form.add(player1Combo, player2Combo, player1ScoreField, player2ScoreField);
        dialog.add(form, new HorizontalLayout(saveBtn, cancelBtn));
        dialog.open();
    }
}