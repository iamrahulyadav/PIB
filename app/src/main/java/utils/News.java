package utils;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by bunny on 29/09/17.
 */

public class News implements Serializable {

    private String title, description, link, pubDate, newsID ;
    private boolean read, pushNotification,bookMark;
    private int newsType=0;



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getNewsID() {
        return newsID;
    }

    public void setNewsID(String newsID) {
        this.newsID = newsID;
    }

    public boolean isPushNotification() {
        return pushNotification;
    }

    public void setPushNotification(boolean pushNotification) {
        this.pushNotification = pushNotification;
    }

    public boolean isBookMark() {
        return bookMark;
    }

    public void setBookMark(boolean bookMark) {
        this.bookMark = bookMark;
    }

    public int getNewsType() {
        return newsType;
    }

    public void setNewsType(int newsType) {
        this.newsType = newsType;
    }

    public void rectifyNewsLink(){

        Uri uri = Uri.parse(link);
        String relID = uri.getQueryParameter("PRID");

        if (relID!=null) {
            if (!relID.isEmpty()) {
                setLink("http://pib.nic.in/Pressreleaseshare.aspx?PRID=" + relID);
            }
        }

    }
}
