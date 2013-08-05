package me.daneagle.redditreader.data;

import java.util.concurrent.TimeUnit;

/**
 * Created by deagle on 7/17/13.
 */
public class Comment {

    private String author;
    private String body;
    private String html;
    private int score;
    private int depth;
    private long created;

    public Comment(String author, String body, String html, int score, int depth, long created){
        this.author = author;
        this.body = body;
        this.html = html;
        this.score = score;
        this.depth = depth;
        this.created = created;
    }

    public String getDetails(){
        return "by " + author + " " + postedAgo();
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }
}
