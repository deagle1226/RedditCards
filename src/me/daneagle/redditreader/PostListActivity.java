package me.daneagle.redditreader;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import me.daneagle.redditreader.Utils.Utils;
import me.daneagle.redditreader.data.Post;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;


/**
 * An activity representing a list of Posts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PostDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link PostListFragment} and the item details
 * (if present) is a {@link PostDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link PostListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class PostListActivity extends FragmentActivity
        implements PostListFragment.Callbacks, ListView.OnItemClickListener,
        ActionBar.OnNavigationListener {

    public static int SDK_INT = Build.VERSION.SDK_INT;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;

    private ArrayAdapter<CharSequence> navList;

    private PullToRefreshAttacher pullToRefreshAttacher;

    private String filter = "Hot";
    private String subreddit = "Front Page";
    private Post post;

    private String fontFamily = "sans-serif-light";

    private Menu menu;

    private final int HEADER_1 = 0;
    private final int HEADER_2 = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);

        if (SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1){
            fontFamily = "sans-serif-light";
        }

        if (findViewById(R.id.post_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((PostListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.post_list))
                    .setActivateOnItemClick(true);
        }

        PullToRefreshAttacher.Options ptrOptions = new PullToRefreshAttacher.Options();
        ptrOptions.refreshScrollDistance = 0.34f;
        pullToRefreshAttacher = new PullToRefreshAttacher(this, ptrOptions);

        PostListFragment frag = (PostListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.post_list);
        frag.setAttacher(pullToRefreshAttacher);

        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_selectable_list_item,
                android.R.id.text1, getResources().getStringArray(R
                .array.subreddit_array)){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (getItem(position).equalsIgnoreCase("default") || getItem(position)
                        .equalsIgnoreCase("favorites")){
                    convertView = getLayoutInflater().inflate(R.layout.list_section_header,
                            parent, false);
                    TextView textView = (TextView)convertView.findViewById(R.id.list_section_header);
                    textView.setText(getItem(position));
                    return convertView;
                }

                convertView = getLayoutInflater().inflate(android.R.layout
                        .simple_selectable_list_item, parent, false);
                TextView textView = (TextView)convertView.findViewById(android.R.id.text1);
                textView.setText(getItem(position));
                textView.setTypeface(Typeface.create(fontFamily, Typeface.NORMAL));
                textView.setAllCaps(true);
                textView.setPadding(Utils.dip(20f, getContext()), textView.getPaddingTop(),
                        textView.getPaddingRight(), textView.getPaddingBottom());
                return convertView;
            }
        });
        drawerList.setOnItemClickListener(this);
        drawerList.setDivider(null);
        drawerList.setDividerHeight(0);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer,
                R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getActionBar().setDisplayShowTitleEnabled(false);
                getActionBar().setTitle(R.string.app_name);
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getActionBar().setDisplayShowTitleEnabled(true);
                getActionBar().setTitle("Subreddits".toUpperCase());
                getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        navList = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.filter_array)){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = getLayoutInflater().inflate(R.layout.nav_list_selected, parent, false);
                }
                TextView title = (TextView) view.findViewById(R.id.nav_list_title);
                title.setText(subreddit);
                title.setAllCaps(true);
                title.setTypeface(Typeface.create(fontFamily, Typeface.NORMAL));
                TextView subtitle = (TextView) view.findViewById(R.id.nav_list_subtitle);
                subtitle.setText(filter.toUpperCase());
                subtitle.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView
                , parent);
                textView.setAllCaps(true);
                textView.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
                return textView;
            }
        };
        navList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setListNavigationCallbacks(navList, this);

        final int actionBarTitle = getResources().getSystem().getIdentifier("action_bar_title", "id",
                "android");
        final TextView title = (TextView)getWindow().findViewById(actionBarTitle);
        if ( title != null ) {
            title.setTypeface(Typeface.create(fontFamily, Typeface.NORMAL));
        }

        getActionBar().setDisplayShowTitleEnabled(false);

    }

    public PullToRefreshAttacher getAttacher(){
        return pullToRefreshAttacher;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //drawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (((String)adapterView.getItemAtPosition(i)).equalsIgnoreCase("default") ||
                ((String)adapterView.getItemAtPosition(i)).equalsIgnoreCase("favorites")) return;
        selectDrawerItem(i);
    }

    private void selectDrawerItem(int position){
        String subreddit =  getResources().getStringArray(R.array.subreddit_array)[position];
        this.subreddit = subreddit;
        if (subreddit.equalsIgnoreCase("Front Page")) subreddit = null;
        PostListFragment frag = (PostListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.post_list);
        frag.setSubreddit(subreddit);
        frag.initialize();
        navList.notifyDataSetChanged();
        drawerList.setItemChecked(position, true);
        drawerLayout.closeDrawer(drawerList);
        navList.notifyDataSetChanged();
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(drawerList)) {
                    drawerLayout.closeDrawer(drawerList);
                } else {
                    drawerLayout.openDrawer(drawerList);
                }
                return true;
            case R.id.activity_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        String filter = getResources().getStringArray(R.array.filter_array)[i];
        this.filter = filter;
        if (filter.equalsIgnoreCase("Hot")) filter = null;
        PostListFragment frag = (PostListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.post_list);
        frag.setFilter(filter);
        frag.initialize();
        navList.notifyDataSetChanged();
        return true;
    }


    @Override
    public void onItemSelected(Post post) {
        this.post = post;
        if (mTwoPane) {
            PostDetailFragment frag = (PostDetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.post_detail_container);
            if (frag != null) frag.cancelFetch();
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putSerializable(Post.class.getSimpleName(), post);
            PostDetailFragment fragment = new PostDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.post_detail_container, fragment)
                    .commit();
            TextView text = (TextView) menu.findItem(R.id.action_bar_text).getActionView()
                    .findViewById(R.id
                            .action_bar_textview);
            text.setText(post.getTitle());
            TextView subtext = (TextView) menu.findItem(R.id.action_bar_text).getActionView()
                    .findViewById(R.id
                            .action_bar_textview_sub);
            subtext.setText(post.getDetails() + " | " + post.getComs());

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, PostDetailActivity.class);
            detailIntent.putExtra(Post.class.getSimpleName(), post);
            startActivity(detailIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        if (mTwoPane) getMenuInflater().inflate(R.menu.action_bar_text, menu);

        return super.onCreateOptionsMenu(menu);
    }
}
