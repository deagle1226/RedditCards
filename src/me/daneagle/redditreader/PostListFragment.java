package me.daneagle.redditreader;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.daneagle.redditreader.Utils.LoaderImageView;
import me.daneagle.redditreader.Utils.Utils;
import me.daneagle.redditreader.data.DownloadImageTask;
import me.daneagle.redditreader.data.Post;
import me.daneagle.redditreader.data.PostManager;
import me.daneagle.redditreader.lazyload.ImageLoader;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

/**
 * A list fragment representing a list of Posts. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link PostDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class PostListFragment extends ListFragment implements AbsListView.OnScrollListener,
        PullToRefreshAttacher.OnRefreshListener {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(Post post);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(Post post) {
        }
    };

    private String subreddit;
    private String filter;
    private List<Post> posts;
    private PostManager manager;

    private boolean loadingMore = false;
    private View loadMoreFooter;

    private PullToRefreshAttacher pullToRefreshAttacher;
    private ImageLoader imageLoader;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PostListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        posts = new ArrayList<Post>();
        subreddit = null;
        filter = null;
        loadMoreFooter = createLoadingFooter();
        imageLoader = new ImageLoader(getActivity(), false);
        initialize();
    }

    public void setAttacher(PullToRefreshAttacher attacher){
        pullToRefreshAttacher = attacher;

    }

    public void initialize(final boolean... reloads){
        manager = new PostManager(subreddit, filter);
        new AsyncTask<Void, Void, List<Post>>() {

            @Override
            protected List<Post> doInBackground(Void... params) {
                if (reloads.length > 0 && reloads[0]){
                    return manager.reloadPosts();
                }
                return manager.fetchPosts();
            }

            @Override
            protected void onPostExecute(List<Post> result) {
                super.onPostExecute(result);
                posts.clear();
                posts.addAll(result);

                if (reloads.length > 0 && reloads[0]){
                    ArrayAdapter<Post> adapter = (ArrayAdapter<Post>)getListAdapter();
                    adapter.notifyDataSetChanged();
                    pullToRefreshAttacher.setRefreshComplete();
                } else {
                    getListView().setDivider(null);
                    getListView().setDividerHeight(0);
                    getListView().addFooterView(loadMoreFooter);
                    setListAdapter(createAdapter());
                    setScrollListener();
                    getListView().removeFooterView(loadMoreFooter);
                    getListView().setSelector(android.R.color.transparent);
                }
                setListShown(true);
            }
        }.execute();
    }

    public void loadMore(){
        System.out.println("LOADING MORE");
        loadingMore = !loadingMore;
        getListView().addFooterView(loadMoreFooter);
        new AsyncTask<Void, Void, List<Post>>() {

            @Override
            protected List<Post> doInBackground(Void... params) {
                return manager.fetchMore();
            }

            @Override
            protected void onPostExecute(List<Post> result) {
                super.onPostExecute(result);
                posts.addAll(result);
                ArrayAdapter<Post> adapter = (ArrayAdapter<Post>)getListAdapter();
                adapter.notifyDataSetChanged();
                loadingMore = !loadingMore;
                getListView().removeFooterView(loadMoreFooter);
            }
        }.execute();
    }

    public void setFilter(String filter){
        this.filter = filter;
        setListShown(false);
    }

    public void setSubreddit(String subreddit){
        this.subreddit = subreddit;
        setListShown(false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(posts.get(position));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    // TODO REFACTOR
    private ArrayAdapter<Post> createAdapter() {
        return new ArrayAdapter<Post>(getActivity(), R.layout.post_list_item, posts) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView postTitle;
                TextView postDetails;
                TextView postScore;
                TextView postComments;
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean fullImage = sharedPref.getBoolean("pref_list_img_preview", true);

                if (Utils.pointsToImage(posts.get(position).getUrl()) && fullImage){
                    convertView = getActivity().getLayoutInflater().inflate(R.layout
                            .post_list_item_fullimg, null);
                    postTitle = (TextView) convertView.findViewById(R.id.post_title);
                    postDetails = (TextView) convertView.findViewById(R.id.post_details);
                    postScore = (TextView) convertView.findViewById(R.id.post_score);
                    postComments = (TextView) convertView.findViewById(R.id.post_comments);
                    LoaderImageView image = (LoaderImageView) convertView.findViewById(R.id.post_image);
                    imageLoader.DisplayImage(posts.get(position).getUrl(), image);
                } else if (Utils.pointsToImage(posts.get(position).getThumbnailUrl())) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout
                            .post_list_item, null);
                    postTitle = (TextView) convertView.findViewById(R.id.post_title);
                    postDetails = (TextView) convertView.findViewById(R.id.post_details);
                    postScore = (TextView) convertView.findViewById(R.id.post_score);
                    postComments = (TextView) convertView.findViewById(R.id.post_comments);

                    LoaderImageView image = (LoaderImageView) convertView.findViewById(R.id.post_image);
                    imageLoader.DisplayImage(posts.get(position).getThumbnailUrl(), image);

                    if (posts.get(position).getTitle().length() < 150){
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                                (RelativeLayout.LayoutParams.MATCH_PARENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                        params.addRule(RelativeLayout.RIGHT_OF, image.getId());
                        params.addRule(RelativeLayout.BELOW, postTitle.getId());
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        postDetails.setLayoutParams(params);
                    }
                } else if (Utils.pointsToImage(posts.get(position).getUrl())){
                    convertView = getActivity().getLayoutInflater().inflate(R.layout
                            .post_list_item_noimg, null);
                    postTitle = (TextView) convertView.findViewById(R.id.post_title);
                    postDetails = (TextView) convertView.findViewById(R.id.post_details);
                    postScore = (TextView) convertView.findViewById(R.id.post_score);
                    postComments = (TextView) convertView.findViewById(R.id.post_comments);
                    ImageView image = (ImageView) convertView.findViewById(R.id.post_image);
                    image.setImageDrawable(getResources().getDrawable(R.drawable.ic_picture_blue));
                    if (posts.get(position).getTitle().length() < 90){
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                                (RelativeLayout.LayoutParams.MATCH_PARENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                        params.addRule(RelativeLayout.RIGHT_OF, image.getId());
                        params.addRule(RelativeLayout.BELOW, postTitle.getId());
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        postDetails.setLayoutParams(params);
                    }
                } else if (posts.get(position).getSelf() != null && posts.get(position).getSelf().length() > 0) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout
                            .post_list_item_noimg, null);
                    postTitle = (TextView) convertView.findViewById(R.id.post_title);
                    postDetails = (TextView) convertView.findViewById(R.id.post_details);
                    postScore = (TextView) convertView.findViewById(R.id.post_score);
                    postComments = (TextView) convertView.findViewById(R.id.post_comments);
                    ImageView image = (ImageView) convertView.findViewById(R.id.post_image);
                    image.setImageDrawable(getResources().getDrawable(R.drawable.ic_author_blue));
                    if (posts.get(position).getTitle().length() < 90){
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                                (RelativeLayout.LayoutParams.MATCH_PARENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                        params.addRule(RelativeLayout.RIGHT_OF, image.getId());
                        params.addRule(RelativeLayout.BELOW, postTitle.getId());
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        postDetails.setLayoutParams(params);
                    }
                } else {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout
                            .post_list_item_noimg, null);
                    postTitle = (TextView) convertView.findViewById(R.id.post_title);
                    postDetails = (TextView) convertView.findViewById(R.id.post_details);
                    postScore = (TextView) convertView.findViewById(R.id.post_score);
                    postComments = (TextView) convertView.findViewById(R.id.post_comments);
                    ImageView image = (ImageView) convertView.findViewById(R.id.post_image);
                    image.setImageDrawable(getResources().getDrawable(R.drawable.ic_website_blue));
                    if (posts.get(position).getTitle().length() < 90){
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                                (RelativeLayout.LayoutParams.MATCH_PARENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                        params.addRule(RelativeLayout.RIGHT_OF, image.getId());
                        params.addRule(RelativeLayout.BELOW, postTitle.getId());
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        postDetails.setLayoutParams(params);
                    }
                }

                postTitle.setText(posts.get(position).getTitle());
                postDetails.setText(posts.get(position).getDetails());
                postScore.setText(Integer.toString(posts.get(position).getScore()));
                postComments.setText(posts.get(position).getComs());

                return convertView;
            }
        };
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisible, int visibleCount,
                         int totalItems) {
        if (firstVisible + visibleCount >= totalItems - visibleCount && !loadingMore){
            loadMore();
        }
    }

    public void setScrollListener() {
        getListView().setOnScrollListener(this);
        pullToRefreshAttacher.setRefreshableView(getListView(), this);
    }


    private View createLoadingFooter() {
        return getActivity().getLayoutInflater().inflate(R.layout.list_load_more, null);
    }


    @Override
    public void onRefreshStarted(View view) {
        initialize(true);
    }
}
