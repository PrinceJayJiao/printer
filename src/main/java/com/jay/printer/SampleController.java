package com.jay.printer;

import com.jay.dataparser.CompareToWord;
import com.jay.dataparser.Comparer;
import com.jay.dataparser.WriteToWord;
import com.jay.listener.SerialPortListener;
import com.jay.pdfparser.PDFParser;
import com.jay.weparser.WEParser;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SampleController {


    public Button saveButton;
    public TextField savePath;
    public Label printDeviceName;
    public Label stdDeviceName;
    public Button listenComportBtn;
    List<File> standardFiles;
    File printFile;

    public Label hintStandard;
    public Label hintPrint;
    public Button compareButton;
    @FXML
    private Button standardFileButton;

    @FXML
    private Button printFileButton;

    @FXML
    private Label standardFileLabel;

    @FXML
    private Label printFileLabel;

    private Stage stage;


    private List<StandardFileParseRes> stdResults;
    private List<StandardFileParseRes> prResults;
    Comparer.ComparisonResultMap comparisonResultMap;
    String type;
    String number;

    @FXML
    private void initialize() {
//        standardFileButton.getStyleClass().add("btn-primary");
//        printFileButton.getStyleClass().add("btn-primary");
////        standardFileLabel.setText("请选择标准值文件");
////        printFileLabel.setText("请选择打印文件");
//        compareButton.getStyleClass().add("btn-primary");
////        compareButton.isCenterShape();

    }

    @FXML
    private void selectStandardFile(ActionEvent event) {
        FileChooser fileChooser = getFileChooser();

        fileChooser.setTitle("选择标准值文件");
        standardFiles = fileChooser.showOpenMultipleDialog(stage);
        if (standardFiles != null && !standardFiles.isEmpty()) {
            // 处理选择的文件
            StringBuilder fileNames = new StringBuilder();
            for (File file : standardFiles) {
                fileNames.append(file.getName()).append(", ");
            }
            fileNames.delete(fileNames.length() - 2, fileNames.length()); // 移除最后的逗号和空格
            standardFileLabel.setText(fileNames.toString());
            standardFileButton.setText("重新选择");
            standardFileButton.getStyleClass().add("btn-success");

            //解析文件
            parseStandard(standardFiles);

        } else {
            standardFileLabel.setText("请选择标准值文件");
        }
    }

    private void parseStandard(List<File> standardFiles) {
        stdResults =  parse(standardFiles);
        String text = "";
        int i = 1;
        for(StandardFileParseRes res :stdResults){
            text += "设备"+i+": "+res.deviceName + " ";
            i++;
        }
        stdDeviceName.setText(text);
    }

    @FXML
    private void selectPrintFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("log文件", "*.log");
        fileChooser.getExtensionFilters().add(extFilter);

        fileChooser.setTitle("选择打印文件");
        printFile = fileChooser.showOpenDialog(stage);
        if (printFile != null) {
            // 处理选择的文件
            printFileLabel.setText(printFile.getName());
            printFileButton.setText("重新选择");
            printFileButton.getStyleClass().add("btn-success");
            System.out.println(printFile.getAbsolutePath());
            parsePrint(printFile);
        } else {
            printFileLabel.setText("请选择打印文件");
        }
    }

    private void parsePrint(File printFile) {
        String tmpPath = getClass().getResource("style.css").getPath();
        System.out.println(tmpPath);
        tmpPath = tmpPath.substring(0,tmpPath.indexOf('/'));
        System.out.println(tmpPath);
        tmpPath += "log/";
        tmpPath = "./log";
        String tmpLogPath = tmpPath+ "temp.log";
        String tmpDocPath = tmpPath+ "test.docx";
        //打印流解析
        WriteToWord wtw = new WriteToWord();
        System.out.println(printFile.getAbsolutePath());
        wtw.writeToWord(printFile.getAbsolutePath(),tmpLogPath,tmpDocPath);
        System.out.println(tmpDocPath);

        File file = new File(tmpDocPath);
        List<File> files = new ArrayList<>();
        files.add(file);
        prResults = parse(files);
        printDeviceName.setText(prResults.get(0).deviceName);

    }

    @FXML
    private void compare(ActionEvent event) throws IOException {
//        List<StandardFileParseRes> results = parse(standardFiles);
//        stdResults = parse(standardFiles);
        if(stdResults !=null&& prResults !=null){
            Map<String,Double> stdMap =  mergeParseRes(stdResults);
            Map<String,Double> prMap = mergeParseRes(prResults);
            comparisonResultMap   = Comparer.compareWithStandardValue(stdMap,prMap);
//            System.out.println(ComparisonResults);

            showCompareRes(comparisonResultMap.getComparisonResults(),prResults.get(0).deviceName);
        }else {
            System.out.println("have not parse yet");
        }


    }

    private Map<String, Double> mergeParseRes(List<StandardFileParseRes> stdResults) {
        Map<String,Double> res =new HashMap<>();
        for (StandardFileParseRes stdRes : stdResults){
            res.putAll(stdRes.map);
        }
        return res;
    }

    private List<StandardFileParseRes> parse(List<File> standardFiles) {
        List<StandardFileParseRes> results = new ArrayList<>();
        System.out.println(standardFiles.size());
        for(File f : standardFiles){
            String path = f.getAbsolutePath();
            StandardFileParseRes res = new StandardFileParseRes();
            if (path.endsWith(".doc")||path.endsWith(".docx")){
                WEParser.parse(path);
                res.deviceName = WEParser.getDeviceName();
                res.map = WEParser.getKeyValueMap();
                res.number = WEParser.getNumber();
                res.type = WEParser.getType();
//                System.out.println(res.type);
            }

            else {
                try {
                    PDFParser.parse(path);
                    res.deviceName = PDFParser.getDeviceName();
                    res.map = PDFParser.getKeyValueMap();
                    res.number = PDFParser.getNumber();
                    res.type = PDFParser.getType();
                }catch (Exception e){
                    System.out.println(e);
                }
            }
            if(type==null && !res.type.equals("Not Found Device Type ! ")) type = res.type;
            if(number==null &&res.number!=null&& !res.number.equals("Not Found Device Number ! ")) number = res.number;
            results.add(res);
        }
        return results;
    }

    private void showCompareRes(List<Comparer.ComparisonResult> ComparisonResults, String deviceName ){

        TableView<Comparer.ComparisonResult> tableView = new TableView<>();

        // 创建列，并设置属性和数据类型
        TableColumn<Comparer.ComparisonResult, String> standardKeyColumn = new TableColumn<>("标准文件项");
        standardKeyColumn.setCellValueFactory(new PropertyValueFactory<>("standardKey"));

        TableColumn<Comparer.ComparisonResult, String> inputKeyColumn = new TableColumn<>("打印流文件项");
//        inputKeyColumn.setCellValueFactory(new PropertyValueFactory<>("inputKey"));
        inputKeyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInputKey()));
        inputKeyColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("不存在");
                } else {
                    setText(item);
                }
            }
        });

        TableColumn<Comparer.ComparisonResult, Double> standardValueColumn = new TableColumn<>("标准值");
        standardValueColumn.setCellValueFactory(new PropertyValueFactory<>("standardValue"));

        TableColumn<Comparer.ComparisonResult, Double> inputValueColumn = new TableColumn<>("打印流值");
        inputValueColumn.setCellValueFactory(new PropertyValueFactory<>("inputValue"));

        TableColumn<Comparer.ComparisonResult, Boolean> isEqualColumn = new TableColumn<>("是否相等");
