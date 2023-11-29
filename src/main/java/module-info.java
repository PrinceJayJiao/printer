module com.jay.printer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires com.google.gson;
    requires org.apache.pdfbox;
    requires tabula;
    requires org.apache.poi.scratchpad;
    requires org.apache.poi.ooxml;
    requires purejavacomm;
    requires java.desktop;

    opens com.jay.printer to javafx.fxml;
    exports com.jay.printer;
    opens  com.jay.dataparser;
    exports com.jay.dataparser;


}