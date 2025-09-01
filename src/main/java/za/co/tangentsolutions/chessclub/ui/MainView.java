package za.co.tangentsolutions.chessclub.ui;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import za.co.tangentsolutions.chessclub.models.Member;
import za.co.tangentsolutions.chessclub.services.MemberService;
import za.co.tangentsolutions.chessclub.services.RankingService;

import java.util.List;

@Route("")
public class MainView extends VerticalLayout {
    
    private final MemberService memberService;
    private final RankingService rankingService;
    private Grid<Member> grid = new Grid<>(Member.class);
    
    @Autowired
    public MainView(MemberService memberService, RankingService rankingService) {
        this.memberService = memberService;
        this.rankingService = rankingService;
        
        setSizeFull();
        setPadding(true);
        
        add(new H1("Chess Club Administration"));
        
        // Buttons
        Button addMemberBtn = new Button("Add Member", e -> showAddMemberDialog());
        Button recordMatchBtn = new Button("Record Match", e -> showRecordMatchDialog());
        Button refreshBtn = new Button("Refresh", e -> refreshGrid());
        
        HorizontalLayout buttonLayout = new HorizontalLayout(addMemberBtn, recordMatchBtn, refreshBtn);
        buttonLayout.setSpacing(true);
        
        // Configure grid
        grid.setColumns("rank", "name", "surname", "email", "birthday", "gamesPlayed");
        grid.getColumnByKey("rank").setHeader("Rank");
        grid.getColumnByKey("gamesPlayed").setHeader("Games Played");
        
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        
        add(buttonLayout, grid);
        refreshGrid();
    }
    
    private void refreshGrid() {
        List<Member> members = memberService.getAllMembers();
        grid.setItems(members);
    }
    
    private void showAddMemberDialog() {
        Dialog dialog = new Dialog();
        FormLayout form = new FormLayout();
        
        TextField nameField = new TextField("Name");
        TextField surnameField = new TextField("Surname");
        TextField emailField = new TextField("Email");
        DatePicker birthdayPicker = new DatePicker("Birthday");
        
        Button saveBtn = new Button("Save", e -> {
            Member member = new Member();
            member.setName(nameField.getValue());
            member.setSurname(surnameField.getValue());
            member.setEmail(emailField.getValue());
            member.setBirthday(birthdayPicker.getValue());
            
            memberService.createMember(member);
            refreshGrid();
            dialog.close();
            Notification.show("Member added successfully!");
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
                    Notification.show("Please select two different players");
                    return;
                }
                
                int player1Score = Integer.parseInt(player1ScoreField.getValue());
                int player2Score = Integer.parseInt(player2ScoreField.getValue());
                
//                rankingService.processMatch(player1.getId(), player2.getId(), player1Score, player2Score);
                
                refreshGrid();
                dialog.close();
                Notification.show("Match recorded successfully!");
            } catch (NumberFormatException ex) {
                Notification.show("Please enter valid scores");
            } catch (Exception ex) {
                Notification.show("Error recording match: " + ex.getMessage());
            }
        });
        
        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        
        form.add(player1Combo, player2Combo, player1ScoreField, player2ScoreField);
        dialog.add(form, new HorizontalLayout(saveBtn, cancelBtn));
        dialog.open();
    }
}