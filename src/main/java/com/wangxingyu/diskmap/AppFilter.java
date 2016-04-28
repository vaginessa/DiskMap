/**
 * DiskUsage - displays sdcard usage on android.
 * Copyright (C) 2016 wangxingyu
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.wangxingyu.diskmap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

public class AppFilter implements Parcelable {
  public boolean enableChildren;
  public boolean showApk;
  public boolean showData;
  public boolean showCache;
  public boolean showDalvikCache;
  public App2SD apps;
  public App2SD memory;
  
  public enum App2SD {
    INTERNAL("internal"),
    APPS2SD("app2sd"),
    BOTH("both");
    
    private String id;
    
    App2SD(String id) {
      this.id = id;
    }
    
    public static App2SD forId(String id) {
      if ("internal".equals(id)) {
        return INTERNAL;
      }
      if ("app2sd".equals(id)) {
        return APPS2SD;
      }
      return BOTH;
    }
    public String toString() {
      return id;
    }
  }
  


  public static AppFilter getFilterForDiskUsage() {
    AppFilter filter = new AppFilter();
    filter.enableChildren = false;
    filter.showApk = true;
    filter.showData = false;
    filter.showCache = false;
    filter.showDalvikCache = false;
    filter.apps = App2SD.APPS2SD;
    filter.memory = App2SD.APPS2SD;
    return filter;
  }
  
  public static AppFilter getFilterForHoneycomb() {
    AppFilter filter = new AppFilter();
    filter.enableChildren = true;
    filter.showApk = true;
    filter.showData = true;
    filter.showCache = true;
    filter.showDalvikCache = true;
    filter.apps = App2SD.BOTH;
    filter.memory = App2SD.INTERNAL;
    return filter;
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof AppFilter)) return false;
    AppFilter filter = (AppFilter) o;
    return filter.enableChildren == enableChildren && filter.showApk == showApk && filter.showData == showData && filter.showCache == showCache && filter.showDalvikCache == showDalvikCache && filter.apps == apps && filter.memory == memory;
  }
  
  public static AppFilter loadSavedAppFilter(Context context) {
    SharedPreferences prefs =
      context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    AppFilter filter = new AppFilter();
    filter.enableChildren = true;
    filter.showApk = prefs.getBoolean("show_apk", true);
    filter.showData = prefs.getBoolean("show_data", true);
    filter.showCache = prefs.getBoolean("show_cache", true);
    filter.showDalvikCache = prefs.getBoolean("show_dalvikCache", true);
    filter.apps = App2SD.forId(prefs.getString("apps", "both"));
    filter.memory = App2SD.forId(prefs.getString("memory", "internal"));
    return filter;
  }

  public AppFilter() {}
  
  public AppFilter(Parcel in) {
    boolean[] arr = new boolean[6];
    in.readBooleanArray(arr);
    enableChildren = arr[0];
    showApk = arr[1];
    showData = arr[2];
    showCache = arr[3];
    showDalvikCache = arr[5];
    apps = App2SD.forId(in.readString());
    memory = App2SD.forId(in.readString());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeBooleanArray(new boolean[] {
        enableChildren, showApk, showData, showCache, false, showDalvikCache
    });
    dest.writeString(apps.toString());
    dest.writeString(memory.toString());
  }
  
  public static final Creator<AppFilter> CREATOR =
    new Creator<AppFilter>() {
    public AppFilter createFromParcel(Parcel in) {
      return new AppFilter(in);
    }

    public AppFilter[] newArray(int size) {
      return new AppFilter[size];
    }
  };
}
