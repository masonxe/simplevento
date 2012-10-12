/*
 * Copyright 2012 Google Inc.
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

package net.abcdroid.devfest12.io;

import static net.abcdroid.devfest12.util.LogUtils.LOGI;
import static net.abcdroid.devfest12.util.LogUtils.makeLogTag;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import net.abcdroid.devfest12.io.model.Announcement;
import net.abcdroid.devfest12.io.model.AnnouncementsResponse;
import net.abcdroid.devfest12.provider.ScheduleContract;
import net.abcdroid.devfest12.provider.ScheduleContract.Announcements;
import net.abcdroid.devfest12.provider.ScheduleContract.SyncColumns;
import net.abcdroid.devfest12.util.Lists;

/**
 * Handler that parses announcements JSON data into a list of content provider operations.
 */
public class AnnouncementsHandler extends JSONHandler {

    private static final String TAG = makeLogTag(AnnouncementsHandler.class);

    public AnnouncementsHandler(Context context, boolean local) {
        super(context);
    }

    public ArrayList<ContentProviderOperation> parse(String json)
            throws IOException {
        final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();

        AnnouncementsResponse response = new Gson().fromJson(json, AnnouncementsResponse.class);
        int numAnnouncements = 0;
        if (response.announcements != null) {
            numAnnouncements = response.announcements.length;
        }

        if (numAnnouncements > 0) {
            LOGI(TAG, "Updating announcements data");

            // Clear out existing announcements
            batch.add(ContentProviderOperation
                    .newDelete(ScheduleContract.addCallerIsSyncAdapterParameter(
                            Announcements.CONTENT_URI))
                    .build());

            for (Announcement announcement : response.announcements) {
                // Save tracks as a json array
                final String tracks =
                        (announcement.tracks != null && announcement.tracks.length > 0)
                                ? new Gson().toJson(announcement.tracks)
                                : null;

                // Insert announcement info
                batch.add(ContentProviderOperation
                        .newInsert(ScheduleContract
                                .addCallerIsSyncAdapterParameter(Announcements.CONTENT_URI))
                        .withValue(SyncColumns.UPDATED, System.currentTimeMillis())
                        // TODO: better announcements ID heuristic
                        .withValue(Announcements.ANNOUNCEMENT_ID,
                                (announcement.date + announcement.title).hashCode())
                        .withValue(Announcements.ANNOUNCEMENT_DATE, announcement.date)
                        .withValue(Announcements.ANNOUNCEMENT_TITLE, announcement.title)
                        .withValue(Announcements.ANNOUNCEMENT_SUMMARY, announcement.summary)
                        .withValue(Announcements.ANNOUNCEMENT_URL, announcement.link)
                        .withValue(Announcements.ANNOUNCEMENT_TRACKS, tracks)
                        .build());
            }
        }

        return batch;
    }
}
