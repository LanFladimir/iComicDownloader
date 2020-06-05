package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImgSaver {
    static ImgSaverImpl imgSaverImpl;

    public ImgSaver(ImgSaverImpl imgSaver) {
        imgSaverImpl = imgSaver;
    }

    public void saveImage(File comicDir, String imgCod, String imgUrl) {
        try {
            URL url = new URL(imgUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream imgInp = connection.getInputStream();
            File img = new File(comicDir.getAbsolutePath() + File.separator + imgCod + ".jpeg");
            FileOutputStream imgOut = new FileOutputStream(img);

            byte[] bytes = new byte[1024 * 100];
            int readLength;
            while ((readLength = imgInp.read(bytes)) != -1) {
                imgOut.write(bytes, 0, readLength);
            }
            imgInp.close();
            imgOut.close();

            imgSaverImpl.imgDownCall(imgCod, true);
        } catch (Exception e) {
            imgSaverImpl.imgDownCall(imgCod, false);

            e.printStackTrace();
        }
    }

}
