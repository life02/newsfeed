/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tormas.home;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Represents an app in AllAppsView.
 */
public class ApplicationInfo extends ItemInfo {

    /**
     * The application name.
     */
    public CharSequence title;

    /**
     * A bitmap of the application's text in the bubble.
     */
    public Bitmap titleBitmap;

    /**
     * The intent used to start the application.
     */
    public Intent intent;

    /**
     * A bitmap version of the application icon.
     */
    public Bitmap iconBitmap;

    public ComponentName componentName;
    
    public String sourceDir;

    public int category = 0;

    ApplicationInfo() {
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT;
    }

    /**
     * Must not hold the Context.
     */
    public ApplicationInfo(ResolveInfo info, IconCache iconCache) {
        this.componentName = new ComponentName(
                info.activityInfo.applicationInfo.packageName,
                info.activityInfo.name);

        this.container = ItemInfo.NO_ID;
        this.setActivity(componentName,
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        iconCache.getTitleAndIcon(this, info);
        
        sourceDir = new String(info.activityInfo.applicationInfo.sourceDir);
        Log.d("ApplicationInfo", "ApplicationInfo sourceDir "+sourceDir);
        if(sourceDir.startsWith("/system/carrier")){
        	category = Category.CATEGORY_CARRIERAPP;
      	}else if(sourceDir.startsWith("/system/app")){
      		category = Category.CATEGORY_SYSTEMAPP;
      	}else if(sourceDir.startsWith("/opl/app")){
      		category = Category.CATEGORY_OEMAPP;
      	}else if(sourceDir.startsWith("/sdcard/app") || sourceDir.startsWith("/data/app")){
      		category = Category.CATEGORY_3RDAPP;
      	}else{
      		category = Category.CATEGORY_3RDAPP; //default also set 3rdapp
      	}
    }
    
    public ApplicationInfo(ApplicationInfo info) {
        super(info);
        componentName = info.componentName;
        title = info.title.toString();
        intent = new Intent(info.intent);
    }

    /**
     * Creates the application intent based on a component name and various launch flags.
     * Sets {@link #itemType} to {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
     *
     * @param className the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    final void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;
    }

    @Override
    public String toString() {
        return "ApplicationInfo(title=" + title.toString() + ")";
    }

    public static void dumpApplicationInfoList(String tag, String label,
            ArrayList<ApplicationInfo> list) {
        Log.d(tag, label + " size=" + list.size());
        for (ApplicationInfo info: list) {
            Log.d(tag, "   title=\"" + info.title + "\" titleBitmap=" + info.titleBitmap
                    + " iconBitmap=" + info.iconBitmap);
        }
    }

    public ShortcutInfo makeShortcut() {
        return new ShortcutInfo(this);
    }
}
