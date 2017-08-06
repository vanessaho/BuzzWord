package ui;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import wordapp.WordApp;
import components.AppStyleArbiter;
import controller.FileController;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import propertymanager.PropertyManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static settings.AppPropertyType.*;
import static settings.InitializationParameters.APP_IMAGEDIR_PATH;

/**
 * This class provides the basic user interface for this application, including all the file controls, but it does not
 * include the workspace, which should be customizable and application dependent.
 *
 * @author Richard McKenna, Ritwik Banerjee
 */
public class AppGUI implements AppStyleArbiter {

    protected FileController fileController;   // to react to file-related controls
    protected Stage          primaryStage;     // the application window
    protected Scene          primaryScene;     // the scene graph
    protected BorderPane     appPane;          // the root node in the scene graph, to organize the containers
    protected VBox           toolbarPane;      // the left toolbar
    protected Button         loginButton;        // button to login the application
    protected Button         createButton;       // button to create new profile on application
    protected Button         helpButton;       // button to seek help
    protected String         applicationTitle; // the application title

    private int appSpecificWindowWidth;  // optional parameter for window width that can be set by the application
    private int appSpecificWindowHeight; // optional parameter for window height that can be set by the application

    /**
     * This constructor initializes the file toolbar for use.
     *
     * @param initPrimaryStage The window for this application.
     * @param initAppTitle     The title of this application, which
     *                         will appear in the window bar.
     * @param app              The app within this gui is used.
     */
    public AppGUI(Stage initPrimaryStage, String initAppTitle, WordApp app) throws IOException, InstantiationException {
        this(initPrimaryStage, initAppTitle, app, -1, -1);
    }

    public AppGUI(Stage primaryStage, String applicationTitle, WordApp appTemplate, int appSpecificWindowWidth, int appSpecificWindowHeight) throws IOException, InstantiationException {
        this.appSpecificWindowWidth = appSpecificWindowWidth;
        this.appSpecificWindowHeight = appSpecificWindowHeight;
        this.primaryStage = primaryStage;
        this.applicationTitle = applicationTitle;
        initializeToolbar();                    // initialize the top toolbar
        initializeToolbarHandlers(appTemplate); // set the toolbar button handlers
        initializeWindow();                     // start the app window (without the application-specific workspace)

    }

    public VBox getToolbarPane() { return toolbarPane; }

    public BorderPane getAppPane() { return appPane; }

    public FileController getController() {return this.fileController; }

    /**
     * Accessor method for getting this application's primary stage's,
     * scene.
     *
     * @return This application's window's scene.
     */
    public Scene getPrimaryScene() { return primaryScene; }

    /**
     * Accessor method for getting this application's window,
     * which is the primary stage within which the full GUI will be placed.
     *
     * @return This application's primary stage (i.e. window).
     */
    public Stage getWindow() { return primaryStage; }

    /****************************************************************************/
    /* BELOW ARE ALL THE PRIVATE HELPER METHODS WE USE FOR INITIALIZING OUR AppGUI */
    /****************************************************************************/

    /**
     * This function initializes all the buttons in the toolbar at the top of
     * the application window. These are related to file management.
     */
    private void initializeToolbar() throws IOException {
        toolbarPane = new VBox(50);
        toolbarPane.setAlignment(Pos.CENTER);
        loginButton = initializeChildButton(toolbarPane, LOGIN_ICON.toString(), LOGIN_TOOLTIP.toString(), LOGIN_WORDS_ICON.toString(), false);
        createButton = initializeChildButton(toolbarPane, CREATE_PROFILE_ICON.toString(), CREATE_PROFILE_TOOLTIP.toString(), CREATE_PROFILE_WORDS_ICON.toString(), false);
        helpButton = initializeChildButton(toolbarPane, HELP_ICON.toString(), HELP_TOOLTIP.toString(), HELP_WORDS_ICON.toString(), false);

        toolbarPane.setMargin(helpButton, new Insets(100, 0, 0, 0));
        toolbarPane.setPadding(new Insets(20, 20, 10, 20));
    }

