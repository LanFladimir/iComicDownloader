package com.thb;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Jsouper {
    private static Jsouper mJsouper;
    private static SQLiteJDBC sqLiteJDBC;
    static String TAG = "Jsouper";
    private static int page = 0;
    private static Map<String, String> cookies = new HashMap<>();
    private static String userAgent = "5.0 (Linux; Android 7.0; MIX Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/59.0.3071.125 Mobile Safari/537.36 MicroMessenger/6.6.1.1220(0x26060135) NetType/4G Language/zh_CN";
    private boolean isEndPage = false;

    static synchronized Jsouper getInstance() {
        if (mJsouper == null) {
            mJsouper = new Jsouper();
            cookies.put("Cy9a_2132_nofavfid", "1");
            cookies.put("Cy9a_2132_union_reguser", "1");
            cookies.put("Cy9a_2132_lastactivity", "1");
            cookies.put("Cy9a_2132_lastact", "1619059697%09index.php%09");
            cookies.put("Cy9a_2132_checkfollow", "1");
            cookies.put("Cy9a_2132_lastcheckfeed", "46133%7C1619059694");
            cookies.put("Cy9a_2132_sendmail", "1");
            cookies.put("Cy9a_2132_it618_union_tuiuid", "1");
            cookies.put("Cy9a_2132_lastvisit", "1619056064");
            cookies.put("Cy9a_2132_saltkey", "gR6lctCr");
            cookies.put("Cy9a_2132_sid", "0");
            cookies.put("Cy9a_2132_auth", "f89b%2BgQz5zzdRcPA5X2bH5ahMVFDEl9BIMDlSqw41mD5vXy6xLIWBNaz6UelJpEEYzoQ%2Fy9cLDE4VPX8ODJiRuq9Lg");
            cookies.put("Cy9a_2132_ulastactivity", "1619059694%7C0");
            cookies.put("Cy9a_2132_lip", "47.242.151.226%2C1619059694");

            sqLiteJDBC = SQLiteJDBC.getInstance();
        }
        return mJsouper;
    }

    private String indexUrl = "https://26thb.com/";

    /**
     * 是否已登录
     */
    boolean isLogin() {
        try {
            Document doc = Jsoup.connect(indexUrl)
                    .timeout(1000 * 10)
                    .cookies(cookies)
                    .userAgent(userAgent)
                    .get();
            String html = doc.html();
            File indexHtmlFile = new File(".\\THB\\html\\index.html");
            FileWriter writer = new FileWriter(indexHtmlFile);
            writer.write(html);
            writer.flush();
            writer.close();

            new Thread(runnable).start();
            return !doc.html().contains("登录");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Runnable runnable = () -> {
        while (!isEndPage) {
            page++;
            readShanghaiList();
            try {
                Thread.sleep(1000 * 20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private void readShanghaiList() {
        String shUrl = "https://26thb.com/forum.php?mod=forumdisplay&fid=36&page=" + page;
        try {
            Document doc = Jsoup.connect(shUrl)
                    .timeout(1000 * 10)
                    .cookies(cookies)
                    .userAgent(userAgent)
                    .get();
            String html = doc.html();
            /*File shanghaiHtmlFile = new File(".\\THB\\html\\shanghai.html");
            FileWriter writer = new FileWriter(shanghaiHtmlFile);
            writer.write(html);
            writer.flush();
            writer.close();*/
            if (!html.contains("下一页")) isEndPage = true;

            Element listParent = doc.getElementsByClass("threadlist cl bzbt1").first();
            Elements girlList_li = listParent.select("li");
            for (Element element : girlList_li) {
                String lianjie = element.select("a").first().attr("href");
                String title = element.select("a >span").first().text();
                String area = "";

                System.out.println("lianjie = " + lianjie);
                System.out.println("title = " + title);
                System.out.println("area = " + area);
                readAndSaveDetail(lianjie);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*相信信息*/
    void readAndSaveDetail(String url) {
        try {
            Document doc = Jsoup.connect(indexUrl + url)
                    .timeout(1000 * 10)
                    .cookies(cookies)
                    .userAgent(userAgent)
                    .get();
            String tid = "";
            for (String s : url.split("&"))
                if (s.contains("tid"))
                    tid = s.split("=")[1];

            String html = doc.html();
            //File shanghaiHtmlFile = new File(".\\THB\\html\\detail" + tid + ".html");
            File shanghaiHtmlFile = new File(".\\THB\\html\\detail.html");
            FileWriter writer = new FileWriter(shanghaiHtmlFile);
            writer.write(html);
            writer.flush();
            writer.close();
            Element detailElement = doc.getElementsByClass("message bz_message").first();
            Elements info_tr = detailElement.select("tr");


            String title = doc.title();
            Elements areaS = doc.select("a.z");
            String province;
            try {
                province = areaS.get(0).text();
            } catch (Exception e) {
                province = "";
            }
            String area;
            try {
                area = areaS.get(1).text();
            } catch (Exception e) {
                area = "";
            }
            String age;
            try {
                age = info_tr.get(2).select("td").text();
            } catch (Exception e) {
                age = "";
            }
            String price;
            try {
                price = info_tr.get(6).select("td").text();
            } catch (Exception e) {
                price = "";
            }
            String address;
            try {
                address = info_tr.get(7).select("td").text();
            } catch (Exception e) {
                address = "";
            }
            String wxqq;
            try {
                wxqq = info_tr.get(8).select("td").text();
            } catch (Exception e) {
                wxqq = "";
            }
            Element fuckdetail;
            String guocheng;
            Elements imgs;
            List<String> imgList = new ArrayList<>();
            try {
                fuckdetail = doc.getElementsByClass("bz_message_table").first();
                guocheng = fuckdetail.text();
                imgs = fuckdetail.getElementsByTag("img");
                for (Element img : imgs) {
                    imgList.add(indexUrl + img.attr("src"));
                }
            } catch (Exception e) {
                guocheng = "";
            }

            sqLiteJDBC.doSQl("INSERT INTO girls (girlid,price,age,title,area,address,wxqq,guocheng) VALUES ('" + tid + "', '" + price + "', '" + age + "', '" + title + "', '" + area + "', '" + address + "', '" + wxqq + "', '" + guocheng + "' );");
            for (String imgurl : imgList) {
                sqLiteJDBC.doSQl("INSERT INTO imgs (imgurl,girlid) VALUES ('" + imgurl + "', '" + tid + "');");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
