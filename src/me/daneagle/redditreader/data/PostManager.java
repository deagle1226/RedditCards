package me.daneagle.redditreader.data;

import android.graphics.drawable.Drawable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by deagle on 7/17/13.
 */
public class PostManager {

    private String URL_TEMPLATE = "http://www.reddit.com/r/SUBREDDIT_NAME/FILTER/" +
            ".json?count=25&after=AFTER";

    private String subreddit;
    private String filter;
    private String url;
    private String after;
    private List<Post> posts;

    public PostManager(String subreddit, String filter){
        this.subreddit = subreddit;
        this.filter = filter;
        after = "";
        posts = new ArrayList<Post>();
        generateURL();
    }

    private void generateURL(){
        if (subreddit == null) {
            url = URL_TEMPLATE.replace("/r/SUBREDDIT_NAME", "");
        } else {
            url = URL_TEMPLATE.replace("SUBREDDIT_NAME", subreddit.toLowerCase());
        }
        if (filter == null) {
            url = url.replace("/FILTER", "");
        } else {
            url = url.replace("FILTER", filter.toLowerCase());
        }
        url = url.replace("AFTER", after);
    }

    public List<Post> fetchPosts(){
        String raw = RemoteData.readContents(url);
        return parseJSON(raw);
    }

    public List<Post> reloadPosts(){
        after = "";
        generateURL();
        String raw = RemoteData.requestContents(url);
        return parseJSON(raw);
    }

    public List<Post> fetchMore() {
        generateURL();
        return fetchPosts();
    }

    private List<Post> parseJSON(String json) {
        List<Post> list = new ArrayList<Post>();
        try {
            JSONObject data = new JSONObject(json).getJSONObject("data");
            JSONArray children = data.getJSONArray("children");
            after = data.getString("after");

            for (int i = 0; i < children.length(); i++) {
                JSONObject child = children.getJSONObject(i).getJSONObject("data");
                Post p = new Post(child.optString("subreddit"), child.optString("title"),
                        child.optString("author"), child.optString("permalink"),
                        child.optString("url"), child.optString("domain"), child.optString("id"),
                        child.optInt("score"), child.optInt("num_comments"),
                        child.optLong("created_utc"), child.optString("thumbnail"), child.optString("selftext"));
                if (p.getTitle() != null){
                    list.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
