package controller;

import data.GameData;
import data.GameFile;
import gui.LetterNode;
import gui.Workspace;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import propertymanager.PropertyManager;
import ui.AppMessageDialogSingleton;
import ui.YesNoCancelDialogSingleton;
import wordapp.WordApp;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static settings.AppPropertyType.*;
import static settings.InitializationParameters.APP_IMAGEDIR_PATH;
import static settings.InitializationParameters.APP_WORKDIR_PATH;

/**
 * @author Po Yiu Ho
 */
public class BuzzController implements FileController {
    private WordApp             app;
    private GameData            data;
    private Stage               primaryStage;
    private GameFile            file;
    private Workspace           workspace;

    private boolean             gamewon;
    private boolean             canExit;
    private boolean             loggedIn;
    private boolean             startSelect;
    private boolean             canSelect;
    private ArrayList<LetterNode> nodes;
    private int                 currentLevel;
    private int                 totalPoints;
    private int                 targetPoints;
    private int                 timeLeft;
    private String              currentMode;
    private String              completedGuess;
    private Label               levelLabel;
    private Label               modeLabel;
    private Label               timeRemaining;
    private Label               targetLabel;
    private Label               points;
    private Button              startTimerButton;
    private Button              pauseButton;
    private Button              replayButton;
    private Button              saveButton;
    private VBox                mainGameScreen;
    private VBox                rows;
    private HBox                time;
    private VBox                rightScreen;
    private HBox                buttonBox;
    private StackPane           pauseScreen;
    private StackPane           grid;
    private Pane                lines;
    private BorderPane          ws;
    private Timeline            timeline;
    private ArrayList<String>   wordsFound;
    private ArrayList<Text>     guess;
    private TreeSet<String>     modeWords;
    private ArrayList<LetterNode> guessedNodes;

    private VBox                wordsGuessed;
    private HBox                lettersGuessed;

    public BuzzController(WordApp wordapp) {
        this.app = wordapp;
        this.data = (GameData) wordapp.getDataComponent();
        this.file = (GameFile) wordapp.getFileComponent();
        this.workspace = (Workspace) wordapp.getWorkspaceComponent();
        this.canExit = true;
        this.loggedIn = false;
        this.currentLevel = 0;
        this.currentMode = "English Dictionary";
    }

    public void start() {
        this.workspace = (Workspace) app.getWorkspaceComponent();
        this.primaryStage = app.getGUI().getWindow();
        guess = new ArrayList<>();
        ws = new BorderPane();
        totalPoints = 0;
        gamewon = false;
        targetPoints = getTargetPoints();
        timeLeft = getInitialTime();
        startSelect = false;

        //center and top
        initNodeGraphics();

        //bottom
        buttonBox = new HBox();

        try {
            startTimerButton = app.getGUI().initializeChildButton(buttonBox, PLAY_ICON.toString(), PLAY_TOOLTIP.toString(), "", false);
            pauseButton = app.getGUI().initializeChildButton(buttonBox, PAUSE_ICON.toString(), PAUSE_TOOLTIP.toString(), "", false);
            replayButton = app.getGUI().initializeChildButton(buttonBox, REPLAY_ICON.toString(), REPLAY_TOOLTIP.toString(), "", false);
            saveButton = app.getGUI().initializeChildButton(buttonBox, SAVE_ICON.toString(), SAVE_TOOLTIP.toString(), "", false);
            pauseButton.setVisible(false);
            saveButton.setVisible(false);
            replayButton.setVisible(false);

            startTimerButton.setId("startTimer");
            pauseButton.setId("startTimer");
            replayButton.setId("startTimer");
            saveButton.setId("startTimer");

            pauseButton.setAlignment(Pos.CENTER);
            saveButton.setAlignment(Pos.CENTER);
            replayButton.setAlignment(Pos.CENTER);
            startTimerButton.setAlignment(Pos.CENTER);

            pauseButton.setMaxSize(50, 50);
            startTimerButton.setMaxSize(50, 50);
            replayButton.setMaxSize(50, 50);
            saveButton.setMaxSize(50, 50);

        } catch (IOException ex) {
            //do nothing
            System.out.println("controller error");
        }


        buttonBox.prefWidthProperty().bind(ws.widthProperty());
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(0, 0, 20, 0));
        ws.setBottom(buttonBox);
        startTimerButton.setOnAction(e -> play());
        saveButton.setOnAction(e -> {
            AppMessageDialogSingleton messageDialog   = AppMessageDialogSingleton.getSingleton();
            PropertyManager propertyManager = PropertyManager.getManager();
            messageDialog.show(propertyManager.getPropertyValue(SAVE_COMPLETED_TITLE), propertyManager.getPropertyValue(SAVE_COMPLETED_MESSAGE));
            handleSaveProgressRequest();
        });
        replayButton.setOnAction(e -> replay());
        pauseButton.setOnAction(e -> pause());