//        isEqualColumn.setCellValueFactory(new PropertyValueFactory<>("isEqual"));
        isEqualColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().getIsEqual()));
        isEqualColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item ? "是" : "否");
                }
            }
        });


        TableColumn<Comparer.ComparisonResult, Boolean> isExistColumn = new TableColumn<>("打印流中是否存在");
        isExistColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().getIsExist()));
        isExistColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item ? "是" : "否");
                }
            }
        });

        // 设置每个列的首选宽度
        standardKeyColumn.setPrefWidth(250);
        inputKeyColumn.setPrefWidth(250);
        standardValueColumn.setPrefWidth(200);
        inputValueColumn.setPrefWidth(200);
        isEqualColumn.setPrefWidth(150);
        isExistColumn.setPrefWidth(250);

        // 添加列到TableView
        tableView.getColumns().addAll(standardKeyColumn, inputKeyColumn, standardValueColumn,
                inputValueColumn, isEqualColumn, isExistColumn);

        // 创建数据列表
        ObservableList<Comparer.ComparisonResult> data = FXCollections.observableArrayList(

        );
        System.out.println(ComparisonResults);
        data.addAll(ComparisonResults);
        System.out.println(data);
        // 设置数据列表到TableView
        tableView.setItems(data);

        //调整tableview样式
        tableView.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
