package gui;

import buzzword.BuzzWord;
import components.AppWorkspaceComponent;
import controller.BuzzController;
import data.GameData;
import data.GameFile;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import propertymanager.PropertyManager;
import ui.AppMessageDialogSingleton;
import wordapp.WordApp;
import ui.AppGUI;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static buzzword.BuzzWordProperties.*;
import static settings.AppPropertyType.*;

/**
 * @author Po Yiu Ho
 */
public class Workspace extends AppWorkspaceComponent {
    WordApp     app;
    AppGUI      gui;
    Stage       primaryStage;
    PropertyManager propertyManager;

    String      login;
    String      password;
    String      prevScreen;
    Label       guiHeadingLabel;
    Label       doBetter;
    TextField   user;
    PasswordField pass;
    Button      userButton;
    Button      homeButton;
    VBox        selectionBar;
    VBox        gameBar;
    VBox        homeBar;
    VBox        profileBox;
    String      screen;

    public Workspace(WordApp initapp) throws IOException {
        app = initapp;
        gui = app.getGUI();
        primaryStage = gui.getWindow();
        propertyManager = PropertyManager.getManager();

        workspace = new BorderPane();
        layoutGUI();
        showHomeScreen();
        setupHandlers();
    }

    public Pane getWorkspace() {return this.workspace;}

