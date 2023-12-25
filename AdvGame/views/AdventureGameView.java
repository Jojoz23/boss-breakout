package views;

import AdventureModel.*;
import RoomCompass.Compass;
import AdventureModel.Passage;
import Commands.*;
import Commands.MovementCommands.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.layout.*;
import javafx.scene.input.KeyEvent; //you will need these!
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.event.EventHandler; //you will need this too!
import javafx.scene.AccessibleRole;
import RoomCompass.RoomCompass;
import views.bars.BarView;
import views.bars.HealthBarView;
import views.bars.StrengthBarView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


/**
 * Class AdventureGameView.
 *
 * This is the Class that will visualize your model.
 * You are asked to demo your visualization via a Zoom
 * recording. Place a link to your recording below.
 *
 * ZOOM LINK: <a href="https://utoronto-my.sharepoint.com/:v:/r/personal/dale_mejia_mail_utoronto_ca/Documents/a2doc.mp4?csf=1&web=1&e=z9dafu&nav=eyJyZWZlcnJhbEluZm8iOnsicmVmZXJyYWxBcHAiOiJTdHJlYW1XZWJBcHAiLCJyZWZlcnJhbFZpZXciOiJTaGFyZURpYWxvZyIsInJlZmVycmFsQXBwUGxhdGZvcm0iOiJXZWIiLCJyZWZlcnJhbE1vZGUiOiJ2aWV3In19">...</a>
 * PASSWORD: N/A
 */
public class AdventureGameView {

    AdventureGame model; //model of the game
    Stage stage; //stage on which all is rendered
    Button saveButton, loadButton, helpButton; //buttons
    Boolean helpToggle = false; //is help on display?

    GridPane gridPane = new GridPane(); //to hold images and buttons
    Label roomDescLabel = new Label(); //to hold room description and/or instructions
    VBox objectsInRoom = new VBox(); //to hold room items
    ScrollPane objInRoomVis = new ScrollPane();//items

    VBox objRoomEve = new VBox();// holds both label and items
    VBox objectsInInventory = new VBox(); //to hold inventory items
    ScrollPane objInInvenVis = new ScrollPane();// items

    VBox objInvEve = new VBox();// holds both label and items
    ImageView roomImageView; //to hold room image
    TextField inputTextField; //for user input
    Compass compass;
    CommandCenter commandCenter;
    boolean inputEnabled;

    private MediaPlayer mediaPlayer; //to play audio
    private boolean mediaPlaying; //to know if the audio is playing

    boolean playerStatsToggle; //to know if player stats is on or off
    boolean objRoomToggle;// to know if object room display is on or off

    boolean objInvToggle;// to know if object inventory display is on or off
    BarView healthBar; // to access the health bar
    BarView strengthBar; // to access the strength bar

    VBox playerStats; // for player stats

    /**
     * Adventure Game View Constructor
     * __________________________
     * Initializes attributes
     * @param model the current AdventureGame model
     * @param stage the stage on which graphics are rendered
     */
    public AdventureGameView(AdventureGame model, Stage stage) {
        this.model = model;
        this.stage = stage;
        RoomCompass roomCompass = new RoomCompass(model);
        this.compass = new Compass(roomCompass);
        this.commandCenter = new CommandCenter();
        this.inputEnabled = true;
        intiUI();
    }

