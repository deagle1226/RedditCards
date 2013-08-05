package me.daneagle.redditreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.daneagle.redditreader.Utils.LoaderImageView;
import me.daneagle.redditreader.Utils.Utils;
import me.daneagle.redditreader.data.Comment;
import me.daneagle.redditreader.data.CommentManager;
import me.daneagle.redditreader.data.DownloadImageTask;
import me.daneagle.redditreader.data.Post;
import me.daneagle.redditreader.lazyload.ImageLoader;

/**
 * A fragment representing a single Post detail screen.
 * This fragment is either contained in a {@link PostListActivity}
 * in two-pane mode (on tablets) or a {@link PostDetailActivity}
 * on handsets.
 */
public class PostDetailFragment extends ListFragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */

    private Post post;
    private List<Comment> comments;
    private CommentManager manager;
    private AsyncTask<Void, Void, List<Comment>> asyncTask;
    private ImageLoader imageLoader;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PostDetailFragment() {
        comments = new ArrayList<Comment>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageLoader = new ImageLoader(getActivity(), true);
        if (getArguments().containsKey(Post.class.getSimpleName())) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            post = (Post) getArguments().getSerializable(Post.class.getSimpleName());
            manager = new CommentManager("http://reddit.com" + post.getPermalink() + ".json");
            initialize();
        } else if (getActivity().getIntent().getExtras().containsKey(Post.class.getSimpleName())) {
            post = (Post) getActivity().getIntent().getExtras().getSerializable(Post.class
                    .getSimpleName());
            manager = new CommentManager("http://reddit.com" + post.getPermalink() + ".json");
            initialize();
            getActivity().getActionBar().setTitle(post.getTitle());
            getActivity().getActionBar().setSubtitle(post.getDetails() + " | " + post.getComs());
        } else {
            System.out.println("POST DIDN'T GO THRU");
            return;
        }

    }

    private void initialize(){
        if (asyncTask != null && asyncTask.getStatus() != AsyncTask.Status.FINISHED) asyncTask.cancel(true);
        asyncTask = new AsyncTask<Void, Void, List<Comment>>() {

            @Override
            protected List<Comment> doInBackground(Void... params) {
                List<Comment> comments = manager.reloadComments();
                if (isCancelled()) return null;
                return comments;
            }

            @Override
            protected void onPostExecute(List<Comment> result) {
                if (isCancelled()) onDestroy();
                super.onPostExecute(result);
                comments.addAll(result);
                addHeader();
                setListAdapter(createAdapter());
                getListView().setHeaderDividersEnabled(false);
                //getListView().setDivider(null);
                //getListView().setDividerHeight(-1);
                //getListView().setBackgroundResource(R.drawable.comment_list_bg);
                //getListView().setPadding(0,0,0,0);
            }
        };
        asyncTask.execute();
    }

    public void cancelFetch(){
        if (asyncTask != null)asyncTask.cancel(true);
    }

    private ArrayAdapter<Comment> createAdapter() {

        return new ArrayAdapter<Comment>(getActivity(), R.layout.comment_list_item,
                comments) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout
                            .comment_list_item, parent, false);
                }

                TextView postTitle;
                postTitle = (TextView) convertView.findViewById(R.id.comment_title);
                TextView postDetails;
                postDetails = (TextView) convertView.findViewById(R.id.comment_details);
                TextView postScore;
                postScore = (TextView) convertView.findViewById(R.id.comment_score);

                postTitle.setText(comments.get(position).getBody());
                postDetails.setText(comments.get(position).getDetails());
                postScore.setText(Integer.toString(comments.get(position).getScore()));

                convertView.setPadding(comments.get(position).getDepth() * Utils.dip(16f,
                        getContext()), convertView.getPaddingTop(), convertView.getPaddingRight(),
                        convertView.getPaddingBottom());
                return convertView;
            }
        };

    }

    private void addHeader(){
        TextView postTitle;
        TextView postDetails;
        TextView postScore;

        Activity a = getActivity();
        LayoutInflater lf = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = lf.inflate(R.layout.post_detail_view,null, false);
        if (Utils.pointsToImage(post.getUrl())) {
            LoaderImageView image = (LoaderImageView) v.findViewById(R.id.post_image);
            imageLoader.DisplayImage(post.getUrl(), image);
        } else if (Utils.pointsToImage(post.getThumbnailUrl())) {
            v = getActivity().getLayoutInflater().inflate(R.layout.post_detail_view_thumb, null,
                    false);;
            LoaderImageView image = (LoaderImageView) v.findViewById(R.id.post_image);
            imageLoader.DisplayImage(post.getThumbnailUrl(), image);
        } else if (post.getSelf() != null && post.getSelf().length() > 0) {
            v = getActivity().getLayoutInflater().inflate(R.layout.post_detail_view_self, null,
                    false);
            TextView selfText = (TextView) v.findViewById(R.id.post_self);
            selfText.setText(post.getSelf());
        } else {
            v = getActivity().getLayoutInflater().inflate(R.layout.post_detail_view_noimg, null,
                    false);
        }

        postTitle = (TextView) v.findViewById(R.id.post_title);
        postDetails = (TextView) v.findViewById(R.id.post_details);
        postScore = (TextView) v.findViewById(R.id.post_score);
        postTitle.setText(post.getTitle());
        postDetails.setText(post.getDetails() + " | " + post.getComs());
        postScore.setText(Integer.toString(post.getScore()));

        Button link = (Button) v.findViewById(R.id.link);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(post.getUrl()));
                startActivity(i);
            }
        });
        Button share = (Button) v.findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        Button author = (Button) v.findViewById(R.id.author);
        author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        getListView().addHeaderView(v);
    }
}
