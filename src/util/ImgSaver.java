package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ImgSaver {
    static Runtime rt;
    static ImgSaverImpl imgSaverImpl;
    static File comicDir;
    //static String savePath = ;

    public ImgSaver(File dir, ImgSaverImpl imgSaver) {
        imgSaverImpl = imgSaver;
        comicDir = dir;
        init();
    }

    static void init() {
        rt = Runtime.getRuntime();
        System.out.println(System.getProperty("user.dir"));
    }

    public void saveImage(String imgCod, String imgUrl) {
        /*try {
            rt.exec("cmd.exe /c cd ./imgs");
            rt.exec("ffmpeg -i " + imgUrl + " " + imgCod + ".jpeg");
            System.out.println("保存成功!");
            imgSaverImpl.imgDownCall(true);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("保存失败!");
            imgSaverImpl.imgDownCall(false);
        }*/

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

    public static void exit() {
        rt.exit(0);
    }
}