    private void layoutGUI() {
        PropertyManager propertyManager = PropertyManager.getManager();
        guiHeadingLabel = new Label(propertyManager.getPropertyValue(WORKSPACE_HEADING_LABEL));

        HBox top = new HBox();
        top.getChildren().add(guiHeadingLabel);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(20, 20, 5, 20));
        gui.getAppPane().setTop(top);
        try {
            gui.showPic();
        } catch(IOException ex) {
            System.out.println("GUI exception caught1");
        }
        login = "";
        password = "";
    }

    private void setupHandlers() {
        BuzzController controller = (BuzzController) gui.getController();
        AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();

        gui.getCreateButton().setOnAction(e -> showCreationScreen());

        gui.getLoginButton().setOnAction(e -> {
            try {
                showLoginScreen();
            } catch (IOException ex) {
                PropertyManager           props  = PropertyManager.getManager();
                dialog.show(props.getPropertyValue(LOGIN_ERROR_TITLE), props.getPropertyValue(LOGIN_ERROR_MESSAGE));
                System.exit(1);
            }

        });

        gui.getHelpButton().setOnAction(e -> showHelpScreen());

        final KeyCombination exit = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN);
        final KeyCombination play = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);
        final KeyCombination log = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);

        gui.getPrimaryScene().addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (exit.match(event)) {
                    if (controller.getCanExit()) {
                        Platform.exit();
                    } else if (!controller.getCanExit() && screen.equals("Game")) {
                        boolean exit = controller.promptToExit();
                        if (exit) {
                            Platform.setImplicitExit(true);
                            Platform.exit();
                        } else {
                            event.consume();
                        }
                    } else {
                        event.consume();
                    }
                } else if (log.match(event) && controller.getLoggedIn()) {
                    logout();
                } else if (play.match(event)) {
                    controller.play();
                }
            }
        });

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (controller.getCanExit()) {
                    Platform.exit();
                } else if (!controller.getCanExit() && screen.equals("Game")) {
                    boolean exit = controller.promptToExit();
                    if (exit) {
                        Platform.setImplicitExit(true);
                        Platform.exit();
                    } else {
                        event.consume();
                    }
                } else {
                    event.consume();
                }
            }
        });


        gui.getPrimaryScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case ESCAPE:
                        if (!controller.getLoggedIn()) {
                            //System.out.println("not logged in");
                            escape();
                        } else if (screen.equals("Help")) {
                            try {
                                System.out.println("help screen here");
                                escape();
                                showModeSelectionScreen();
                            } catch(Exception ex) {
                                System.out.println("So many exceptions here");
                            }
                        }
                        event.consume();
                        break;
                    case ENTER:
                        if (!controller.getCanExit() && screen.equals("Creation")) {
                            login = user.getText();
                            password = pass.getText();

                            if (login.length() >= 5 && password.length() >= 5) {
                                doBetter.setVisible(false);
                                controller.handleCreateNewProfileRequest(login, password);
                            } else {
                                System.out.println("why");
                                doBetter.setVisible(true);
                            }

                        }

                        if (!controller.getCanExit() && screen.equals("Login")) {
                            login = user.getText();
                            password = pass.getText();

                            boolean right = controller.handleLoginRequest(login, password);

                            if (right) {
                                doBetter.setVisible(false);
                                showModeSelectionScreen();
                            } else {
                                doBetter.setVisible(true);
                            }
                        }

                        if (controller.getCanExit() && screen.equals("Profile")) {
                            GameData data = (GameData) app.getDataComponent();
                            data.setPassword(pass.getText());
                        }
                }
            }
        });
    }

    private void showHomeScreen() {
        screen = "Home";
        // in charge of loading all dictionaries
        GameData data = (GameData) app.getDataComponent();
        GameFile file = (GameFile) app.getFileComponent();
        file.loadAllWords(data);
    }

    private void showHelpScreen() {
        prevScreen = screen;
        VBox helpScreen = new VBox(5);
        Text help = new Text("How good are you at identifying words, or perhaps even guessing words? In BuzzWord, the player" +
                " will be given a graph of letters, and s/he has to identify words that can be formed by connecting the" +
                " nodes in this graph. The longer the word, the higher its score! But of course, we don’t want people to" +
                " get points for randomly typing things, so the game may ask the player to identify the correct meaning" +
                " of the word. Failure to do so may carry a harsh punishment! As players get promoted to higher levels," +
                " the graphs get more complex, and the players are forced to guess longer words.");

        help.setStyle("-fx-font-size: 20px; " +
                "-fx-stroke: white;" +
                "-fx-fill: white;");
        Text rules = new Text("You will be given a certain amount of time to find as many words as you can. In BuzzWord, " +
                "the target amount of points is 75, and so the player would need to get that amount or more to win. Only words " +
                "that are 3 letters or longer will count. After level 4, only words that are 4 letters or longer will count. " + "");
                //"Moreover, the amount of time that the player has will decrease slowly with each level. Good Luck!");
        rules.setStyle("-fx-font-size: 20px; " +
                "-fx-stroke: white;" +
                "-fx-fill: white;");
        helpScreen.setStyle("-fx-background-color: #002B72;" +
                "-fx-opacity: 0.5;");
        helpScreen.getChildren().setAll(help, rules);
        helpScreen.setPadding(new Insets(10, 10, 10, 10));
        helpScreen.prefWidthProperty().bind(workspace.widthProperty());
        helpScreen.prefHeightProperty().bind(workspace.heightProperty());
        help.setWrappingWidth(helpScreen.prefWidthProperty().doubleValue() - 40);
        rules.setWrappingWidth(helpScreen.prefWidthProperty().doubleValue() - 40);
        ScrollPane helpScrollPane = new ScrollPane(helpScreen);
        helpScreen.setMargin(rules, new Insets(10, 10, 10, 10));
        helpScreen.setMargin(help, new Insets(10, 10, 10, 10));
        helpScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        ((BorderPane) workspace).setCenter(helpScrollPane);
        gui.getAppPane().setCenter(workspace);
        gui.getAppPane().setMargin(workspace, new Insets(50, 50, 50, 50));
        gui.getAppPane().getCenter().setId(propertyManager.getPropertyValue(LEFT_TOOLBAR_ID));
        screen = "Help";
    }

    private void showLoginScreen() throws IOException {
        screen = "Login";
        BuzzController controller = (BuzzController) gui.getController();
        AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();

        VBox logandpass = new VBox(5);
        logandpass.setPadding(new Insets(20, 20, 20, 20));

        Label newUser = new Label("USERNAME: ");
        user = new TextField();
        user.setAlignment(Pos.CENTER);
        user.setPromptText("Enter your username");
        HBox userBox = new HBox();
        userBox.getChildren().setAll(newUser, user);
        userBox.setAlignment(Pos.CENTER);
        newUser.getStyleClass().setAll("labels");
        newUser.prefWidthProperty().bind(logandpass.widthProperty().multiply(0.5));

        Label newPass = new Label("PASSWORD: ");
        pass = new PasswordField();
        pass.setAlignment(Pos.CENTER);
        pass.setPromptText("Enter your password");
        HBox passBox = new HBox();
        passBox.getChildren().setAll(newPass, pass);
        passBox.setAlignment(Pos.CENTER);
        newPass.getStyleClass().setAll("labels");
        newPass.prefWidthProperty().bind(logandpass.widthProperty().multiply(0.5));

        Button logIn = new Button("LOG IN");
        logIn.setStyle("-fx-text-fill: blue;" +
                "-fx-opacity: 0.5;" +
                "-fx-font-size: 16px;" +
                "-fx-background-radius: 10px;" +
                "-fx-border:none;");
        doBetter = new Label("Your username and password do not match.");
        doBetter.setVisible(false);
        doBetter.setStyle("-fx-text-fill:white;");

        logandpass.getChildren().setAll(userBox, passBox, logIn, doBetter);
        logandpass.setAlignment(Pos.CENTER);
        logandpass.setSpacing(50);

        ((BorderPane) workspace).setCenter(logandpass);
        gui.getAppPane().setCenter(workspace);
        gui.getAppPane().setMargin(workspace, new Insets(70, 100, 100, 100));
        gui.getAppPane().getCenter().setId(propertyManager.getPropertyValue(LEFT_TOOLBAR_ID));

        Platform.setImplicitExit(false);
        controller.setCanExit(false);
        gui.enableButtons(false);

        logIn.setOnAction(e -> {
            login = user.getText();
            password = pass.getText();

            boolean right = controller.handleLoginRequest(login, password);

            if (right) {
                doBetter.setVisible(false);
                showModeSelectionScreen();
            } else {
                doBetter.setVisible(true);
            }
        });

    }

    public Button getUserButton() {return this.userButton;}

    private void showCreationScreen() {
        screen = "Creation";
        BuzzController controller = (BuzzController) gui.getController();
        AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();

        VBox logandpass = new VBox(5);
        logandpass.setPadding(new Insets(20, 20, 20, 20));

        Label newUser = new Label("Create a username: ");
        user = new TextField();
        user.setAlignment(Pos.CENTER);
        user.setPromptText("Enter a username");
        HBox userBox = new HBox();
        userBox.getChildren().setAll(newUser, user);
        userBox.setAlignment(Pos.CENTER);
        newUser.getStyleClass().setAll("labels");
        newUser.prefWidthProperty().bind(logandpass.widthProperty().multiply(0.6));

        Label newPass = new Label("Create a new password: ");
        pass = new PasswordField();
        pass.setAlignment(Pos.CENTER);
        pass.setPromptText("Enter a password");
        HBox passBox = new HBox();
        passBox.getChildren().setAll(newPass, pass);
        passBox.setAlignment(Pos.CENTER);
        newPass.getStyleClass().setAll("labels");
        newPass.prefWidthProperty().bind(logandpass.widthProperty().multiply(0.6));

        Button submit = new Button("Submit");
        submit.setStyle("-fx-text-fill: blue;" +
                "-fx-opacity: 0.75;" +
                "-fx-font-size: 16px;" +
                "-fx-background-radius: 10px;" +
                "-fx-border:none;");

        doBetter = new Label("Your username/password has to be equal to or longer than 5 letters.");
        doBetter.setVisible(false);
        doBetter.setStyle("-fx-text-fill:white;");

        logandpass.getChildren().setAll(userBox, passBox, submit, doBetter);
        logandpass.setAlignment(Pos.CENTER);
        logandpass.setSpacing(50);

        ((BorderPane) workspace).setCenter(logandpass);
        gui.getAppPane().setCenter(workspace);
        gui.getAppPane().setMargin(workspace, new Insets(100, 100, 100, 100));
        gui.getAppPane().getCenter().setId(propertyManager.getPropertyValue(LEFT_TOOLBAR_ID));

        Platform.setImplicitExit(false);
        controller.setCanExit(false);
        gui.enableButtons(false);

        submit.setOnAction(e -> {
            login = user.getText();
            password = pass.getText();

            if (login.length() >= 5 && password.length() >= 5) {
                doBetter.setVisible(false);
                controller.handleCreateNewProfileRequest(login, password);
            } else {
                System.out.println("why");
                doBetter.setVisible(true);
            }
        });

    }

    public void escape() {
        BuzzController controller = (BuzzController) gui.getController();

        Platform.setImplicitExit(true);
        controller.setCanExit(true);

        gui.enableButtons(true);
        gui.getAppPane().getChildren().remove(workspace);
        gui.getAppPane().setLeft(gui.getToolbarPane());
        try {
            gui.showPic();
        } catch(IOException ex) {
            System.out.println("GUI exception caught1");
        }
        screen = "Home";
    }

    private void showModeSelectionScreen() {
        escape();
        screen = "ModeSelection";
        BuzzController controller = (BuzzController) gui.getController();
        AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
        controller.setLoggedIn(true);

        selectionBar = new VBox(20);
        userButton = new Button(((GameData)app.getDataComponent()).getUsername().toUpperCase());
        userButton.setId("userButton");

        Label selection = new Label("Select a game mode");
        selection.setStyle("-fx-text-fill: white;" +
                "");
        ChoiceBox modes = new ChoiceBox(FXCollections.observableArrayList(
                "English Dictionary", "Animals", "First Names"));

        modes.setValue(controller.getCurrentMode());
        Button startButton = new Button("Start Playing");
        startButton.setId("startButton");

        Button logOut = new Button("Log out");
        logOut.setId("logOutButton");
        Button help = new Button("Help");

        try {
            help = gui.initializeChildButton(selectionBar, HELP_ICON.toString(), HELP_TOOLTIP.toString(), HELP_WORDS_ICON.toString(), false);

        } catch(IOException ex) {
            System.out.println ("exception");
        }

        help.setAlignment(Pos.CENTER);
        help.setId("help");
        selectionBar.getChildren().setAll(userButton, selection, modes, startButton, logOut, help);
        selectionBar.setMargin(help, new Insets(0, 20, 0, 20));
        selectionBar.setMargin(selection, new Insets(20, 0, 0, 0));
        selectionBar.setMargin(userButton, new Insets(0, 0, 20, 0));
        selectionBar.setMargin(startButton, new Insets(10, 0, 0, 0));
        selectionBar.setAlignment(Pos.CENTER);
        selectionBar.prefWidthProperty().bind(gui.getToolbarPane().widthProperty());

        gui.getAppPane().setLeft(selectionBar);
        gui.getAppPane().setMargin(selectionBar, new Insets(10, 10, 10, 10));
        gui.getAppPane().getLeft().setId(propertyManager.getPropertyValue(LEFT_TOOLBAR_ID));

        try {
            gui.showPic();
        } catch(IOException ex) {
            System.out.println("GUI exception caught2");
        }

        startButton.setOnMouseClicked(e -> {
            controller.setCurrentMode((String) modes.getValue());
            showLevelSelectionScreen((String) modes.getValue());

        });
        userButton.setOnAction(e -> showProfileScreen());
        logOut.setOnAction(e -> logout());
        help.setOnAction(e -> showHelpScreen());
    }

    private void logout() {
        escape();
        BuzzController controller = (BuzzController) gui.getController();
        controller.setLoggedIn(false);
    }

    public void showProfileScreen() {
        screen = "Profile";

        showLeftGamebar();
        GameData data = (GameData) app.getDataComponent();
        profileBox = new VBox();

        Label username = new Label(((GameData)app.getDataComponent()).getUsername().toUpperCase());

        username.getStyleClass().setAll("labels");
        username.setAlignment(Pos.CENTER);
        username.setPadding(new Insets(50, 0, 0, 0));

        HBox threeModesBox = new HBox();
        Label personalBest = new Label("Personal Bests");
        personalBest.getStyleClass().setAll("small-labels");
        personalBest.setAlignment(Pos.CENTER);
        personalBest.setPadding(new Insets(50, 0, 20, 0));

        VBox dicBox = new VBox(20);
        VBox aniBox = new VBox(20);
        VBox nameBox = new VBox(20);
        threeModesBox.getChildren().setAll(dicBox, aniBox, nameBox);
        dicBox.prefWidthProperty().bind(threeModesBox.widthProperty().divide(3));
        aniBox.prefWidthProperty().bind(threeModesBox.widthProperty().divide(3));
        nameBox.prefWidthProperty().bind(threeModesBox.widthProperty().divide(3));
        dicBox.setAlignment(Pos.CENTER);
        aniBox.setAlignment(Pos.CENTER);
        nameBox.setAlignment(Pos.CENTER);

        Button changePass = new Button("Change your password");
        changePass.setId("logOutButton");
        changePass.setPadding(new Insets(20, 20, 20, 20));

        Label dictionary = new Label("English Dictionary");
        Label dicScore = new Label("Level " + Integer.toString(((GameData)app.getDataComponent()).getLevel("English Dictionary")));
        if (data.getLevel("English Dictionary") == 0) {
            dicScore.setText("Never played before");
        }
        Label animals = new Label("Animals");
        Label aniScore = new Label("Level " + Integer.toString(((GameData)app.getDataComponent()).getLevel("Animals")));
        if (data.getLevel("Animals") == 0) {
            aniScore.setText("Never played before");
        }
        Label names = new Label("First Names");
        Label nameScore = new Label("Level " + Integer.toString(((GameData)app.getDataComponent()).getLevel("First Names")));
        if (data.getLevel("First Names") == 0) {
            nameScore.setText("Never played before");
        }

        try {
            dictionary.setGraphic(app.getGUI().loadImage(DICTIONARY_ICON.toString()));
            animals.setGraphic(app.getGUI().loadImage(ANIMAL_ICON.toString()));
            names.setGraphic(app.getGUI().loadImage(NAME_ICON.toString()));
        } catch(IOException ex) {
            System.out.println("error with profile screen");
        }

        dicBox.getChildren().setAll(dictionary, dicScore);
        aniBox.getChildren().setAll(animals, aniScore);
        nameBox.getChildren().setAll(names, nameScore);
        for (Node n: threeModesBox.getChildren()) {
            for (Node l: ((VBox) n).getChildren()) {
                l.getStyleClass().setAll("small-labels");
            }
        }


        profileBox.getChildren().setAll(username, personalBest, threeModesBox, changePass);
        profileBox.setAlignment(Pos.TOP_CENTER);
        profileBox.setMargin(changePass, new Insets( 10, 10, 10, 10));
        profileBox.setId("toolbar");
        app.getGUI().getAppPane().setCenter(profileBox);
        app.getGUI().getAppPane().setMargin(profileBox, new Insets(10, 10, 10, 10));
        changePass.setOnAction(e -> changePass(username, personalBest, threeModesBox, changePass));
    }

    private void changePass(Label user, Label personal, HBox modes, Button other) {
        Label newPass = new Label("New Password: ");
        HBox box = new HBox(15);
        Button change = new Button("Change");
        change.setPadding(new Insets(20, 20, 20, 20));
        change.setId("logOutButton");
        pass.clear();
        newPass.getStyleClass().add("small-labels");
        box.setAlignment(Pos.CENTER);
        box.getChildren().setAll(newPass, pass, change);
        profileBox.getChildren().setAll(user, personal, modes, box);
        change.setOnAction(e -> {
            BuzzController controller = (BuzzController) gui.getController();
            controller.handleSaveProgressRequest();
            profileBox.getChildren().setAll(user, personal, modes, other);
        });
    }

    public void showLevelSelectionScreen(String s) {
        showHomeBar();

        screen = "LevelSelection";
        BuzzController controller = (BuzzController) gui.getController();
        controller.setCurrentMode(s);
        VBox modeScreen = new VBox(20);
        //modeScreen.setAlignment(Pos.CENTER);
        Label mode = new Label(s + " Levels");
        mode.setAlignment(Pos.CENTER);
        mode.setStyle("-fx-text-fill: black;" +
                "-fx-font-size: 35px;" +
                "-fx-opacity: 0.5");
        mode.setPadding(new Insets(40, 10, 10, 20));
        VBox levels = new VBox(10);
        HBox row1 = new HBox(25);

        ArrayList<Button> levelList = new ArrayList<>();
        // level nodes here
        for (int i = 1; i <= 4; i++) {
            Button level = new Button(Integer.toString(i));
            levelList.add(level);
            row1.getChildren().add(level);
        }

        row1.setPadding(new Insets(20, 20, 20, 20));
        row1.setAlignment(Pos.CENTER);

        HBox row2 = new HBox(25);
        row2.setPadding(new Insets(20, 20, 20, 20));
        row2.setAlignment(Pos.CENTER);
        // rest of level nodes here
        for (int i = 5; i <= 8; i++) {
            Button level = new Button(Integer.toString(i));
            levelList.add(level);
            row2.getChildren().add(level);
        }

        for (int i = 1; i <= levelList.size(); i++) {
            levelList.get(i - 1).getStyleClass().setAll("levelButton");
            levelList.get(i - 1).setAlignment(Pos.CENTER);
            levelList.get(i - 1).setPadding(new Insets(25, 35, 25, 35));

            if (i <= controller.getMaxLevel(s) + 1) {
                levelList.get(i - 1).setDisable(false);
            } else {
                levelList.get(i - 1).setDisable(true);
            }
        }

        for (Button b: levelList) {
            b.setOnAction(e -> {
                controller.setCurrentLevel(Integer.valueOf(b.getText()));
                showGameScreen();
            });
        }
// userButton, selection, modes, startButton, logOut, help
        /*if (selectionBar.getChildren().size() == 6) {
            selectionBar.getChildren().remove(5);
        }*/
        levels.getChildren().setAll(row1, row2);
        modeScreen.getChildren().setAll(mode, levels);
        gui.getAppPane().setCenter(modeScreen);
    }

    private void showGameScreen() {
        BuzzController controller = (BuzzController) gui.getController();
        prevScreen = screen;
        screen = "Game";
        controller.start();
    }

    private void showHomeBar() {
        BuzzController controller = (BuzzController) gui.getController();

        homeBar = new VBox(20);
        userButton = new Button(((GameData)app.getDataComponent()).getUsername().toUpperCase());
        userButton.setId("userButton");

        Label selection = new Label("Select a game mode");
        selection.setStyle("-fx-text-fill: white;" +
                "");
        ChoiceBox modes = new ChoiceBox(FXCollections.observableArrayList(
                "English Dictionary", "Animals", "First Names"));
        modes.setValue(controller.getCurrentMode());

        Button startButton = new Button("Start Playing");
        startButton.setId("startButton");

        Button home = new Button("Home");
        home.setId("startButton");
        Button help = new Button("Help");

        try {
            help = gui.initializeChildButton(homeBar, HELP_ICON.toString(), HELP_TOOLTIP.toString(), HELP_WORDS_ICON.toString(), false);

        } catch(IOException ex) {
            System.out.println ("exception");
        }

        help.setAlignment(Pos.CENTER);
        help.setId("help");
        homeBar.getChildren().setAll(userButton, selection, modes, startButton, home, help);
        homeBar.setMargin(help, new Insets(0, 20, 0, 20));
        homeBar.setMargin(selection, new Insets(20, 0, 0, 0));
        homeBar.setMargin(userButton, new Insets(0, 0, 20, 0));
        homeBar.setMargin(startButton, new Insets(10, 0, 0, 0));
        homeBar.setAlignment(Pos.CENTER);
        homeBar.prefWidthProperty().bind(gui.getToolbarPane().widthProperty());

        gui.getAppPane().setLeft(homeBar);
        gui.getAppPane().setMargin(homeBar, new Insets(10, 10, 10, 10));
        gui.getAppPane().getLeft().setId(propertyManager.getPropertyValue(LEFT_TOOLBAR_ID));

        try {
            gui.showPic();
        } catch(IOException ex) {
            System.out.println("GUI exception caught2");
        }

        startButton.setOnMouseClicked(e -> {
            controller.setCurrentMode((String) modes.getValue());
            showLevelSelectionScreen((String) modes.getValue());

        });
        userButton.setOnAction(e -> showProfileScreen());
        home.setOnAction(e -> showModeSelectionScreen());
        help.setOnAction(e -> showHelpScreen());
    }

    public void showLeftGamebar() {
        gameBar = new VBox(20);
        userButton = new Button(((GameData)app.getDataComponent()).getUsername().toUpperCase());
        userButton.setId("userButton");

        homeButton = new Button("HOME");
        homeButton.setId("userButton");
        homeButton.setAlignment(Pos.CENTER);

        gameBar.getChildren().setAll(userButton, homeButton);
        gameBar.setAlignment(Pos.CENTER);
        gameBar.prefWidthProperty().bind(gui.getToolbarPane().widthProperty());
        gameBar.setMargin(homeButton, new Insets(130, 0, 0 ,0));

        gui.getAppPane().setLeft(gameBar);
        gui.getAppPane().setMargin(gameBar, new Insets(10, 10, 10, 10));
        gui.getAppPane().getLeft().setId(propertyManager.getPropertyValue(LEFT_TOOLBAR_ID));

        userButton.setOnAction(e -> showProfileScreen());
        homeButton.setOnAction(e -> showModeSelectionScreen());
    }

    @Override
    public void reloadWorkspace() {

    }

    @Override
    public void initStyle() {
        gui.getAppPane().setId(propertyManager.getPropertyValue(ROOT_BORDERPANE_ID)); // background
        gui.getToolbarPane().getStyleClass().setAll(propertyManager.getPropertyValue(SEGMENTED_BUTTON_BAR)); // buttonbar
        gui.getToolbarPane().setId(propertyManager.getPropertyValue(LEFT_TOOLBAR_ID)); // toolbar

        ObservableList<Node> toolbarChildren = gui.getToolbarPane().getChildren();

        //toolbarChildren.get(0).getStyleClass().add(propertyManager.getPropertyValue(FIRST_TOOLBAR_BUTTON));
        toolbarChildren.get(toolbarChildren.size() - 1).setId(propertyManager.getPropertyValue(LAST_TOOLBAR_BUTTON));

        workspace.getStyleClass().add(CLASS_BORDERED_PANE);
        guiHeadingLabel.getStyleClass().setAll(propertyManager.getPropertyValue(HEADING_LABEL));
    }
}
