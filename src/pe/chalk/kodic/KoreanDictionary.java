/**
 * A simple dictionary for Korean, powered by National Institute of the Korean Language
 * Copyright (C) 2015  ChalkPE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pe.chalk.kodic;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @author ChalkPE <amato0617@gmail.com>
 * @since 2015-06-04
 */
public class KoreanDictionary extends Application {
    private TextField input;

    private StackPane contents;
    private ListView<String> list;
    private ProgressIndicator progress;

    @Override
    public void start(Stage stage) throws Exception {
        //Init window

        stage.setMinWidth(250);
        stage.setMinHeight(500);

        stage.setWidth(450);
        stage.setHeight(700);

        stage.setTitle("KoreanDictionary");
        stage.setOnCloseRequest(value -> System.exit(0));

        //Create nodes

        EventHandler<ActionEvent> searchHandler = value -> this.search();

        input = new TextField();
        input.setOnAction(searchHandler);
        input.setPromptText("검색어를 입력하세요");
        HBox.setHgrow(input, Priority.ALWAYS);

        Button searchButton = new Button("검색");
        searchButton.setOnAction(searchHandler);

        list = new ListView<>();
        list.setOnMouseClicked(click -> {
            if(click.getClickCount() == 2){
                ClipboardContent content = new ClipboardContent();
                content.putString(list.getSelectionModel().getSelectedItem());

                Clipboard.getSystemClipboard().setContent(content);
            }
        });
        list.setItems(FXCollections.observableArrayList("A simple dictionary for Korean", "Powered by National Institute of the Korean Language", "", "Copyright (c) 2015 ChalkPE", "Licensed under GNU General Public License v3.0", "", "https://github.com/ChalkPE/KoreanDictionary"));

        progress = new ProgressIndicator();
        StackPane.setMargin(progress, new Insets(50));

        //Init layouts

        VBox top = new VBox(new HBox(6, input, searchButton));
        top.setPadding(new Insets(6));

        contents = new StackPane(list);

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(contents);

        Scene scene = new Scene(new Group());
        scene.setRoot(root);

        stage.setScene(scene);
        stage.show();
    }

    public void search(){
        String text = input.getText();
        if(text.length() == 0){
            list.setItems(FXCollections.observableArrayList("[오류] 찾을 단어를 입력해 주세요!"));
            return;
        }

        if(contents.getChildren().contains(progress)){
            list.setItems(FXCollections.observableArrayList("[오류] 이미 찾고 있는 중입니다!"));
            return;
        }

        contents.getChildren().add(progress);

        list.setItems(FXCollections.observableArrayList("[정보] 단어를 찾는 중입니다..."));
        new Thread(() -> {
            ObservableList<String> result = FXCollections.observableArrayList();
            try{
                result.addAll(KoreanFinder.getAllNounStartsWith(text));

                if(result.size() == 0){
                    result.add("[정보] 해당 단어로 시작하는 단어가 없습니다!");
                }
            }catch(IOException e){
                e.printStackTrace();
                result.add("[오류] 네트워크 오류가 발생했습니다! 인터넷 연결을 확인해 주세요!");
            }finally{
                Platform.runLater(() -> {
                    contents.getChildren().remove(progress);
                    list.setItems(result);
                });
            }
        }).start();
    }

    public static void main(String[] args){
        launch(args);
    }
}
