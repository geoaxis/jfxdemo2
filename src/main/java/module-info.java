module com.example.jfxdemo2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.jfxdemo2 to javafx.fxml;
    exports com.example.jfxdemo2;
}