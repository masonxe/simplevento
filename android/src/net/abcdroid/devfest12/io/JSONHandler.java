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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.Context;

/**
 * An abstract object that is in charge of parsing JSON data with a given, known
 * structure, into a list of content provider operations (inserts, updates,
 * etc.) representing the data.
 */
public abstract class JSONHandler {
    protected static Context mContext;

	protected JSONHandler(Context context) {
		mContext = context;
	}

	public abstract ArrayList<ContentProviderOperation> parse(String json) throws IOException;

	/**
	 * Loads the JSON text resource with the given ID and returns the JSON
	 * content.
	 */
	public static String loadResourceJson(Context context, int resource) throws IOException {
		InputStream is = context.getResources().openRawResource(resource);
		Writer writer = new StringWriter();
		char[] buffer = new char[1024];
		try {
			Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		} finally {
			is.close();
		}

		return writer.toString();
	}

	public static String loadResourceJsonRemote(Context context, String url) throws IOException {
		// InputStream is = context.getResources().openRawResource(resource);
		InputStream is = downloadUrl(url);
		Writer writer = new StringWriter();
		char[] buffer = new char[1024];
		try {
			Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			is.close();
		}

		return writer.toString();
	}

	private static InputStream downloadUrl(String url_) {
		HttpURLConnection con = null;
		URL url;
		InputStream is = null;
		try {
			url = new URL(url_);
			con = (HttpURLConnection) url.openConnection();
			con.setReadTimeout(10000 /* milliseconds */);
			con.setConnectTimeout(15000 /* milliseconds */);
			con.setRequestMethod("GET");
			con.setDoInput(true);			
			con.connect();
			is = con.getInputStream();
		} catch (Exception e) {			
			e.printStackTrace();
		}
		return is;

	}

}
