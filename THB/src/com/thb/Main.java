package com.thb;

public class Main {
    public static void main(String[] args) {
        Jsouper mJsouper = Jsouper.getInstance();
        System.out.println("isLogin : " + mJsouper.isLogin());

        /*if (mJsouper.isLogin())
            mJsouper.readShanghaiList();*/
        //mJsouper.readAndSaveDetail("https://26thb.com/forum.php?mod=viewthread&tid=78770&extra=page%3D2");
        //mJsouper.readAndSaveDetail("https://26thb.com/forum.php?mod=viewthread&tid=65394&extra=page%3D1");
        mJsouper.isLogin();
    }
}
