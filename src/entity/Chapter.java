package entity;

/**
 * 章节信息
 */
public class Chapter {
    String website;
    String chapter;

    public Chapter(String website, String chapter) {
        this.website = website;
        this.chapter = chapter;
    }

    public Chapter() {
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "website='" + website + '\'' +
                ", chapter='" + chapter + '\'' +
                '}';
    }
}