//        tableView.getStylesheets().add("table-view");

        // 创建一个对话框
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("比对结果");
        dialog.setHeaderText("设备名："+deviceName);

        VBox contentPane = new VBox(tableView);
        dialog.getDialogPane().setContent(contentPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        dialogPane.getStyleClass().add("my-dialog-pane");

        // 显示对话框
        dialog.showAndWait();

    }


    // 保存按钮响应函数
    public void save(ActionEvent event) {


        Dialog<ButtonType> dialog = new Dialog<>();

        HBox hBox = gethBox();
        dialog.getDialogPane().setContent(hBox);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.setTitle("保存比对结果");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        dialogPane.getStyleClass().add("my-dialog-pane");
        dialog.showAndWait();
//        dialog.setHeaderText("设备名："+deviceName);


    }



    private HBox gethBox() {
        Label label = new Label();
        label.setText("保存路径");
        TextField tf = new TextField();
        tf.setText("default path ");
        tf.prefWidth(800);
        Button openFileChooserBtn = new Button("浏览");
        Button saveBtn = new Button("保存");
//        btn.setOnAction(this::openFileChooser);
        DirectoryChooser dc = new DirectoryChooser();

        // 设置按钮点击事件，打开文件选择器并获取选择的文件路径
        openFileChooserBtn.setOnAction(e -> {
            // 显示文件选择器对话框
            File selectedFile = dc.showDialog(this.stage);
            if (selectedFile != null) {
                // 将选择的文件路径设置到文本输入框中
                String fileName = new Date().getTime()+".docx";
                String path = selectedFile.getAbsolutePath()+"/"+fileName;
                tf.setText(path);
            }
        });
        saveBtn.setOnAction(event ->{
            if(comparisonResultMap!=null)
                System.out.println(type);
            CompareToWord.compareToWord(comparisonResultMap,type,number,tf.getText());
        });
        HBox hBox = new HBox(label,tf,openFileChooserBtn,saveBtn);
        return hBox;
    }

    public void listenComPort(ActionEvent event) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SerialPortListener.listen();
            }
        });
        thread.start();
    }


    public static class MapEntry {
        private String key;
        private Double value;

        public MapEntry(String key, Double value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Double getValue() {
            return value;
        }
    }

    private FileChooser getFileChooser(){
        FileChooser fileChooser = new FileChooser();

        // 创建文件扩展名过滤器
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("文档文件 (*.doc, *.docx, *.pdf)", "*.doc", "*.docx", "*.pdf");

        // 添加文件扩展名过滤器到FileChooser
        fileChooser.getExtensionFilters().add(extFilter);

        return fileChooser;
    }
    class StandardFileParseRes{
        public String deviceName;
        public Map<String,Double> map;

        public String type;
        public String number;
    }
}