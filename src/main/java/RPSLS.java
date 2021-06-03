import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.util.HashMap;

public class RPSLS extends Application implements MyListener
{
    HashMap<String, Scene> sceneMap;
    Button joinButton, rockB, paperB, scissorB, lizardB, spockB, nameB;
    TextField portField, ipField, nameField;
    Label portL, ipL;
    ListView serverInfo;
    Client client;
    Stage stage;
    private final Image rockImg = new Image("/rock.jpg"), paperImg = new Image("/paper.jpg"), scissorImg = new Image("/scissors.jpg"), lizardImg = new Image("/lizard.jpg"), spockImg = new Image("/spock.jpg");
    boolean lobby = false, dropConnection = false;
    PauseTransition moveToLobby;

    public RPSLS getInstance()
    {
        return this;
    }

    public static void main(String[] args)
    {
        launch(args);
    }

    private void setButtons(boolean disabled)
    {
        rockB.setDisable(disabled);
        paperB.setDisable(disabled);
        scissorB.setDisable(disabled);
        lizardB.setDisable(disabled);
        spockB.setDisable(disabled);
    }

    private void clearStyles()
    {
        rockB.setStyle(null);
        paperB.setStyle(null);
        scissorB.setStyle(null);
        lizardB.setStyle(null);
        spockB.setStyle(null);
    }