    public Button getLoginButton() {return this.loginButton;}
    public Button getCreateButton() {return this.createButton;}
    public Button getHelpButton() {return this.helpButton;}

    private void initializeToolbarHandlers(WordApp app) throws InstantiationException {
        AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();

        try {
            Method         getFileControllerClassMethod = app.getClass().getMethod("getFileControllerClass");
            String         fileControllerClassName      = (String) getFileControllerClassMethod.invoke(app);
            Class<?>       klass                        = Class.forName("controller." + fileControllerClassName);
            Constructor<?> constructor                  = klass.getConstructor(WordApp.class);
            fileController = (FileController) constructor.newInstance(app);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
            PropertyManager           props  = PropertyManager.getManager();
            dialog.show(props.getPropertyValue(INITIALIZATION_ERROR_TITLE), props.getPropertyValue(INITIALIZATION_ERROR_MESSAGE));
            System.exit(1);
        }

    }

    public void showPic() throws IOException {
        HBox center = new HBox();

        PropertyManager propertyManager = PropertyManager.getManager();
        URL imgDirURL = WordApp.class.getClassLoader().getResource(APP_IMAGEDIR_PATH.getParameter());
        if (imgDirURL == null)
            throw new FileNotFoundException("Image resources folder does not exist.");
        try (InputStream appPicStream = Files.newInputStream(Paths.get(imgDirURL.toURI()).resolve(propertyManager.getPropertyValue(APP_PIC)))) {
            ImageView pic = new ImageView(new Image(appPicStream));
            center.getChildren().setAll(pic);
            center.setAlignment(Pos.CENTER);
            appPane.setCenter(center);
        } catch (URISyntaxException e) {
            System.out.println("BEE ERROR");
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            PropertyManager           props  = PropertyManager.getManager();
            dialog.show(props.getPropertyValue(PROPERTIES_LOAD_ERROR_TITLE), props.getPropertyValue(PROPERTIES_LOAD_ERROR_MESSAGE));
        }
    }

    public void enableButtonVisibility(boolean visible) {
        loginButton.setVisible(visible);
        createButton.setVisible(visible);
        helpButton.setVisible(true);
    }

    public void enableHelp() {helpButton.setDisable(false);}


    public void enableButtons(boolean b) {
        loginButton.setDisable(!b);
        createButton.setDisable(!b);
        helpButton.setDisable(!b);
    }

    // INITIALIZE THE WINDOW (i.e. STAGE) PUTTING ALL THE CONTROLS
    // THERE EXCEPT THE WORKSPACE, WHICH WILL BE ADDED THE FIRST
    // TIME A NEW Page IS CREATED OR LOADED
    private void initializeWindow() throws IOException {
        PropertyManager propertyManager = PropertyManager.getManager();

        // SET THE WINDOW TITLE
        primaryStage.setTitle(applicationTitle);

        // GET THE SIZE OF THE SCREEN
        Screen      screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        // AND USE IT TO SIZE THE WINDOW
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());


        // ADD THE TOOLBAR ONLY, NOTE THAT THE WORKSPACE
        // HAS BEEN CONSTRUCTED, BUT WON'T BE ADDED UNTIL
        // THE USER STARTS EDITING A COURSE
        appPane = new BorderPane();
        appPane.setLeft(toolbarPane);
        appPane.setMargin(toolbarPane, new Insets(10, 10, 10, 10));
        primaryScene = appSpecificWindowWidth < 1 || appSpecificWindowHeight < 1 ? new Scene(appPane)
                : new Scene(appPane,
                appSpecificWindowWidth,
                appSpecificWindowHeight);

