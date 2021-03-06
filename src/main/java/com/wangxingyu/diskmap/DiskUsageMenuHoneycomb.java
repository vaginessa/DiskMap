package com.wangxingyu.diskmap;

import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;

import com.wangxingyu.diskmap.entity.FileSystemSuperRoot;

public class DiskUsageMenuHoneycomb extends DiskUsageMenu {
    private SearchView searchView;
    private Drawable origSearchBackground;

    public DiskUsageMenuHoneycomb(DiskUsage diskusage) {
        super(diskusage);
    }

    @Override
    public void onCreate() {
        ActionBar actionBar = diskusage.getActionBar();
        if (actionBar != null) {//actionBar = null FIXME
            actionBar.setDisplayHomeAsUpEnabled(true); //
        }
    }

    @Override
    public boolean readyToFinish() {
        return true;
    }

    @Override
    public void wrapAndSetContentView(View view, FileSystemSuperRoot newRoot) {
        super.wrapAndSetContentView(view, newRoot);
        diskusage.setContentView(view);
        diskusage.invalidateOptionsMenu();
    }

    public void setShowAsAction(MenuItem item) {
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean finishedSearch(FileSystemSuperRoot newRoot, String searchQuery) {
        boolean matched = super.finishedSearch(newRoot, searchQuery);
        if (matched) {
            searchView.setBackgroundDrawable(origSearchBackground);
        } else {
            searchView.setBackgroundColor(Color.parseColor("#FFDDDD"));
        }
        return matched;
    }

    @Override
    public MenuItem makeSearchMenuEntry(Menu menu) {
        MenuItem item = menu.add("Search");
        searchView = new SearchView(diskusage);
        origSearchBackground = searchView.getBackground();
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(android.R.drawable.ic_search_category_default);
        item.setActionView(searchView);
        if (searchPattern != null) {
            searchView.setIconified(false);
            searchView.setQuery(searchPattern, false);
        }
        searchView.setOnCloseListener(new OnCloseListener() {
            @Override
            public boolean onClose() {
                Log.d("diskusage", "search closed");
                searchPattern = null;
                diskusage.applyPatternNewRoot(masterRoot, null);
                return false;
            }
        });
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                onQueryTextChange(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("diskusage", "search query changed to: " + newText);
                searchPattern = newText;
                applyPattern(searchPattern);
                return true;
            }
        });
        return item;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        setShowAsAction(showMenuItem);
        return true;
    }


    @Override
    public void searchRequest() {
        // FIXME: implement something?
        // TODO Auto-generated method stub

    }
}
