package main;

import com.lowagie.text.BadElementException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;

import entity.Chapter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import util.ImgSaver;
import util.ImgSaverImpl;

public class Controller {
    @FXML
    private TextField view_maxstep;
    @FXML
    private TextField view_website;
    @FXML
    private TextArea view_infos;
    @FXML
    private Button view_check;
    @FXML
    private Button view_download;

    private String mWebSite;
    private HashMap<String, String> mComicMap = new HashMap<>();//单卷漫画图片
    private ArrayList<Chapter> mChapterList = new ArrayList<>();//漫画全集列表
    private ArrayList<Chapter> mErrorList = new ArrayList<>();//漫画全集列表--下载失败
    private String mComicName;
    private String mComicSetpName;
    private boolean isComicPage = false;//是否是漫画单卷页
    private boolean downloading = false;//是否在下载中
    private StringBuilder mInfo = new StringBuilder();


    @FXML
    void check(ActionEvent event) {
        //清空 textarea
        view_infos.clear();
        mChapterList.clear();
        mErrorList.clear();
        mInfo = new StringBuilder();
        mComicMap.clear();
        mWebSite = view_website.getText();
        //校验网址&解析
        if (mWebSite.length() != 0) {
            //判断全集/单卷
            try {
                if (mWebSite.contains("article_list")) {
                    setText("检测为：漫画章节页");
                    mInfo.append("检测为：漫画章节页");
                    String comicInfo = mWebSite.split("omyschool.com/")[1];
                    String[] comcieInfos = comicInfo.split("/");
                    mComicName = URLDecoder.decode(comcieInfos[2], "utf-8");
                    setText("漫画: " + mComicName);
                    isComicPage = false;
                    view_maxstep.setVisible(true);
                } else if (mWebSite.contains("article_detail")) {
                    setText("检测为：漫画单卷页");
                    String comicInfo = mWebSite.split("omyschool.com/")[1];
                    String[] comcieInfos = comicInfo.split("/");
                    mComicName = URLDecoder.decode(comcieInfos[3], "utf-8");
                    mComicSetpName = URLDecoder.decode(comcieInfos[4], "utf-8");
                    setText("漫画: " + mComicName + "\n" + "单卷: " + mComicSetpName);
                    isComicPage = true;
                    view_maxstep.setVisible(false);
                } else {
                    setText("error wensite");
                }
            } catch (UnsupportedEncodingException e) {
                setText("漫画信息解析异常: " + e.getMessage());
            }

            //解析页面&获取单卷所有照片map/全集数据
            new Thread(() -> {
                if (isComicPage) {//单卷页
                    mComicMap = getmComicMap(new Chapter(mWebSite, ""));
                } else {//总章
                    try {
                        Document chapterDoc = Jsoup.connect(mWebSite)
                                .userAgent("Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)")
                                .timeout(1000 * 15)
                                .get();
                        Elements chapters = chapterDoc.select("div.chapter");
                        for (Element chapter : chapters) {
                            Element a = chapter.select("a").first();
                            mChapterList.add(new Chapter(a.attr("href"), a.text()));
                        }
                        setText("章节内容读取完成");
                    } catch (SocketTimeoutException e) {
                        setText("章节内容读取超时：" + e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                        setText("章节内容读取异常：" + e.getMessage());
                    }
                }
            }).start();
        }
    }

    @FXML
    void download() {
        if (isComicPage) {
            if (downloading)
                setText("正在下载，勿重复点击");
            if (mComicMap.size() == 0) {
                setText("先解析以获取漫画图片数据");
                downloading = false;
            } else {
                downloading = true;
                downloadComic(mComicMap, mComicName, mComicSetpName);
            }
        } else {
            if (mChapterList.size() == 0)
                setText("尚未读取到章节列表");
            else downloadChapter();
        }
    }

    /**
     * 下载单卷
     */
    private void downloadComic(HashMap<String, String> mComicMap, String mComicName, String mComicSetpName) {
        setText("开始下载-------------");
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File homeDirectory = fsv.getHomeDirectory();
        File comicDir = new File(homeDirectory + File.separator + mComicName + File.separator + mComicSetpName);
        boolean newDir = comicDir.mkdirs();
        System.out.println("创建文件夹( " + comicDir + " )：" + newDir);

        setText("目录地址(暂存桌面)-------------");
        setText(comicDir.getAbsolutePath());

        ImgSaverImpl imgImpl = (imgCod, boo) -> setText(imgCod + (boo ? "下载成功" : "下载失败"));
        ImgSaver imgSaver = new ImgSaver(imgImpl);
        new Thread(() -> {
            for (String s : mComicMap.keySet()) {
                imgSaver.saveImage(comicDir, s, mComicMap.get(s));
            }
            setText("照片下载完成!\n合成PDF文件");
            try {
                String pdfFilePath = comicDir + File.separator + mComicSetpName + ".pdf";
                FileOutputStream fos = new FileOutputStream(pdfFilePath);

                com.lowagie.text.Document doc = new com.lowagie.text.Document(null, 0, 0, 0, 0);
                PdfWriter.getInstance(doc, fos);
                BufferedImage img;
                Image image;

                File[] imgFiles = comicDir.listFiles();
                assert imgFiles != null;
                for (File imgFile : imgFiles) {
                    if (imgFile.getName().endsWith(".bmp")
                            || imgFile.getName().endsWith(".wbmp")
                            || imgFile.getName().endsWith(".gif")
                            || imgFile.getName().endsWith(".PNG")
                            || imgFile.getName().endsWith(".png")
                            || imgFile.getName().endsWith(".JPG")
                            || imgFile.getName().endsWith(".jpg")
                            || imgFile.getName().endsWith(".JPEG")
                            || imgFile.getName().endsWith(".jpeg")
                            || imgFile.getName().endsWith(".WBMP")) {
                        System.out.println("READ FILE: " + imgFile.getName());
                        try {
                            img = ImageIO.read(imgFile);
                            if (img == null) {
                                System.out.println("img NULL");
                            }
                            assert img != null;
                            Rectangle rectangle = new Rectangle(img.getWidth(), img.getHeight());
                            doc.setPageSize(rectangle);
                            image = Image.getInstance(imgFile.getAbsolutePath());
                            doc.open();
                            doc.add(image);
                        } catch (Exception e) {
                            setText("跳过异常图片: " + imgFile.getName());
                        }
                    }
                }
                doc.close();
                setText("PDF文件 创建完成");
                mComicMap.clear();
                view_website.setText("");
            } catch (IOException e) {
                e.printStackTrace();
                downloading = false;
                setText("PDF文件合成异常(IOException)：" + e.getMessage());
            } catch (BadElementException e) {
                e.printStackTrace();
                downloading = false;
                setText("PDF文件合成异常(BadElementException)：" + e.getMessage());
            } catch (DocumentException e) {
                e.printStackTrace();
                downloading = false;
                setText("PDF文件合成异常(DocumentException)：" + e.getMessage());
            } finally {
                downloading = false;
            }
        }).start();
    }

    /**
     * 下载整部漫画
     */
    private void downloadChapter() {
        int maxStep;//全集下载最后集数
        try {
            maxStep = Integer.valueOf(view_maxstep.getText());
            setText("将开始下载最新" + maxStep + "集漫画");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            setText("集数输入异常，默认下载最新2章");
            maxStep = 2;
        }

        //Collections.reverse(mChapterList);//倒序
        List<Chapter> subList = mChapterList.subList(0, maxStep);
        System.out.println(subList.size());

        for (Chapter chapter : subList) {
            if (maxStep > 0) {
                System.out.println(mChapterList.toString());
                new Thread(() -> downloadComic(
                        getmComicMap(chapter)
                        , mComicName
                        , chapter.getChapter())).start();
                maxStep--;
            }
        }

        if (mErrorList.size() > 0) {
            mChapterList = mErrorList;
            mErrorList.clear();
            for (Chapter chapter : mChapterList) {
                if (maxStep > 0) {
                    //System.out.println(mChapterList.toString());
                    new Thread(() ->
                            downloadComic(
                                    getmComicMap(chapter)
                                    , mComicName
                                    , chapter.getChapter())
                    ).start();
                    maxStep--;
                }
            }
        }
    }


    /**
     * 获取单卷所有漫画数据
     */
    private HashMap<String, String> getmComicMap(Chapter chapter) {
        if (!chapter.getWebsite().contains("omyschool"))
            chapter.setWebsite("http://omyschool.com/" + chapter.getWebsite());

        HashMap<String, String> mComicMap = new HashMap<>();//单卷漫画图片
        try {
            int imgCount = 0;
            Document mDoc = Jsoup.connect(chapter.getWebsite())
                    .userAgent("Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)")
                    .timeout(1000 * 15)
                    .get();
            Element imgDev = mDoc.select("div#imgs").first();
            Elements imgs = imgDev.select("div");
            setText("搜索漫画照片...");
            imgs.remove(0);//包含第一个父类div
            for (Element img : imgs) {
                //System.out.println(img.outerHtml());
                Elements amp = img.select("amp-img");
                if (amp.size() >= 1) {
                    Element amp_img = amp.first();
                    String dataId = img.attr("data-id");
                    if (!dataId.equals("")) {
                        System.out.print("图片id：" + dataId);
                        System.out.println("    ------    图片地址：" + amp_img.attr("src"));
                        imgCount++;
                        mComicMap.put(dataId, amp_img.attr("src"));
                    }
                }
            }
            setText("共可取出" + imgCount + "张图片");
        } catch (Exception e) {
            e.printStackTrace();
            setText("读取异常: " + e.getClass().getName());
            mErrorList.add(chapter);
        }
        return mComicMap;
    }

    private void setText(String text) {
        mInfo.append("\n").append(text);
        Platform.runLater(() -> {
            view_infos.setText(mInfo.toString());
            view_infos.selectEnd();//保持最底部
            view_infos.deselect();
        });
    }
}