        URL imgDirURL = WordApp.class.getClassLoader().getResource(APP_IMAGEDIR_PATH.getParameter());
        if (imgDirURL == null)
            throw new FileNotFoundException("Image resources folder does not exist.");
        try (InputStream appLogoStream = Files.newInputStream(Paths.get(imgDirURL.toURI()).resolve(propertyManager.getPropertyValue(APP_LOGO)))) {
            primaryStage.getIcons().add(new Image(appLogoStream));
        } catch (URISyntaxException e) {
            System.out.println("APPGUI ERROR");
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            PropertyManager           props  = PropertyManager.getManager();
            dialog.show(props.getPropertyValue(PROPERTIES_LOAD_ERROR_TITLE), props.getPropertyValue(PROPERTIES_LOAD_ERROR_MESSAGE));
            System.exit(1);
        }

        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }

    /**
     * This is a public helper method for initializing a simple button with
     * an icon and tooltip and placing it into a toolbar.
     *
     * @param toolbarPane Toolbar pane into which to place this button.
     * @param icon        Icon image file name for the button.
     * @param tooltip     Tooltip to appear when the user mouses over the button.
     * @param disabled    true if the button is to start off disabled, false otherwise.
     * @return A constructed, fully initialized button placed into its appropriate
     * pane container.
     */
    public Button initializeChildButton(Pane toolbarPane, String icon, String tooltip, String message, boolean disabled) throws IOException {
        PropertyManager propertyManager = PropertyManager.getManager();

        URL imgDirURL = WordApp.class.getClassLoader().getResource(APP_IMAGEDIR_PATH.getParameter());
        if (imgDirURL == null)
            throw new FileNotFoundException("Image resources folder does not exist.");

        Button button = new Button(propertyManager.getPropertyValue(message));
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.TOP_LEFT);
        try (InputStream imgInputStream = Files.newInputStream(Paths.get(imgDirURL.toURI()).resolve(propertyManager.getPropertyValue(icon)))) {
            Image buttonImage = new Image(imgInputStream);
            ImageView imageview = new ImageView(buttonImage);
            imageview.setFitHeight(36);
            imageview.setFitWidth(36);
            button.setDisable(disabled);
            button.setGraphic(imageview);
            Tooltip buttonTooltip = new Tooltip(propertyManager.getPropertyValue(tooltip));
            button.setTooltip(buttonTooltip);
            toolbarPane.getChildren().add(button);

            //button.setStyle("-fx-text-fill: white;");
        } catch (URISyntaxException e) {
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            PropertyManager           props  = PropertyManager.getManager();
            dialog.show(props.getPropertyValue(LOAD_IMAGE_ERROR_TITLE), props.getPropertyValue(LOAD_IMAGE_ERROR_MESSAGE));
            System.exit(1);
        }

        return button;
    }

    public ImageView loadImage(String icon) throws IOException {
        PropertyManager propertyManager = PropertyManager.getManager();
        URL imgDirURL = WordApp.class.getClassLoader().getResource(APP_IMAGEDIR_PATH.getParameter());
        ImageView imageview = new ImageView();
        if (imgDirURL == null)
            throw new FileNotFoundException("Image resources folder does not exist.");

        try (InputStream imgInputStream = Files.newInputStream(Paths.get(imgDirURL.toURI()).resolve(propertyManager.getPropertyValue(icon)))) {
            Image buttonImage = new Image(imgInputStream);
            imageview = new ImageView(buttonImage);
            imageview.setFitHeight(36);
            imageview.setFitWidth(36);

            //button.setStyle("-fx-text-fill: white;");
        } catch (URISyntaxException e) {
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            PropertyManager           props  = PropertyManager.getManager();
            dialog.show(props.getPropertyValue(LOAD_IMAGE_ERROR_TITLE), props.getPropertyValue(LOAD_IMAGE_ERROR_MESSAGE));
        }

        return imageview;
    }

    /**
     * This function specifies the CSS style classes for the controls managed
     * by this framework.
     */
    @Override
    public void initStyle() {
        // currently, we do not provide any stylization at the framework-level
    }
}
