package com.yogpc.gi;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import com.google.gson.Gson;

public class HttpRequest {
  private static final Gson gson = new Gson();

  static TranslateEntry[] get(final String s) {
    try {
      final StringBuilder sb = new StringBuilder();
      sb.append("http://www.google.com/transliterate?langpair=en|ja&text=");
      sb.append(URLEncoder.encode(s, "UTF-8"));
      final URLConnection huc = new URL(sb.toString()).openConnection();
      final InputStream is = huc.getInputStream();
      final Reader r = new InputStreamReader(is, "UTF-8");
      final TranslateEntry[] te = gson.fromJson(r, TranslateEntry[].class);
      r.close();
      is.close();
      return te;
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