    /**
     * Initialize the UI
     */
    public void intiUI() {

        // setting up the stage
        this.stage.setTitle("mejiadal's Adventure Game");

        //Inventory + Room items
        objectsInInventory.setSpacing(10);
        objectsInInventory.setAlignment(Pos.TOP_CENTER);
        objectsInRoom.setSpacing(10);
        objectsInRoom.setAlignment(Pos.TOP_CENTER);

        // GridPane, anyone?
        gridPane.setPadding(new Insets(20));
        gridPane.setBackground(new Background(new BackgroundFill(
                Color.valueOf("#000000"),
                new CornerRadii(0),
                new Insets(0)
        )));

        //Three columns, three rows for the GridPane
        ColumnConstraints column1 = new ColumnConstraints(150);
        ColumnConstraints column2 = new ColumnConstraints(650);
        ColumnConstraints column3 = new ColumnConstraints(150);
        column3.setHgrow( Priority.SOMETIMES ); //let some columns grow to take any extra space
        column1.setHgrow( Priority.SOMETIMES );

        // Row constraints
        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints( 550 );
        RowConstraints row3 = new RowConstraints();
        row1.setVgrow( Priority.SOMETIMES );
        row3.setVgrow( Priority.SOMETIMES );

        gridPane.getColumnConstraints().addAll( column1 , column2 , column1 );
        gridPane.getRowConstraints().addAll( row1 , row2 , row1 );

        // Buttons
        saveButton = new Button("Save");
        saveButton.setId("Save");
        customizeButton(saveButton, 100, 50);
        makeButtonAccessible(saveButton, "Save Button", "This button saves the game.", "This button saves the game. Click it in order to save your current progress, so you can play more later.");
        addSaveEvent();

        loadButton = new Button("Load");
        loadButton.setId("Load");
        customizeButton(loadButton, 100, 50);
        makeButtonAccessible(loadButton, "Load Button", "This button loads a game from a file.", "This button loads the game from a file. Click it in order to load a game that you saved at a prior date.");
        addLoadEvent();

        helpButton = new Button("Instructions");
        helpButton.setId("Instructions");
        customizeButton(helpButton, 200, 50);
        makeButtonAccessible(helpButton, "Help Button", "This button gives game instructions.", "This button gives instructions on the game controls. Click it to learn how to play.");
        addInstructionEvent();

        HBox topButtons = new HBox();
        topButtons.getChildren().addAll(saveButton, helpButton, loadButton);
        topButtons.setSpacing(10);
        topButtons.setAlignment(Pos.CENTER);

        inputTextField = new TextField();
        inputTextField.setFont(new Font("Arial", 16));
        inputTextField.setFocusTraversable(true);

        inputTextField.setAccessibleRole(AccessibleRole.TEXT_AREA);
        inputTextField.setAccessibleRoleDescription("Text Entry Box");
        inputTextField.setAccessibleText("Enter commands in this box.");
        inputTextField.setAccessibleHelp("This is the area in which you can enter commands you would like to play.  Enter a command and hit return to continue.");
        addTextHandlingEvent(); //attach an event to this input field

        //labels for inventory and room items
        Label objLabel =  new Label("Objects in Room");
        objLabel.setAlignment(Pos.CENTER);
        objLabel.setStyle("-fx-text-fill: white;");
        objLabel.setFont(new Font("Arial", 16));

        Label invLabel =  new Label("Your Inventory");
        invLabel.setAlignment(Pos.CENTER);
        invLabel.setStyle("-fx-text-fill: white;");
        invLabel.setFont(new Font("Arial", 16));

        // Setting up the displays
        objInRoomVis = new ScrollPane(objectsInRoom);
        objInRoomVis.setPadding(new Insets(10));
        objInRoomVis.setStyle("-fx-background: #000000; -fx-background-color:transparent;");
        objInRoomVis.setFitToWidth(true);

        objRoomToggle = true;

        objRoomEve = new VBox();
        objRoomEve.setSpacing(5);
        objRoomEve.getChildren().addAll(objLabel, objInRoomVis);

        objInInvenVis = new ScrollPane(objectsInInventory);
        objInInvenVis.setPadding(new Insets(10));
        objInInvenVis.setStyle("-fx-background: #000000; -fx-background-color:transparent;");
        objInInvenVis.setFitToWidth(true);

        objInvToggle = true;

        objInvEve = new VBox();
        objInvEve.setSpacing(5);
        objInvEve.getChildren().addAll(invLabel, objInInvenVis);


        //add all the widgets to the GridPane
        gridPane.add( objRoomEve, 0, 1, 1, 1 );  // Add obj in room display
        gridPane.add( topButtons, 1, 0, 1, 1 );  // Add buttons
        gridPane.add( objInvEve, 2, 1, 1, 1 );  // Add obj in inven display


        Label commandLabel = new Label("What would you like to do?");
        commandLabel.setStyle("-fx-text-fill: white;");
        commandLabel.setFont(new Font("Arial", 16));

        updateScene(""); //method displays an image and whatever text is supplied
        updateItems(); //update items shows inventory and objects in rooms

        // adding the text area and submit button to a VBox
        VBox textEntry = new VBox();
        textEntry.setStyle("-fx-background-color: #000000;");
        textEntry.setPadding(new Insets(20, 20, 20, 20));
        textEntry.getChildren().addAll(commandLabel, inputTextField);
        textEntry.setSpacing(10);
        textEntry.setAlignment(Pos.CENTER);
        gridPane.add( textEntry, 1, 2, 2, 1 );

        this.playerStatsToggle = false;
        playerStats = new VBox();
        playerStats.setSpacing(10);
        playerStats.setAlignment(Pos.CENTER_LEFT);
        // event for hiding or opening player stats
        playerStatsAndObjEvent();

        CompassView compassView = new CompassView(compass);

        // Render everything
        var scene = new Scene( gridPane ,  1000, 800);
        scene.setFill(Color.BLACK);
        this.stage.setScene(scene);
        this.stage.setResizable(false);
        this.stage.show();

        setEventFilter();
    }

