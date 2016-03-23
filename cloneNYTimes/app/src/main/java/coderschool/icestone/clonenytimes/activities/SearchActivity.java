package coderschool.icestone.clonenytimes.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Set;

import coderschool.icestone.clonenytimes.R;
import coderschool.icestone.clonenytimes.SpacesItemDecoration;
import coderschool.icestone.clonenytimes.adapters.ArticleAdapter;
import coderschool.icestone.clonenytimes.fragments.FilterSettingsFragment;
import coderschool.icestone.clonenytimes.listeners.EndlessRecyclerViewScrollListener;
import coderschool.icestone.clonenytimes.listeners.RecyclerItemClickListener;
import coderschool.icestone.clonenytimes.models.Article;
import coderschool.icestone.clonenytimes.models.SearchFilter;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.util.TextUtils;

public class SearchActivity extends AppCompatActivity
        implements FilterSettingsFragment.OnSettingsChangeListener, DatePickerDialog.OnDateSetListener {
    public static final String FILENAME = "searchFilter.txt";

    public static final String NYT_API_KEY = "2429de0a20fae61bcdc8626c5a95d172:4:74784179";
    public static final String NYT_ARTICLE_SEARCH_URL = "http://api.nytimes.com/svc/search/v2/articlesearch.json";

    RecyclerView rvResults;
    ArrayList<Article> articles;
    ArticleAdapter adapter;
    SearchFilter searchFilter;
    String searchQuery;
    String numSearchResults;
    FilterSettingsFragment settingsDialog;
    TextView tvNumResults;
    TextView tvSearchQueryValue;
    TextView tvSearchFilterTitle;
    TextView tvSearchFilterValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        searchFilter = loadSearchFilter();
        setupViews();
    }

    public void setupViews() {
        articles = new ArrayList<>();

        tvSearchQueryValue = (TextView) findViewById(R.id.tvSearchQueryValue);
        tvNumResults = (TextView) findViewById(R.id.tvNumResults);
        tvSearchFilterTitle = (TextView) findViewById(R.id.tvSearchFilterTitle);
        tvSearchFilterValue = (TextView) findViewById(R.id.tvSearchFilterValue);

        rvResults = (RecyclerView) findViewById(R.id.rvResults);
        adapter = new ArticleAdapter(articles);
        rvResults.setAdapter(adapter);

        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvResults.setLayoutManager(gridLayoutManager);
        SpacesItemDecoration decoration = new SpacesItemDecoration(16);
        rvResults.addItemDecoration(decoration);
        rvResults.addOnItemTouchListener(
                new RecyclerItemClickListener(this, rvResults,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Intent i = new Intent(getApplicationContext(), ArticleActivity.class);
                                Article article = articles.get(position);
                                i.putExtra("article", article);
                                startActivity(i);
                            }

                            @Override
                            public void onItemLongClick(View view, int position) {
                                // ...
                            }
                        }));
        rvResults.setOnScrollListener(new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadMoreArticles(page);
            }
        });

        searchForArticles("Top Stories");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchForArticles(query);
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showSettingsDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void searchForArticles(String query) {
        searchQuery = query;
        tvSearchQueryValue.setText(searchQuery);
        if (searchFilter.isFilterSet()) {
            tvSearchFilterTitle.setVisibility(View.VISIBLE);
            tvSearchFilterValue.setVisibility(View.VISIBLE);
            tvSearchFilterValue.setText(searchFilter.toString());
        } else {
            tvSearchFilterTitle.setVisibility(View.GONE);
            tvSearchFilterValue.setVisibility(View.GONE);
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(NYT_ARTICLE_SEARCH_URL,
                getSearchQueryParams(query, 0),
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        JSONArray articleJsonResults = null;
                        try {
                            JSONObject responseObj = response.getJSONObject("response");
                            articleJsonResults = responseObj.getJSONArray("docs");
                            numSearchResults = responseObj.getJSONObject("meta").getString("hits");
                            tvNumResults.setText(numSearchResults);
                            adapter.swap(Article.fromJsonArray(articleJsonResults));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void loadMoreArticles(int page) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(NYT_ARTICLE_SEARCH_URL,
                getSearchQueryParams(searchQuery, page),
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        JSONArray articleJsonResults = null;
                        try {
                            articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                            articles.addAll(Article.fromJsonArray(articleJsonResults));
                            int curSize = adapter.getItemCount();
                            adapter.notifyItemRangeInserted(curSize, articles.size() - 1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void showSettingsDialog() {
        FragmentManager fm = getSupportFragmentManager();
        settingsDialog = FilterSettingsFragment.newInstance(searchFilter);
        settingsDialog.show(fm, "fragment_edit_name");
    }

    // http://developer.nytimes.com/docs/read/article_search_api_v2
    private RequestParams getSearchQueryParams(String query, int page) {
        RequestParams params = new RequestParams();
        params.put("q", query);
        params.put("page", page);
        params.put("api-key", NYT_API_KEY);

        String beginDateYYYYMMDD = searchFilter.getBeginDate(SearchFilter.FORMAT_YYYYMMDD);
        if (!TextUtils.isBlank(beginDateYYYYMMDD)) {
            params.put("begin_date", beginDateYYYYMMDD);
        }

        String sortOrder = searchFilter.getSortOrder();
        if (!TextUtils.isBlank(sortOrder)) {
            params.put("sort", sortOrder);
        }

        Set ndTopics = searchFilter.getNewsDeskTopics();
        if (ndTopics.size() > 0) {
            params.put("fq", "news_desk:(\"" + StringUtils.join(ndTopics.toArray(), "\" \"") + "\")");
        }

        Log.d("DEBUG", ">>>>>" + params.toString());
        return params;
    }

    public void saveSearchFilterSettings(SearchFilter newSearchFilter) {
        searchFilter = newSearchFilter;
        saveSearchFilter(searchFilter);
        searchForArticles(searchQuery);
    }

    public void onDateSet(DatePicker view, int yy, int mm, int dd) {
        populateSetDate(yy, mm + 1, dd);
    }

    public void populateSetDate(int year, int month, int day) {
        if (settingsDialog != null) {
            settingsDialog.populateSetDate(year, month, day);
        }
    }


    private void saveSearchFilter(SearchFilter filter) {
        try {
            FileOutputStream fos = this.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(filter);
            os.close();
            fos.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public SearchFilter loadSearchFilter() {
        SearchFilter filter = new SearchFilter();
        try {
            FileInputStream fis = this.openFileInput(FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            filter = (SearchFilter) is.readObject();
            is.close();
            fis.close();
            return (filter);
        } catch (ClassNotFoundException cnfe) {
            Log.e("Exception", "ClassNotFoundException: " + cnfe.toString());
        } catch (IOException e) {
            Log.e("Exception", "IOException: " + e.toString());
        }
        return filter;
    }

}