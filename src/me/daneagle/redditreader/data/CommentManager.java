package me.daneagle.redditreader.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by deagle on 7/17/13.
 */
public class CommentManager {
    private String url;

    public CommentManager(String url) {
        this.url = url;
    }

    private Comment loadComment(JSONObject data, int depth) {
        Comment comment = null;
        try {
            comment = new Comment(data.getString("author"), data.getString("body"),
                    data.getString("body_html"), (data.getInt("ups") - data.getInt("downs")),
                    depth, data.getLong("created_utc"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return comment;
    }

    private void process(ArrayList<Comment> comments, JSONArray json, int depth) throws Exception {
        for (int i = 0; i < json.length(); i++) {
            if (json.getJSONObject(i).optString("kind") == null) continue;
            if (json.getJSONObject(i).optString("kind").equals("t1") == false) continue;
            JSONObject data = json.getJSONObject(i).getJSONObject("data");
            Comment comment = loadComment(data, depth);
            if (comment.getAuthor() != null) {
                comments.add(comment);
                addReplies(comments, data, depth + 1);
            }
        }
    }

    private void addReplies(ArrayList<Comment> comments, JSONObject parent, int depth) {
        try {
            if (parent.get("replies").equals("")) return;
            JSONArray array = parent.getJSONObject("replies").getJSONObject("data").getJSONArray
                    ("children");
            process(comments, array, depth);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Comment> fetchComments() {
        ArrayList<Comment> comments = new ArrayList<Comment>();
        try {
            String raw = RemoteData.readContents(url);
            JSONObject data = new JSONArray(raw).getJSONObject(1).getJSONObject("data");
            process(comments, data.getJSONArray("children"), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return comments;
    }

    public ArrayList<Comment> reloadComments() {
        ArrayList<Comment> comments = new ArrayList<Comment>();
        try {
            String raw = RemoteData.requestContents(url);
            JSONObject data = new JSONArray(raw).getJSONObject(1).getJSONObject("data");
            process(comments, data.getJSONArray("children"), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return comments;
    }
}
