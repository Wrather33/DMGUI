package com.example.dmgui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class GMGUI extends Application {
    public Commands status;
    public static void main (String[]args){
        launch();
    }
    @Override
    public void start(Stage stage) throws IOException {
        Label label = new Label("Download Manager");
        label.setFont(new Font("Times New Roman", 30));
        GridPane gridPane = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setHgrow(Priority.ALWAYS);
        columnConstraints.setHalignment(HPos.CENTER);
        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        ColumnConstraints columnConstraints2 = new ColumnConstraints();
        gridPane.getColumnConstraints().addAll(columnConstraints, columnConstraints1);
        RowConstraints rowConstraints = new RowConstraints();
        RowConstraints rowConstraints1 = new RowConstraints();
        gridPane.getRowConstraints().addAll(rowConstraints, rowConstraints1);
        Button start = new Button("Add");
        start.setFont(new Font("Times New Roman", 30));
        gridPane.add(label, 0, 0);
        gridPane.add(start, 0, 1);
        start.setOnAction(actionEvent -> {
            GridPane child = new GridPane();
            ColumnConstraints cl1 = new ColumnConstraints();
            cl1.setHgrow(Priority.ALWAYS);
            ColumnConstraints cl2 = new ColumnConstraints();
            ColumnConstraints cl3 = new ColumnConstraints();
            ColumnConstraints cl4 = new ColumnConstraints();
            cl4.setHgrow(Priority.ALWAYS);
            cl4.setHalignment(HPos.CENTER);
            cl3.setHalignment(HPos.CENTER);
            cl2.setHalignment(HPos.CENTER);
            cl1.setHalignment(HPos.CENTER);
            cl3.setHgrow(Priority.ALWAYS);
            cl2.setHgrow(Priority.ALWAYS);
            ColumnConstraints cl5 = new ColumnConstraints();
            cl5.setHalignment(HPos.CENTER);
            cl5.setHgrow(Priority.ALWAYS);
            ColumnConstraints cl6 = new ColumnConstraints();
            cl6.setHalignment(HPos.CENTER);
            cl6.setHgrow(Priority.ALWAYS);
            TextField url = new TextField();

            url.setPromptText("Url");
            TextField path = new TextField();
            path.setPromptText("Path");
            VBox vBox = new VBox(url, path);
            Button download = new Button("Download");
            Label result = new Label("");
            Label size = new Label("");
            Label form = new Label("");
            Button remove = new Button("Remove");
            VBox vBox1 = new VBox(download, remove);
            child.getColumnConstraints().addAll(cl1, cl2, cl3, cl4, cl5, cl6);
            child.add(vBox, 0, 0);
            child.add(vBox1, 1, 0);
            child.add(form, 2, 0);
            child.add(size, 3, 0);
            child.add(result, 4, 0);
            remove.setOnAction(actionEvent1 -> {
                gridPane.getChildren().remove(child);
            });
            gridPane.add(child, 0, gridPane.getRowCount());

            download.setOnAction(actionEvent1 -> {
                result.setText("");
                size.setText("");
                form.setText("");
                child.getChildren().remove(vBox1);
                Button fire = new Button("Continue");
                Button stop = new Button("Stop");
                Button delete = new Button("Interrupt");
                VBox vBox2 = new VBox(delete, stop);
                child.add(vBox2, 1, 0);
                AtomicBoolean flag = new AtomicBoolean(true);
                String URL = url.getText();
                String PATH  = path.getText();
                url.clear();
                path.clear();
                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {

                        try {
                                URL address = new URL(URL);
                                URLConnection connection = address.openConnection();
                                int length = connection.getContentLength();
                                InputStream inputStream = address.openStream();
                                String fileName = new File(address.getFile()).getName();
                                String type = Files.probeContentType(Path.of(address.getFile()));
                                if (type == null) {
                                    throw new FileNotFoundException();
                                }
                                String pathName = PATH;
                                if (!Files.exists(Path.of(pathName))) {
                                    throw new InvalidPathException(pathName, "Директории не существует");
                                }
                            Platform.runLater(()->{
                                size.setText("Size:\n "+String.valueOf(length/1024)+" Kb");
                                form.setText("Type: "+type);
                            });
                                FileOutputStream fileOutputStream = new FileOutputStream(pathName + "\\\\" + fileName);
                                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bufferedInputStream.readAllBytes());
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                                while (true) {
                                    if (isCancelled() && Files.exists(Path.of(pathName + "\\\\" + fileName))) {
                                        bufferedInputStream.close();
                                        bufferedOutputStream.close();
                                        byteArrayInputStream.close();
                                        byteArrayOutputStream.close();
                                        Files.delete(Path.of(pathName + "\\\\" + fileName));
                                        updateMessage(String.format("%s interrupted", fileName));
                                        return null;
                                    }
                                   else if(flag.get()){
                                        int c = byteArrayInputStream.read();
                                        if (c == -1) {
                                            break;
                                        } else {
                                            byteArrayOutputStream.write(c);
                                            updateMessage(((byteArrayOutputStream.size()/1024) * 100 / (length/1024)) + "%\n" +
                                                    String.format("[%d Kb/%d Kb]", byteArrayOutputStream.size()/1024, length/1024));
                                        }
                                    }

                                }
                                    bufferedOutputStream.write(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size());
                                    updateMessage(String.format("%s downloaded", fileName));
                                    bufferedInputStream.close();
                                    bufferedOutputStream.close();
                                    byteArrayInputStream.close();
                                    byteArrayOutputStream.close();
                            } catch (FileNotFoundException e) {
                                updateMessage("Файл не найден");
                            } catch (MalformedURLException e) {
                                updateMessage("Невердный адрес");
                            } catch (InvalidPathException e) {
                                updateMessage("Неверный путь");
                            } catch (IOException e) {
                                updateMessage("Ошибка ввода");
                            }

                        return null;
                        }

                };
                task.messageProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                        result.setText(t1);
                    }
                });

                Thread thread = new Thread(task);
                thread.setDaemon(true);
                fire.setOnAction(actionEvent3 -> {
                            vBox2.getChildren().remove(fire);
                            vBox2.getChildren().add(stop);
                    flag.set(true);
                        }
                );
                stop.setOnAction(actionEvent2 -> {
                            vBox2.getChildren().remove(stop);
                            vBox2.getChildren().add(fire);
                            flag.set(false);

                        });

                delete.setOnAction(actionEvent4 -> {
                    task.cancel();
                });
                task.setOnCancelled(EventHandler->{
                    child.getChildren().remove(vBox2);
                    child.add(vBox1, 1, 0);
                });
                task.setOnFailed(EventHandler->{
                    child.getChildren().remove(vBox2);
                    child.add(vBox1, 1, 0);
                });
                task.setOnSucceeded(EventHandler->{
                    child.getChildren().remove(vBox2);
                    child.add(vBox1, 1, 0);
                });
                thread.start();
            });
        });
        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        stage.setScene(new Scene(scrollPane, 640, 480));
        stage.show();

    }

}

