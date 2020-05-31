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

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;

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
    private TextField view_website;
    @FXML
    private TextArea view_infos;
    @FXML
    private Button view_check;
    @FXML
    private Button view_download;

    private String mWebSite;
    private HashMap<String, String> mComicMap = new HashMap<>();//单卷漫画图片
    private ArrayList<String> mChapterList = new ArrayList<>();//漫画全集列表
    private String mComicName;
    private String mComicSetpName;
    private boolean isComicPage = false;//是否是漫画单卷页
    private boolean downloading = false;//是否在下载中
    private StringBuilder mInfo = new StringBuilder();

    @FXML
    void check(ActionEvent event) {
        //清空 textarea
        view_infos.clear();
        mInfo = new StringBuilder();
        mComicMap.clear();
        mWebSite = view_website.getText();
        //校验网址
        if (mWebSite.length() != 0) {
            try {
                if (mWebSite.contains("article_list")) {
                    setText("检测为：漫画章节页");
                    mInfo.append("检测为：漫画章节页");
                    String comicInfo = mWebSite.split("omyschool.com/")[1];
                    String[] comcieInfos = comicInfo.split("/");
                    mComicName = URLDecoder.decode(comcieInfos[2], "utf-8");
                    setText("漫画: " + mComicName + "\n" + "全集漫画下载，将下载最新两章(测试阶段)");
                    isComicPage = false;
                } else if (mWebSite.contains("article_detail")) {
                    setText("检测为：漫画单卷页");
                    String comicInfo = mWebSite.split("omyschool.com/")[1];
                    String[] comcieInfos = comicInfo.split("/");
                    mComicName = URLDecoder.decode(comcieInfos[3], "utf-8");
                    mComicSetpName = URLDecoder.decode(comcieInfos[4], "utf-8");
                    setText("漫画: " + mComicName + "\n" + "单卷: " + mComicSetpName);
                    isComicPage = true;
                } else {
                    setText("error wensite");
                }
            } catch (UnsupportedEncodingException e) {
                setText("漫画信息解析异常: " + e.getMessage());
            }

            new Thread(() -> {
                if (isComicPage) {
                    try {
                        int imgCount = 0;
                        Document mDoc = Jsoup.connect(mWebSite)
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
                    }
                } else {
                    try {
                        Document chapterDoc = Jsoup.connect(mWebSite)
                                .userAgent("Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)")
                                .timeout(1000 * 15)
                                .get();
                        Elements chapters = chapterDoc.select("div.chapter");
                        for (Element chapter : chapters) {
                            Element a = chapter.select("a").first();
                            System.out.println(a.attr("href"));
                            System.out.println(a.text());
                        }
                    } catch (SocketTimeoutException e) {
                        setText("读取超时：" + e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @FXML
    void download() {
        if (isComicPage) {
            downloadComic();
        } else {
            downloadChapter();
        }
    }

    /**
     * 下载单卷
     */
    private void downloadComic() {
        if (downloading)
            setText("正在下载，勿重复点击");
        if (mComicMap.size() == 0) {
            setText("先解析以获取漫画图片数据");
            downloading = false;
        } else {
            downloading = true;
            setText("开始下载-------------");
            FileSystemView fsv = FileSystemView.getFileSystemView();
            File homeDirectory = fsv.getHomeDirectory();
            File comicDir = new File(homeDirectory + File.separator + mComicName + File.separator + mComicSetpName);
            boolean newDir = comicDir.mkdirs();
            System.out.println("创建文件夹( " + comicDir + " )：" + newDir);

            setText("目录地址(暂存桌面)-------------");
            setText(comicDir.getAbsolutePath());

            ImgSaverImpl imgImpl = (imgCod, boo) -> setText(imgCod + (boo ? "下载成功" : "下载失败"));
            ImgSaver imgSaver = new ImgSaver(comicDir, imgImpl);
            new Thread(() -> {
                for (String s : mComicMap.keySet()) {
                    imgSaver.saveImage(s, mComicMap.get(s));
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
    }

    /**
     * 下载整部漫画
     */
    private void downloadChapter() {
        setText("working");
    }

    private void setText(String text) {
        mInfo.append("\n").append(text);
        Platform.runLater(() -> view_infos.setText(mInfo.toString()));
    }
}