    /**
     * Add an event filter to the scene to listen for any inputted key presses.
     */
    private void setEventFilter() {
        Scene scene = stage.getScene();
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                if (inputEnabled) {
                    stopArticulation();
                    setCommand(event);
                    executeCommand();
                }
            }
        });
    }

    /**
     * Given a key press, assign the command center the action bound to said key.
     *
     * @param event The key that was pressed.
     */
    public void setCommand(KeyEvent event) {
        switch (event.getCode()) {
            case W -> commandCenter.setCommand(new MoveUpCommand(model));
            case S -> commandCenter.setCommand(new MoveDownCommand(model));
            case A -> commandCenter.setCommand(new MoveLeftCommand(model));
            case D -> commandCenter.setCommand(new MoveRightCommand(model));
            case E -> commandCenter.setCommand(new InspectCommand(roomDescLabel, model));
            default -> commandCenter.setCommand(new NothingCommand());
        }
    }

    /**
     * Execute the command assigned to the command center.
     * <p>
     * If the assigned command was a move, then do either of three things:
     * 1. If the route is available and unblocked, execute a transition and update the scene.
     * 2. If the route is available but blocked, tell the player which key item they need to proceed.
     * 3. If the route is unavailable, inform the player that they may not go this way.
     */
    private void executeCommand() {
        // If the command was a movement command:
        if (commandCenter.getCommand() instanceof MovementCommand) {

            // Keep track of the previous and current room after a move.
            int previousRoom = model.player.getCurrentRoom().getRoomNumber();
            commandCenter.execute();
            int currentRoom = model.player.getCurrentRoom().getRoomNumber();

            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> {
                inputEnabled = true;
                updateScene("");
                updateItems();
            });

            // If the room has changed:
            if (previousRoom != currentRoom) {
                inputEnabled = false;
                executeTransition();
                pause.play();

                if (currentRoom == 11) {
                    PauseTransition pause2 = new PauseTransition(Duration.seconds(15));
                    pause2.setOnFinished(event -> {
                        try {
                            create_BossView();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    pause2.play();
                }
            }

            // Otherwise:
            else {
                String direction = getMovementDirection(); // Get the direction of the attempted move.
                boolean routeAvailable =
                        model.player.getCurrentRoom().getMotionTable().optionExists(direction);

                // If the route to a room is available but blocked:
                if (routeAvailable) {
                    Passage targetedPassage = getTargetedPassage(direction);
                    assert targetedPassage != null; // Should always be true for a route is available.
                    roomDescLabel.setText
                            ("You will need " + targetedPassage.getKeyName() + " to enter here!");
                }

                // If the route does not exist:
                else {
                    roomDescLabel.setText("You cannot go this way!");
                }
            }
        }

        // Otherwise, execute the command as is.
        else { commandCenter.execute(); }

    }

    /**
     * Given a direction, return the passage corresponding to that direction.
     *
     * @param direction the direction of the desired passage.
     * @return the passage correlating to the given direction.
     */
    private Passage getTargetedPassage(String direction) {
        for (Passage passage : model.player.getCurrentRoom().getMotionTable().passageTable) {
            if (passage.getDirection().equalsIgnoreCase(direction)) {
                return passage;
            }
        }
        return null;
    }

    /**
     * GIven that the command center's assigned command is a movement command, return its String direction.
     *
     * @return the String direction of the attempted move.
     */
    private String getMovementDirection() {
        Command current = this.commandCenter.getCommand();
        assert current instanceof MovementCommand;

        if (current instanceof MoveUpCommand) {
            return "UP";
        } else if (current instanceof MoveDownCommand) {
            return "DOWN";
        } else if (current instanceof MoveLeftCommand) {
            return "LEFT";
        } else {
            return "RIGHT";
        }
    }

    /**
     * Change the room image to a black image and play a footstep SFX to indicate a room transition.
     */
    private void executeTransition() {
        String roomImage = model.getDirectoryName() + "/room-images/transition.png";
        Image transitionImage = new Image(roomImage);
        roomImageView = new ImageView(transitionImage);
        roomImageView.setPreserveRatio(true);
        roomImageView.setFitWidth(400);
        roomImageView.setFitHeight(400);

        roomDescLabel.setText("You are moving into a new room.");
        roomDescLabel.setPrefWidth(500);
        roomDescLabel.setPrefHeight(500);
        roomDescLabel.setTextOverrun(OverrunStyle.CLIP);
        roomDescLabel.setWrapText(true);
        VBox roomPane = new VBox(roomImageView, roomDescLabel);
        roomPane.setPadding(new Insets(10));
        roomPane.setAlignment(Pos.TOP_CENTER);
        roomPane.setStyle("-fx-background-color: #000000;");

        objectsInRoom.getChildren().clear();
        objectsInInventory.getChildren().clear();

        String musicFile = model.getDirectoryName() + "/sounds/transition.mp3";
        Media sound = new Media(new File(musicFile).toURI().toString());

        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
        mediaPlaying = true;

        gridPane.add(roomPane, 1, 1);
        stage.sizeToScene();
    }

    /**
     * makeButtonAccessible
     * __________________________
     * For information about ARIA standards, see
     * <a href="https://www.w3.org/WAI/standards-guidelines/aria/"></a>
     *
     * @param inputButton the button to add screenreader hooks to
     * @param name        ARIA name
     * @param shortString ARIA accessible text
     * @param longString  ARIA accessible help text
     */
    public static void makeButtonAccessible(Button inputButton, String name, String shortString, String longString) {
        inputButton.setAccessibleRole(AccessibleRole.BUTTON);
        inputButton.setAccessibleRoleDescription(name);
        inputButton.setAccessibleText(shortString);
        inputButton.setAccessibleHelp(longString);
        inputButton.setFocusTraversable(true);
    }

    /**
     * customizeButton
     * __________________________
     *
     * @param inputButton the button to make stylish :)
     * @param w           width
     * @param h           height
     */
    protected void customizeButton(Button inputButton, int w, int h) {
        inputButton.setPrefSize(w, h);
        inputButton.setFont(new Font("Arial", 16));
        inputButton.setStyle("-fx-background-color: #17871b; -fx-text-fill: white;");
    }

    /**
     * customizeButton
     * __________________________
     *
     * @param inputButton the button to make stylish, with contrast ratio in mind :)
     * @param w           width
     * @param h           height
     */
    protected void customizeObjectButton(Button inputButton, int w, int h) {
        inputButton.setPrefSize(w, h);
        inputButton.setFont(new Font("Arial", 16));
        inputButton.setStyle("-fx-background-color: #b55c21; -fx-text-fill: #ffffff;");
    }

    /**
     * addTextHandlingEvent
     * __________________________
     * Add an event handler to the myTextField attribute 
     *
     * Your event handler should respond when users 
     * hits the ENTER or TAB KEY. If the user hits 
     * the ENTER Key, strip white space from the
     * input to myTextField and pass the stripped 
     * string to submitEvent for processing.
     *
     * If the user hits the TAB key, move the focus 
     * of the scene onto any other node in the scene 
     * graph by invoking requestFocus method.
     */
    private void addTextHandlingEvent() {
        EventHandler<KeyEvent> eventHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                if (event.getCode().equals(KeyCode.ENTER)) {
                    String input = inputTextField.getText().strip();
                    submitEvent(input);
                    inputTextField.clear();
                } else if (event.getCode().equals(KeyCode.TAB)) {
                    gridPane.requestFocus();
                }
            }
        };

        inputTextField.setOnKeyPressed(eventHandler);
    }


    /**
     * submitEvent
     * __________________________
     *
     * @param text the command that needs to be processed
     */
    private void submitEvent(String text) {

        text = text.strip(); //get rid of white space
        stopArticulation(); //if speaking, stop

        if (text.equalsIgnoreCase("LOOK") || text.equalsIgnoreCase("L")) {
            String roomDesc = this.model.getPlayer().getCurrentRoom().getRoomDescription();
            String objectString = this.model.getPlayer().getCurrentRoom().getObjectString();
            if (!objectString.isEmpty()) roomDescLabel.setText(roomDesc + "\n\nObjects in this room:\n" + objectString);
            articulateRoomDescription(); //all we want, if we are looking, is to repeat description.
            return;
        } else if (text.equalsIgnoreCase("HELP") || text.equalsIgnoreCase("H")) {
            showInstructions();
            return;
        } else if (text.equalsIgnoreCase("COMMANDS") || text.equalsIgnoreCase("C")) {
            showCommands(); //this is new!  We did not have this command in A1
            return;
        } else if (text.equalsIgnoreCase("Triple") || text.equalsIgnoreCase("T")) {
            if (model.player.getCurrentRoom().getRoomNumber() % 2 == 0){
                Triple tripleGame = new Triple();
                tripleGame.playminiGame();
                if (tripleGame.Won()){
                    ArrayList<Passage> passages = (ArrayList<Passage>) model.player.getCurrentRoom().getMotionTable().passageTable;
                    Random rand = new Random();
                    Passage randomPassage = passages.get(rand.nextInt(passages.size()));
                    String direction = randomPassage.getDirection();
                    System.out.println("You won! You are now moved to a random room.");
                    submitEvent(direction);
                } else {
                    model.player.changeHealthBar(-10);
                }

            } else {
                System.out.println("You cannot play Triple in this Room!.");
            }
            return;
        } else if (text.equalsIgnoreCase("Flip") || text.equalsIgnoreCase("F")) {
            if (model.player.getCurrentRoom().getRoomNumber() % 3 == 0) {
                coinFlip coinflipGame = new coinFlip();
                coinflipGame.playminiGame();
                if (coinflipGame.Won()) {
                    ArrayList<Passage> passages = (ArrayList<Passage>) model.player.getCurrentRoom().getMotionTable().passageTable;
                    Random rand = new Random();
                    Passage randomPassage = passages.get(rand.nextInt(passages.size()));
                    String direction = randomPassage.getDirection();
                    System.out.println("You won! You are now moved to a random room.");
                    submitEvent(direction);
                } else {
                    healthBar.change(-20);
                }

            } else {
                System.out.println("You cannot play Flip in this Room!.");
            }
            return;
        } else if (text.equalsIgnoreCase("Dice") || text.equalsIgnoreCase("D")) {
            if (model.player.getCurrentRoom().getRoomNumber() == 1 || model.player.
                    getCurrentRoom().getRoomNumber() == 5 || model.player.getCurrentRoom().getRoomNumber() == 7) {
                Dice diceGame = new Dice();
                diceGame.playminiGame();
                if (diceGame.Won()) {
                    ArrayList<Passage> passages = (ArrayList<Passage>) model.player.getCurrentRoom().getMotionTable().passageTable;
                    Random rand = new Random();
                    Passage randomPassage = passages.get(rand.nextInt(passages.size()));
                    String direction = randomPassage.getDirection();
                    System.out.println("You won! You are now moved to a random room.");
                    submitEvent(direction);
                } else {
                    healthBar.change(-5);
                }

            } else {
                System.out.println("You cannot play Dice in this Room!.");
            }
            return;
        }

        //try to move!
        String output = this.model.interpretAction(text); //process the command!

        if (output == null || (!output.equals("GAME OVER") && !output.equals("FORCED") && !output.equals("HELP"))) {
            updateScene(output);
            updateItems();
        } else if (output.equals("GAME OVER")) {
            updateScene("");
            updateItems();
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> {
                try {
                    create_BossView();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            pause.play();

        } else if (output.equals("FORCED")) {
            updateScene("");
            updateItems();
            PauseTransition pause = new PauseTransition(Duration.seconds(6));
            pause.setOnFinished(event -> {
                submitEvent("FORCED");
            });
            pause.play();
        }
    }

    /**
     * Creates the Boss View needed for the battle system
     *
     * @throws IOException
     */
    public void create_BossView() throws IOException {
        BossView boss_view = new BossView(this.model, this.stage);
        gridPane.requestFocus();
    }

    /**
     * showCommands
     * __________________________
     * update the text in the GUI (within roomDescLabel)
     * to show all the moves that are possible from the
     * current room.
     */
    private void showCommands() {
        roomDescLabel.setText("Your possible moves here are: \n" + model.getPlayer().getCurrentRoom().getCommands());
    }


    /**
     * updateScene
     * __________________________
     *
     * Show the current room, and print some text below it.
     * If the input parameter is not null, it will be displayed
     * below the image.
     * Otherwise, the current room description will be dispplayed
     * below the image.
     *
     * @param textToDisplay the text to display below the image.
     */
    public void updateScene(String textToDisplay) {

        getRoomImage(); //get the image of the current room
        formatText(textToDisplay); //format the text to display
        roomDescLabel.setPrefWidth(500);
        roomDescLabel.setPrefHeight(500);
        roomDescLabel.setTextOverrun(OverrunStyle.CLIP);
        roomDescLabel.setWrapText(true);
        VBox roomPane = new VBox(roomImageView,roomDescLabel);
        roomPane.setPadding(new Insets(10));
        roomPane.setAlignment(Pos.TOP_CENTER);
        roomPane.setStyle("-fx-background-color: #000000;");

        gridPane.add(roomPane, 1, 1);
        stage.sizeToScene();
        compass.update();


        //finally, articulate the description
        if (textToDisplay == null || textToDisplay.isBlank()) articulateRoomDescription();
    }

    /**
     * formatText
     * __________________________
     *
     * Format text for display.
     *
     * @param textToDisplay the text to be formatted for display.
     */
    private void formatText(String textToDisplay) {
        if (textToDisplay == null || textToDisplay.isBlank()) {
            String roomDesc = this.model.getPlayer().getCurrentRoom().getRoomDescription() + "\n";
            String objectString = this.model.getPlayer().getCurrentRoom().getObjectString();
            if (objectString != null && !objectString.isEmpty()) roomDescLabel.setText(roomDesc + "\n\nObjects in this room:\n" + objectString);
            else roomDescLabel.setText(roomDesc);
        } else roomDescLabel.setText(textToDisplay);
        roomDescLabel.setStyle("-fx-text-fill: white;");
        roomDescLabel.setFont(new Font("Arial", 16));
        roomDescLabel.setAlignment(Pos.CENTER);
    }

    /**
     * getRoomImage
     * __________________________
     *
     * Get the image for the current room and place 
     * it in the roomImageView 
     */
    private void getRoomImage() {

        int roomNumber = this.model.getPlayer().getCurrentRoom().getRoomNumber();
        String roomImage = this.model.getDirectoryName() + "/room-images/" + roomNumber + ".png";

        Image roomImageFile = new Image(roomImage);
        roomImageView = new ImageView(roomImageFile);
        roomImageView.setPreserveRatio(true);
        roomImageView.setFitWidth(400);
        roomImageView.setFitHeight(400);

        //set accessible text
        roomImageView.setAccessibleRole(AccessibleRole.IMAGE_VIEW);
        roomImageView.setAccessibleText(this.model.getPlayer().getCurrentRoom().getRoomDescription());
        roomImageView.setFocusTraversable(true);
    }

    /**
     * updateItems
     * __________________________
     *
     * This method is partially completed, but you are asked to finish it off.
     *
     * The method should populate the objectsInRoom and objectsInInventory Vboxes.
     * Each Vbox should contain a collection of nodes (Buttons, ImageViews, you can decide)
     * Each node represents a different object.
     * 
     * Images of each object are in the assets 
     * folders of the given adventure game.
     */
    public void updateItems() {

        //write some code here to add images of objects in a given room to the objectsInRoom Vbox
        //write some code here to add images of objects in a player's inventory room to the objectsInInventory Vbox
        //please use setAccessibleText to add "alt" descriptions to your images!
        //the path to the image of any is as follows:
        //this.model.getDirectoryName() + "/objectImages/" + objectName + ".jpg";

        objectsInRoom.getChildren().clear();
        objectsInInventory.getChildren().clear();

        addImageButtons(this.model.getPlayer().getCurrentRoom().getObjectsInRoom(), objectsInRoom);
        addImageButtons(this.model.getPlayer().inventory, objectsInInventory);

    }

    private void addImageButtons(ArrayList<AdventureObject> lst, VBox vbox) {
        for (AdventureObject object : lst) {
            String objectName = object.getName();
            String objectDesc = object.getDescription();
            String objectHelp = object.getHelpTxt();
            Image objectImage = new Image(this.model.getDirectoryName() + "/objectImages/" + objectName + ".png");
            ImageView objectImageView = new ImageView(objectImage);
            objectImageView.setPreserveRatio(true);
            objectImageView.setFitWidth(100);
            objectImageView.setFitHeight(100);

            Button objectButton = new Button(objectName, objectImageView);
            objectButton.setContentDisplay(ContentDisplay.TOP);
            customizeObjectButton(objectButton, 100, 100);
            int othernNum = 0;

            // Go through all the button nodes to find how many times this item is duplicated
            for (Node node : vbox.getChildren()) {
                if (node instanceof Button) {
                    if (((Button) node).getText().startsWith(objectName.split("x")[0])) {
                        if (((Button) node).getText().split("x").length != 2) {
                            othernNum = 1;
                        } else {
                            othernNum = Integer.parseInt(((Button) node).getText().split("x")[1]);
                        }
                    }
                }
            }

            // Add 1 to that count to account for this item
            int count = 1 + othernNum;

            // if there was no duplicates, put this item on screen
            if (count == 1) {
                makeButtonAccessible(objectButton, objectName, objectName, objectDesc);
                objectButton.setTooltip(new Tooltip(objectHelp));
                objectButton.setFont(Font.font(14));
                vbox.getChildren().add(objectButton);

                // Different functionality for left and right mouse click
                EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                            if (objectsInRoom.getChildren().contains(objectButton)) {
                                submitEvent("take " + object.getName());
                            } else if (objectsInInventory.getChildren().contains(objectButton)) {
                                submitEvent("drop " + object.getName());
                            }

                    }
                };

                objectButton.setOnMouseClicked(eventHandler);
            }
            // else update the button there to reflect how many duplicates of this item are there
            else {
                Button button = (Button) vbox.getChildren().stream().filter(node -> node instanceof Button && ((Button) node).getText().startsWith(objectName.split("x")[0])).findAny().get();
                button.setText(button.getText().split("x")[0] + "x" + count);
            }

        }
    }

    /*
     * Show the game instructions.
     *
     * If helpToggle is FALSE:
     * -- display the help text in the CENTRE of the gridPane (i.e. within cell 1,1)
     * -- use whatever GUI elements to get the job done!
     * -- set the helpToggle to TRUE
     * -- REMOVE whatever nodes are within the cell beforehand!
     *
     * If helpToggle is TRUE:
     * -- redraw the room image in the CENTRE of the gridPane (i.e. within cell 1,1)
     * -- set the helpToggle to FALSE
     * -- Again, REMOVE whatever nodes are within the cell beforehand!
     */
    public void showInstructions() {
        if (helpToggle == Boolean.FALSE) {
            String help = model.getInstructions().strip();
            Label helpLabel = new Label(help);
            helpLabel.setWrapText(true);
            helpLabel.setStyle("-fx-text-fill: white;");
            helpLabel.setFont(new Font("Arial", 16));
            helpLabel.setAlignment(Pos.CENTER);

            ScrollPane helpPane = new ScrollPane();
            helpPane.setStyle("-fx-background: #000000; -fx-background-color:transparent;");
            helpPane.setContent(helpLabel);
            helpPane.setFitToWidth(true);

            removeByCell(1, 1);
            gridPane.add(helpPane, 1, 1);

            helpToggle = Boolean.TRUE;
        } else {
            removeByCell(1, 1);

            VBox roomPane = new VBox(roomImageView, roomDescLabel);
            roomPane.setPadding(new Insets(10));
            roomPane.setAlignment(Pos.TOP_CENTER);
            roomPane.setStyle("-fx-background-color: #000000;");

            gridPane.add(roomPane, 1, 1);
            stage.sizeToScene();

            helpToggle = Boolean.FALSE;
        }
    }

    private void removeByCell(int row, int col) {
        gridPane.getChildren().removeIf
                (node -> GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col);
    }

    /**
     * This method handles the event related to the
     * help button.
     */
    public void addInstructionEvent() {
        helpButton.setOnAction(e -> {
            stopArticulation(); //if speaking, stop
            showInstructions();
        });
    }

    /**
     * Responds to a 'H' click by showing the or closing the player's stats
     */
    public void playerStatsAndObjEvent(){
        // Initialize them
        healthBar = new HealthBarView(this.model.getPlayer(), this);
        strengthBar = new StrengthBarView(this.model.getPlayer(), this);
        this.model.getPlayer().setStrengthBar(strengthBar);
        this.model.getPlayer().setHealthBar(healthBar);

        EventHandler<KeyEvent> keyBindClick = new EventHandler<KeyEvent>(){

            @Override
            public void handle(KeyEvent event){
                // close or open health bar
                if (event.getCode().equals(KeyCode.H)){
                    showPlayerStats();
                }
                // close or open objects in room display
                else if(event.getCode().equals(KeyCode.R)){
                    if(objRoomToggle) {
                        removeByCell(1, 0);
                        objRoomToggle = false;
                    }
                    else{
                        removeByCell(1, 0);
                        gridPane.add(objRoomEve, 0, 1, 1, 1);
                        objRoomToggle = true;
                    }

                }
                // close or open inventory display
                else if(event.getCode().equals(KeyCode.I)){
                    if(objInvToggle) {
                        removeByCell(1, 2);
                        objInvToggle = false;
                    }
                    else{
                        removeByCell(1, 2);
                        gridPane.add(objInvEve, 2, 1, 1, 1);
                        objInvToggle = true;
                    }
                }
            }
        };

        // Make the gridpane wait for it
        this.gridPane.setOnKeyPressed(keyBindClick);

    }

    public void showPlayerStats(){
        // if player stats is off
        if (!this.playerStatsToggle) {

            // turn it on, make and show it
            this.playerStatsToggle = true;
            removeByCell(2, 0);
            playerStats.getChildren().clear();
            playerStats.getChildren().add(healthBar.get());
            playerStats.getChildren().add(strengthBar.get());
            gridPane.add(playerStats, 0, 2, 1, 1);
        }
        // else
        else {
            //turn it off and close it
            this.playerStatsToggle = false;
            removeByCell(2, 0);
        }
    }

    /**
     * This method handles the event related to the
     * save button.
     */
    public void addSaveEvent() {
        saveButton.setOnAction(e -> {
            gridPane.requestFocus();
            SaveView saveView = new SaveView(this);
        });
    }

    /**
     * This method handles the event related to the
     * load button.
     */
    public void addLoadEvent() {
        loadButton.setOnAction(e -> {
            gridPane.requestFocus();
            LoadView loadView = new LoadView(this);
        });
    }


    /**
     * This method articulates Room Descriptions
     */
    public void articulateRoomDescription() {
        String musicFile;
        String adventureName = this.model.getDirectoryName();
        String roomName = this.model.getPlayer().getCurrentRoom().getRoomName();

        if (!this.model.getPlayer().getCurrentRoom().getVisited())
            musicFile = "./" + adventureName + "/sounds/" + roomName.toLowerCase() + "-long.mp3";
        else musicFile = "./" + adventureName + "/sounds/" + roomName.toLowerCase() + "-short.mp3";
        musicFile = musicFile.replace(" ", "-");

        Media sound = new Media(new File(musicFile).toURI().toString());

        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
        mediaPlaying = true;

    }

    /**
     * This method stops articulations
     * (useful when transitioning to a new room or loading a new game)
     */
    public void stopArticulation() {
        if (mediaPlaying) {
            mediaPlayer.stop(); //shush!
            mediaPlaying = false;
        }
    }

    public AdventureGame getModel() {
        return model;
    }

    /**
     * This is a gameOver method. It will close the game when the user dies.
     */
    public void gameOver(){
        PauseTransition pause  = new PauseTransition(Duration.seconds(10));
        pause.setOnFinished(actionEvent -> Platform.exit());
        pause.play();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                removeByCell(i,j);
            }
        }
        VBox gameEnd = new VBox(roomDescLabel);
        gameEnd.setAlignment(Pos.CENTER);
        gridPane.add(gameEnd, 1, 1);
        roomDescLabel.setText("YOU LOST ALL YOUR HEALTH! GAME OVER!");
        roomDescLabel.setAlignment(Pos.CENTER);
    }
}