    @Override
    public void dataReceived(GameInfo g)
    {
        //We are joining the lobby
        if (g.type == 0)
        {
            Platform.runLater(()->{
                serverInfo.getItems().clear();
                serverInfo.getItems().add(g.msg);
            });
            //setButtons(false);
            nameB.setDisable(true);
            nameField.setDisable(true);
            setButtons(true);
            clearStyles();
            lobby = true;
        }
        //Someone left the game
        if (g.type == 1)
        {
            if (lobby)
            {
                int index = serverInfo.getItems().indexOf(g.name);
                if (index > 0)
                    Platform.runLater(()->{
                        serverInfo.getItems().remove(index);
                    });
            }
        }
        //Challenged, enter game
        if (g.type == 2)
        {
            lobby = false;
            Platform.runLater(()->{
                serverInfo.getItems().clear();
                serverInfo.getItems().add(g.msg);
            });
            setButtons(false);
        }
        //Tried to challenge someone already ingame
        if (g.type == 3)
        {
            Platform.runLater(()->{
                new Alert(Alert.AlertType.ERROR, g.name + " is already in game.", ButtonType.OK).show();
            });
        }
        //Tried to challenge themselves
        if (g.type == 4)
        {
            Platform.runLater(()->{
                new Alert(Alert.AlertType.ERROR, "You cannot challenge yourself!", ButtonType.OK).show();
            });
        }
        //Server connection dropped
        if (g.type == 5)
        {
            setButtons(true);
            clearStyles();
            nameField.setDisable(true);
            nameB.setDisable(true);
            lobby = false;
            dropConnection = true;
        }
        //If we are receiving a win report
        if (g.type == 6)
        {
            moveToLobby.playFromStart();
        }
        //Enable Set name button
        if (g.type == 7)
        {
            nameB.setDisable(false);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        moveToLobby = new PauseTransition(Duration.seconds(5));
        moveToLobby.setOnFinished(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e)
            {
                if (dropConnection)
                    return;
                //Notify the server we need to go back to the lobby, send playerlist
                GameInfo g = new GameInfo(6);
                client.send(g);
            }
        });
        stage = primaryStage;
        // TODO Auto-generated method stub
        primaryStage.setTitle("RPLS Client");
        nameB = new Button("Set Name");
        nameB.setDisable(true);
        nameB.setDefaultButton(true);
        nameB.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e)
            {
                if (nameField.getText().equals(""))
                {
                    serverInfo.getItems().add("Name cannot be empty.");
                    serverInfo.scrollTo(serverInfo.getItems().size() - 1);
                    return;
                }
                GameInfo g = new GameInfo(-1);
                g.msg = nameField.getText();
                client.send(g);
            }
        });
        nameField = new TextField();
        portL = new Label("Port: ");
        ipL = new Label("IP: ");
        rockB = new Button("");
        paperB = new Button("");
        scissorB = new Button("");
        lizardB = new Button("");
        spockB = new Button("");
        rockB.setGraphic(new ImageView(rockImg));
        paperB.setGraphic(new ImageView(paperImg));
        scissorB.setGraphic(new ImageView(scissorImg));
        lizardB.setGraphic(new ImageView(lizardImg));
        spockB.setGraphic(new ImageView(spockImg));
        //Disable buttons until we get another player
        setButtons(true);
        //On each button action, send our choice to the server, disable buttons and show our selection
        rockB.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e)
            {
                GameInfo g = new GameInfo(8);
                g.choice = 0;
                g.msg = "rock";
                client.send(g);
                rockB.setStyle("-fx-background-color:red");
                setButtons(true);
            }
        });
        paperB.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e)
            {
                GameInfo g = new GameInfo(8);
                g.choice = 1;
                g.msg = "paper";
                client.send(g);
                paperB.setStyle("-fx-background-color:red");
                setButtons(true);
            }
        });
        scissorB.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e)
            {
                GameInfo g = new GameInfo(8);
                g.choice = 2;
                g.msg = "scissors";
                client.send(g);
                scissorB.setStyle("-fx-background-color:red");
                setButtons(true);
            }
        });
        lizardB.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e)
            {
                GameInfo g = new GameInfo(8);
                g.choice = 3;
                g.msg = "lizard";
                client.send(g);
                lizardB.setStyle("-fx-background-color:red");
                setButtons(true);
            }
        });
        spockB.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e)
            {
                GameInfo g = new GameInfo(8);
                g.choice = 4;
                g.msg = "spock";
                client.send(g);
                spockB.setStyle("-fx-background-color:red");
                setButtons(true);
            }
        });
        portField = new TextField();
        ipField = new TextField();
        HBox pBox = new HBox(portL, portField);
        pBox.setHgrow(portField, Priority.ALWAYS);
        HBox iBox = new HBox(ipL, ipField);
        iBox.setHgrow(ipField, Priority.ALWAYS);
        joinButton = new Button("Connect");
        joinButton.setDefaultButton(true);
        joinButton.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e)
            {
                //Is our port an integer value?
                try
                {
                    int port = Integer.parseInt(portField.getText());
                    primaryStage.setScene(sceneMap.get("game"));
                    primaryStage.sizeToScene();
                    client = new Client(data->
                    {
                        Platform.runLater(()->{serverInfo.getItems().add(data.toString());
                            serverInfo.scrollTo(serverInfo.getItems().size() - 1);
                        });}, ipField.getText(), port);
                    client.start();
                    client.setListener(getInstance());
                }
                catch (Exception ex)
                {
                    new Alert(Alert.AlertType.ERROR, "Port must be an integer number.", ButtonType.OK).show();
                }
            }
        });
        HBox bBox = new HBox(joinButton);
        bBox.setAlignment(Pos.CENTER);
        Scene startScene = new Scene(new VBox(30, pBox, iBox, bBox), 400, 200);
        HBox p2Box = new HBox(10, new Label("Name: "), nameField, nameB);
        p2Box.setHgrow(nameField, Priority.ALWAYS);
        p2Box.setAlignment(Pos.CENTER);
        serverInfo = new ListView();

        //Code to assign images to each item in listview if it contains a move report from the server
        serverInfo.setCellFactory(param -> new ListCell<String>(){
            private ImageView imageView = new ImageView();
            @Override
            public void updateItem(String name, boolean empty)
            {
                super.updateItem(name, empty);
                if (empty || name == null)
                {
                    setText("");
                    setGraphic(null);
                }
                else
                {
                    if (name.contains("rock"))
                        imageView.setImage(rockImg);
                    else if (name.contains("paper"))
                        imageView.setImage(paperImg);
                    else if (name.contains("scissors"))
                        imageView.setImage(scissorImg);
                    else if (name.contains("lizard"))
                        imageView.setImage(lizardImg);
                    else if (name.contains("spock"))
                        imageView.setImage(spockImg);
                    else
                        imageView.setImage(null);
                    setText(name);
                    setGraphic(imageView);
                }
            }
        });

        //When an item is clicked, it send the player's name to the server and starts a new game.
        serverInfo.setOnMouseClicked(e->{
            //Don't listen if we aren't in the lobby or the selected index is invalid
            if (!lobby || serverInfo.getSelectionModel().getSelectedIndex() <= 0)
                return;
            String op = serverInfo.getSelectionModel().getSelectedItem().toString();
            GameInfo s = new GameInfo(-1);
            s.msg = op;
            client.send(s);
        });
        serverInfo.setPrefHeight(125);
        HBox b2Box = new HBox(15, rockB, paperB, scissorB, lizardB, spockB);
        b2Box.setAlignment(Pos.CENTER);
        VBox myBox = new VBox(10, p2Box, serverInfo, b2Box);
        Scene gameScene = new Scene(myBox, 400, 200);
        sceneMap = new HashMap<String, Scene>();
        sceneMap.put("start", startScene);
        sceneMap.put("game", gameScene);
        primaryStage.setScene(sceneMap.get("start"));
        primaryStage.setHeight(200);
        primaryStage.setWidth(400);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
            public void handle(WindowEvent e)
            {
                if (client != null)
                    client.kill();
                Platform.exit();
            }
        });
        primaryStage.show();
    }
}