        //right
        initTrackGraphics();
    }

    public void play() {
        //System.out.println("play");
        this.workspace.getUserButton().setDisable(true);
        this.canExit = false;
        this.canSelect = true;
        this.completedGuess = "";
        Platform.setImplicitExit(false);
        this.rightScreen.setVisible(true);
        this.startTimerButton.setVisible(false);
        this.pauseButton.setVisible(true);
        this.time.setVisible(true);
        this.startSelect = true;
        data.resetWords();

        for (Node n : this.rows.getChildren()) {
            for (Node l : ((HBox) n).getChildren()) {
                ((LetterNode) l).setNodeId("letterNode");
            }
        }

        timeline = new Timeline();
        IntegerProperty timeSeconds =
                new SimpleIntegerProperty(timeLeft);
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(timeLeft + 1),
                        e -> end(),
                        new KeyValue(timeSeconds, 0)));
        timeline.playFromStart();
        timeRemaining.textProperty().bind(timeSeconds.asString());

        this.rows.addEventFilter(MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                app.getGUI().getPrimaryScene().startFullDrag();
            }
        });

        this.rows.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                for (LetterNode n : nodes) {
                    n.reset();
                }

                if (validateWord(completedGuess)) {
                    data.addWord(currentMode, completedGuess);
                    updateWords();
                }

                lines.getChildren().clear();
                startSelect = true;
                guess.clear();
                updateTrack();
                completedGuess = "";
                checkPoints();

            }
        });

        for (LetterNode n : nodes) {
            n.setOnMouseDragEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (canSelect) {
                        if (!n.getConnected()) {
                            if (startSelect) {
                                guess.add(new Text(Character.toString(n.getChar())));

                                completedGuess += n.getChar();
                                System.out.println(completedGuess);
                                n.highlight();
                                startSelect = false;
                            }

                            if (surroundUsed(n)) {
                                guess.add(new Text(Character.toString(n.getChar())));

                                completedGuess += n.getChar();
                                System.out.println(completedGuess);
                                n.highlight();
                            }
                        }

                        updateTrack();
                    }
                }
            });
        }

        this.guessedNodes = new ArrayList<>();
        this.grid.requestFocus();
        this.grid.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    for (LetterNode n : nodes) {
                        n.reset();
                    }

                    if (validateWord(completedGuess)) {
                        data.addWord(currentMode, completedGuess);
                        updateWords();
                    }

                    lines.getChildren().clear();
                    startSelect = true;
                    guess.clear();
                    updateTrack();
                    completedGuess = "";
                    checkPoints();
                } else {
                    boolean added = false;
                    for (LetterNode n : nodes) {
                        if (Character.toString(n.getChar()).equals(event.getText().toUpperCase())) {
                            guessedNodes.add(n);
                            if (!added) {
                                if (!guess.isEmpty()) {
                                    for (LetterNode no: nodes) {
                                        if (surroundUsed(n)) {
                                            guess.add(new Text(Character.toString(n.getChar())));
                                            completedGuess += n.getChar();
                                            added = true;
                                        } else {
                                            no.reset();
                                        }
                                    }
                                } else {
                                    guess.add(new Text(Character.toString(n.getChar())));
                                    completedGuess += n.getChar();
                                    added = true;
                                }
                            }
                            n.highlight();

                        }
                    }

                }

                updateTrack();
            }
        });
    }

    private boolean validateWord(String s) {
        int minChars = (this.currentLevel > 4)? 4: 3;
        if (s.length() >= minChars) {
            if (data.getPossible().contains(s)) {
                if (data.getList(currentMode).contains(s)) {
                    return false;
                }

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    private boolean surroundUsed(LetterNode n) {
        int currentx = n.getCol();
        int currenty = n.getRow();
        ArrayList<Integer> posRows = new ArrayList<>();
        ArrayList<Integer> posCols = new ArrayList<>();
        posRows.add(currenty);
        posCols.add(currentx);
        if (currenty == 1) {
            posRows.add(currenty + 1);
        } else if (currenty == 5) {
            posRows.add(currenty - 1);
        } else {
            posRows.add(currenty - 1);
            posRows.add(currenty + 1);
        }

        if (currentx == 1) {
            posCols.add(currentx + 1);
        } else if (currentx == 5) {
            posCols.add(currentx - 1);
        } else {
            posCols.add(currentx - 1);
            posCols.add(currentx + 1);
        }

        for (int r: posRows) {
            for (int c: posCols) {
                LetterNode found = (LetterNode) ((HBox) rows.getChildren().get(r - 1)).getChildren().get(c - 1);
                if (!found.equals(n)) {
                    if (found.getUsed()) {
                        if (!found.getConnected()) {
                            Line line = found.connectTo(n);
                            lines.getChildren().add(line);
                            return true;
                        }

                    }
                }

            }
        }

        return false;
    }

    private void end() {
        PropertyManager           props  = PropertyManager.getManager();
        AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();

        //System.out.println("Ended");
        replayButton.setVisible(true);
        saveButton.setVisible(true);
        pauseButton.setVisible(false);
        this.rightScreen.setVisible(false);
        this.time.setVisible(false);
        this.canExit = true;
        this.canSelect = false;
        Platform.setImplicitExit(true);

        if (timeLeft == 0) {
            gamewon = false;
        } else {
            gamewon = true;
        }

        if (gamewon) {
            StackPane winScreen = new StackPane();
            Label win = new Label("Y O U  W I N !");
            win.setStyle("-fx-font-size: 30px;" +
                    "-fx-text-fill: white;");
            win.setAlignment(Pos.CENTER);
            winScreen.setAlignment(Pos.CENTER);
            //winScreen.prefHeightProperty().bind(grid.heightProperty());
            winScreen.getChildren().setAll(grid, win);

            grid.toBack();
            this.mainGameScreen.getChildren().setAll(winScreen, levelLabel);

            this.startTimerButton.setVisible(true);
            this.startTimerButton.setOnAction(e -> this.nextLevel());
            //setting personal best
            if (data.getLevel(this.currentMode) + 1 == this.currentLevel) {
                data.increaseLevel(this.currentMode);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        dialog.show(props.getPropertyValue(PERSONAL_BEST_TITLE), props.getPropertyValue(PERSONAL_BEST_MESSAGE));
                    }
                });
            }

            handleSaveProgressRequest();
        }
    }

    private void nextLevel() {
        this.time.setVisible(false);
        this.canExit = true;
        Platform.setImplicitExit(true);
        this.startTimerButton.setOnAction(e -> play());
        gamewon = false;
        this.totalPoints = 0;
        this.currentLevel++;
        targetPoints = getTargetPoints();
        data.resetWords();
        updateWords();
        updateTrack();

        //center and top
        initNodeGraphics();

        for (Node row: this.rows.getChildren()) {
            for (Node n : ((HBox) row).getChildren()) {
                ((LetterNode) n).setNodeId("pauseNode");
            }
        }

    }

    private void cont() {
        //System.out.println("continuing");
        this.startTimerButton.setVisible(false);
        this.pauseButton.setVisible(true);
        this.replayButton.setVisible(false);
        this.mainGameScreen.getChildren().setAll(grid, this.levelLabel);
        this.canSelect = true;
        this.lines.setVisible(true);

        for (Node row: this.rows.getChildren()) {
            for (Node n: ((HBox) row).getChildren()) {
                if (((LetterNode) n).getUsed()) {
                    ((LetterNode) n).setNodeId("dragNode");
                } else {
                    ((LetterNode) n).setNodeId("letterNode");
                }

            }
        }
        this.timeline.play();
    }

    private void pause() {
        //System.out.println("Pause");
        this.pauseButton.setVisible(false);
        this.startTimerButton.setVisible(true);
        this.replayButton.setVisible(true);
        this.startTimerButton.setOnAction(e -> cont());
        //make sure timer is stopped
        this.timeline.pause();
        this.canSelect = false;
        this.lines.setVisible(false);

        pauseScreen = new StackPane();
        Label pause = new Label("G A M E  P A U S E D");
        pause.setStyle("-fx-font-size: 30px;" +
                "-fx-text-fill: white;");
        pauseScreen.setAlignment(Pos.CENTER);
        pauseScreen.prefHeightProperty().bind(grid.heightProperty());
        pauseScreen.getChildren().setAll(grid, pause);
        for (Node row: this.rows.getChildren()) {
            for (Node n: ((HBox) row).getChildren()) {
                ((LetterNode) n).setNodeId("pauseNode");
            }
        }

        grid.toBack();
        this.mainGameScreen.getChildren().setAll(pauseScreen, levelLabel);
    }


    private void replay() {
        startTimerButton.setVisible(true);
        saveButton.setVisible(false);
        replayButton.setVisible(false);
        pauseButton.setVisible(false);
        this.time.setVisible(false);
        this.canExit = true;
        Platform.setImplicitExit(true);
        this.startTimerButton.setOnAction(e -> play());
        gamewon = false;
        this.totalPoints = 0;
        targetPoints = getTargetPoints();

        //center and top
        initNodeGraphics();

        for (Node row: this.rows.getChildren()) {
            for (Node n : ((HBox) row).getChildren()) {
                ((LetterNode) n).setNodeId("pauseNode");
            }
        }

        initTrackGraphics();
    }

    // show and keeps track of guess
    private void updateTrack() {
        lettersGuessed.getChildren().clear();
        for (Text t: guess) {
            lettersGuessed.getChildren().add(t);
            t.setStyle("-fx-text-fill: white;" +
                    "-fx-padding: 20px;" +
                    "-fx-font-weight: 900;" +
                    "-fx-fill: white;" +
                    "-fx-font-size: 20px;");
        }
    }

    private void updateWords() {
        wordsGuessed.getChildren().clear();
        for (String s: data.getList(currentMode)) { // get current words list
            Label l = new Label(s);
            l.setAlignment(Pos.CENTER);
            l.setStyle("-fx-font-weight: 700;" +
                    "-fx-font-size: 18px;");
            wordsGuessed.prefWidthProperty().bind(ws.widthProperty().multiply(0.45));
            wordsGuessed.getChildren().add(l);
        }
    }

    private void initTrackGraphics() {
        rightScreen = new VBox(10);

        time = new HBox();
        Label timeLabel = new Label("Time Remaining: ");
        timeLabel.getStyleClass().setAll("small-labels");
        timeRemaining = new Label(timeLeft + " second(s)");
        timeRemaining.setAlignment(Pos.CENTER);
        timeRemaining.getStyleClass().setAll("small-labels");
        time.getChildren().setAll(timeLabel, timeRemaining);
        time.setVisible(false);

        rightScreen.setAlignment(Pos.CENTER);

        VBox currentGuesses = new VBox(5);
        Label guesses = new Label("Current Guess");
        guesses.getStyleClass().setAll("small-labels");
        lettersGuessed = new HBox(5);

        lettersGuessed.prefHeightProperty().bind(ws.heightProperty().multiply(0.4));
        lettersGuessed.setStyle("-fx-background-color: linear-gradient(to right, #3550AB, #2C3F7F);" +
                "-fx-opacity: 0.5;");
        lettersGuessed.setAlignment(Pos.CENTER);
        currentGuesses.getChildren().setAll(guesses, lettersGuessed);
        currentGuesses.setAlignment(Pos.TOP_LEFT);

        VBox words = new VBox(10);
        wordsGuessed = new VBox(10); // where we put our words prob with HBox with word labels inside
        updateWords();

        ScrollPane wordsGuessedScrollPane = new ScrollPane(wordsGuessed);
        wordsGuessedScrollPane.setPadding(new Insets(10, 10, 10, 10));
        wordsGuessedScrollPane.setStyle("-fx-opacity: 0.2;");
        wordsGuessedScrollPane.prefHeightProperty().bind(ws.heightProperty().multiply(0.5));
        Label guessed = new Label("Words Guessed: ");
        guessed.getStyleClass().setAll("small-labels");
        HBox pointBox = new HBox();

        Label total = new Label("Total Points: ");
        total.getStyleClass().setAll("small-labels");

        points = new Label();
        this.checkPoints();

        total.prefWidthProperty().bind(ws.widthProperty().multiply(0.25));
        points.prefWidthProperty().bind(ws.widthProperty().multiply(0.25));
        total.setAlignment(Pos.CENTER_LEFT);
        points.setAlignment(Pos.CENTER);
        points.getStyleClass().setAll("small-labels");
        pointBox.getChildren().setAll(total, points);
        //points.setPadding(new Insets(0, 0, 0, 300));

        pointBox.setAlignment(Pos.TOP_LEFT);
        words.getChildren().setAll(guessed, wordsGuessedScrollPane, pointBox);
        words.setAlignment(Pos.TOP_LEFT);


        HBox target = new HBox();
        Label targetPointsLabel = new Label("Target Points: ");
        targetLabel = new Label(Integer.toString(targetPoints));
        target.getChildren().setAll(targetPointsLabel, targetLabel);
        targetPointsLabel.getStyleClass().setAll("small-labels");
        targetLabel.getStyleClass().setAll("small-labels");
        target.setPadding(new Insets(10, 0, 10, 0));

        rightScreen.getChildren().setAll(time, currentGuesses, words, target);
        rightScreen.setMargin(time, new Insets(0, 0, 0, 0));
        //rightScreen.setStyle("-fx-background-color: black;");
        rightScreen.prefHeightProperty().bind(ws.heightProperty());
        rightScreen.prefWidthProperty().bind(ws.widthProperty().multiply(0.5));

        ws.setRight(rightScreen);
        rightScreen.setVisible(false);
    }

    private void checkPoints() {
        ArrayList<String> list = data.getList(this.currentMode);
        totalPoints = 0;

        for (String s: list) {
            totalPoints += s.length();
        }

        IntegerProperty s = new SimpleIntegerProperty(totalPoints);
        points.textProperty().bind(s.asString());

        if (totalPoints >= targetPoints) {
            end();
        }
    }

    private void initNodeGraphics() {
        mainGameScreen = new VBox(15);
        modeLabel = new Label(this.currentMode);
        modeLabel.setAlignment(Pos.BOTTOM_LEFT);
        modeLabel.setStyle("-fx-text-fill: white;" +
                "-fx-font-size: 35px;" +
                "-fx-opacity: 0.5;");

        modeLabel.setPadding(new Insets(10, 10, 10, 20));
        rows = new VBox(25);
        grid = new StackPane();
        generateGrid(rows);

        for (Node n: this.rows.getChildren()) {
            for (Node l: ((HBox) n).getChildren()) {
                ((LetterNode) l).setNodeId("pauseNode");
            }
        }

        levelLabel = new Label("L E V E L  " + this.currentLevel);
        levelLabel.getStyleClass().setAll("labels");

        mainGameScreen.getChildren().setAll(grid, levelLabel);

        rows.setPadding(new Insets(20, 50, 10, 50));
        rows.setAlignment(Pos.CENTER);
        levelLabel.setAlignment(Pos.CENTER);

        levelLabel.prefWidthProperty().bind(mainGameScreen.widthProperty());
        //modeLabel.prefWidthProperty().bind(ws.widthProperty());
            //top
        ws.setTop(modeLabel);
        //center
        ws.setCenter(mainGameScreen);

        workspace.showLeftGamebar();
        app.getGUI().getAppPane().setCenter(ws);
        app.getGUI().getAppPane().setMargin(ws, new Insets(10, 10, 10, 10));
    }

    private void generateGrid(VBox rows) {
        //System.out.println("grid");

        lines = new Pane();
        lines.prefWidthProperty().bind(rows.prefWidthProperty());
        lines.prefHeightProperty().bind(rows.prefHeightProperty());
        data.resetPossibleWords();
        nodes = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            HBox row = new HBox(25);
            for (int j = 1; j <= 5; j++) {
                LetterNode node = new LetterNode(i, j);
                nodes.add(node);
                row.getChildren().add(node);
                //System.out.println(node.getChar() + ": " + node.getRow() + ", " + node.getCol());
            }

            rows.getChildren().add(row);
        }

        boolean moreThanTarget = gridSolver();

        if (!moreThanTarget) {
            //System.out.println("Another grid");
            rows.getChildren().clear();
            generateGrid(rows);
        } else {
            for (String s: wordsFound) {
                data.addPossibleWord(s);
            }
        }

        grid.getChildren().setAll(rows, lines);
        lines.toBack();
    }

    private boolean gridSolver() {
        int total = 0; // change to 0 after fixing
        wordsFound = new ArrayList<>();
        modeWords = new TreeSet<>();

        if (this.currentMode.equals("English Dictionary")) {
            modeWords = data.getDictionary();
        } else if (this.currentMode.equals("Animals")) {
            modeWords = data.getAnimals();
        } else {
            modeWords = data.getNames();
        }

        for (String s: modeWords) {
            ArrayList<LetterNode> nos = new ArrayList<>();
            for (LetterNode n: nodes) {
                if (n.getChar() == s.charAt(0)) {
                    nos.add(n);
                }
            }

            if (s.equals(findWord(s, 0, nos))) {
                wordsFound.add(s);
                System.out.println(s + " " + findWord(s, 0, nos));
            }

        }

        for (String w: wordsFound) {
            total += w.length();
        }

        if (total >= targetPoints) {
            return true;
        } else {
            //return false when this is done
            return false;
        }

    }

    private String findWord(String w, int i, ArrayList<LetterNode> start) {
        if (!start.isEmpty()) {
            String s = "" + w.charAt(i);
            if (i + 1 < w.length()) {
                for (LetterNode n: start) {
                    return s + findWord(w, i + 1, findNextChars(n, w.charAt(i + 1)));
                }
            }

            return s;
        } else {
            return "";
        }

    }

    private ArrayList<LetterNode> findNextChars(LetterNode currentNode, char lookFor) {
        //possible cols => currentx - 1, currentx, currentx + 1
        //possible rows => currenty - 1, currenty, currenty + 1
        ArrayList<LetterNode> nextChars = new ArrayList<>();

        int currentx = currentNode.getCol();
        int currenty = currentNode.getRow();
        ArrayList<Integer> posRows = new ArrayList<>();
        ArrayList<Integer> posCols = new ArrayList<>();
        posRows.add(currenty);
        posCols.add(currentx);
        if (currenty == 1) {
            posRows.add(currenty + 1);
        } else if (currenty == 5) {
            posRows.add(currenty - 1);
        } else {
            posRows.add(currenty - 1);
            posRows.add(currenty + 1);
        }

        if (currentx == 1) {
            posCols.add(currentx + 1);
        } else if (currentx == 5) {
            posCols.add(currentx - 1);
        } else {
            posCols.add(currentx - 1);
            posCols.add(currentx + 1);
        }

        for (int r: posRows) {
            for (int c: posCols) {
                LetterNode found = (LetterNode) ((HBox) rows.getChildren().get(r - 1)).getChildren().get(c - 1);
                if (!found.equals(currentNode)) {
                    if (found.getChar() == lookFor) {
                        if (!found.getUsed()) {
                            nextChars.add(found);
                        }
                    }
                }
            }
        }

        return nextChars;
    }

    public boolean promptToExit() {
        PropertyManager            propertyManager   = PropertyManager.getManager();
        YesNoCancelDialogSingleton yesNoCancelDialog = YesNoCancelDialogSingleton.getSingleton();

        pause();

        yesNoCancelDialog.show(propertyManager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE),
                propertyManager.getPropertyValue(SAVE_UNSAVED_WORK_MESSAGE));

        String selection = yesNoCancelDialog.getSelection();
        if (selection.equals(YesNoCancelDialogSingleton.YES)) {
            handleSaveProgressRequest();
            return true;
        } else {
            return false;
        }
    }

    private int getInitialTime() {
        int time = 30 * (9 - (this.currentLevel - 1));

        return time;
    }

    public void setCanExit(boolean b) {this.canExit = b;}
    public boolean getCanExit() {return this.canExit;}

    public void setLoggedIn(boolean b) {this.loggedIn = b;}
    public boolean getLoggedIn() {return this.loggedIn;}

    public int    getMaxLevel(String mode) {return data.getLevel(mode);}
    public void setCurrentLevel(int i) {this.currentLevel = i;}
    public int getCurrentLevel() {return this.currentLevel;}

    public void setCurrentMode(String s) {this.currentMode = s;}
    public String getCurrentMode() {return this.currentMode;}

    private int getTargetPoints() { // for calculating target points
        return 75;
    }

    @Override
    public boolean handleLoginRequest(String user, String pass) {
        try {
            this.workspace = (Workspace) app.getWorkspaceComponent();
            File workFile = new File(APP_WORKDIR_PATH.getParameter() + "\\" + user + ".json");
            if (file.getDecryptedPass(workFile.toPath()).equals(pass)) {
                file.loadData(data, workFile.toPath());
                this.loggedIn = true;
                return true;
            } else {
                //testing will return true;
                //return true;
                return false;
            }

        } catch(Exception ex) {
            return false;
        }
    }

    @Override
    public void handleCreateNewProfileRequest(String user, String pass) {
        PropertyManager           props  = PropertyManager.getManager();
        AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
        try {
            this.workspace = (Workspace) app.getWorkspaceComponent();

            file.saveCredentials(user, pass);
            workspace.escape();

        } catch(Exception ex) {
            dialog.show(props.getPropertyValue(CREATE_PROFILE_ERROR_TITLE), props.getPropertyValue(CREATE_PROFILE_ERROR_MESSAGE));
        }
    }

    @Override
    public void handleSaveProgressRequest() {
        try {
            File workFile = new File(APP_WORKDIR_PATH.getParameter() + "\\" + data.getUsername() + ".json");
            file.saveData(data, workFile.toPath());
        } catch(Exception ex) {
            //do nothing
        }
    }

    @Override
    public void handleHelpRequest() {
        System.out.println("pressing help button");
    }
}
