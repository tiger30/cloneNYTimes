package coderschool.icestone.clonenytimes.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by IceStone on 3/16/2016.
 */
public class Article implements Serializable {
    private static final String IMAGE_PREFIX = "http://www.nytimes.com/";
    String webUrl;
    String headline;
    String thumbnail;

    public String getWebUrl() {
        return webUrl;
    }

    public String getHeadline() {
        return headline;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public Article(String webUrl, String headline, String thumbnail) {
        this.webUrl = webUrl;
        this.headline = headline;
        this.thumbnail = thumbnail;
    }

    public Article(JSONObject jsonObject) {
        try {
            this.webUrl = jsonObject.getString("web_url");
            this.headline = jsonObject.getJSONObject("headline").getString("main");
            JSONArray multimedia = jsonObject.getJSONArray("multimedia");
            if (multimedia.length() > 0) {
                this.thumbnail = IMAGE_PREFIX + multimedia.getJSONObject(0).getString("url");
            } else {
                this.thumbnail = "";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Article> fromJsonArray(JSONArray jsonArray) {
        ArrayList<Article> results = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++ ) {
            try {
                results.add(new Article(jsonArray.getJSONObject(i)));
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
        return results;
    }

}
