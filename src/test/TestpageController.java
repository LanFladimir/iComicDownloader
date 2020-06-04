package test;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class TestpageController {

    @FXML
    private Button test_errorimg;

    String errorImgSite = "";

    /**
     * 测试错误照片
     * @param event
     */
    @FXML
    void testErrorimg(ActionEvent event) {
        try {
            //save img
            URL url = new URL(errorImgSite);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream imgInp = connection.getInputStream();
            File img = new File( "error.jpeg");
            FileOutputStream imgOut = new FileOutputStream(img);

            byte[] bytes = new byte[1024 * 100];
            int readLength;
            while ((readLength = imgInp.read(bytes)) != -1) {
                imgOut.write(bytes, 0, readLength);
            }
            imgInp.close();
            imgOut.close();

            //转换
            //BitmapFactoty
            Image image;

        } catch (Exception e) {
            e.printStackTrace();
        }



    }

}
