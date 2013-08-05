package me.daneagle.redditreader.data;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by deagle on 7/17/13.
 */
public class Post implements Serializable {

    private String subreddit;
    private String title;
    private String author;
    private String permalink;
    private String url;
    private String domain;
    private String id;
    private int score;
    private int comments;
    private long created;
    private String thumbnailUrl;
    private String self;

    public Post(String subreddit, String title, String author, String permalink, String url,
                String domain, String id, int score, int comments, long created,
                String thumnailUrl, String self){
        this.subreddit = subreddit;
        this.title = title;
        this.author = author;
        this.permalink = permalink;
        this.url = url;
        this.domain = domain;
        this.id = id;
        this.score = score;
        this.comments =  comments;
        this.created = created;
        this.thumbnailUrl = thumnailUrl;
        this.self = self;
    }

    public String getDetails(){
        return "to r/" + subreddit + " by " + author + " (" + domain + ")\n" + postedAgo();
    }

    public String getComs(){
        return comments + " comments";
    }

    public String postedAgo(){
        long now = System.currentTimeMillis()/1000;
        long diff = now - created;
        if (diff < 60){
            return String.format("%d seconds ago", diff);
        } else if (diff < 2 * 60) {
            return String.format("%d min %d secs ago",
                    TimeUnit.SECONDS.toMinutes(diff), diff -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(diff)));
        } else if (diff < 60 * 60) {
            return String.format("%d mins %d secs ago",
                    TimeUnit.SECONDS.toMinutes(diff), diff -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(diff)));
        } else if (diff < 2 * 60 * 60) {
            return String.format("%d hour %d mins %d secs ago",
                    TimeUnit.SECONDS.toHours(diff),
                    TimeUnit.SECONDS.toMinutes(diff) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours
                    (diff)),
                    diff - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(diff)));
        }
        return String.format("%d hours %d mins %d secs ago",
                TimeUnit.SECONDS.toHours(diff),
                TimeUnit.SECONDS.toMinutes(diff) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours
                        (diff)),
                diff - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(diff)));
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self){
        this.self = self;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
