package utils;

import android.util.Log;
import android.util.Xml;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;

import static com.android.volley.VolleyLog.TAG;

/**
 * Created by bunny on 29/09/17.
 */

public class NewsParser {

    String response;




    XmlPullParser parser;

    public NewsParser(String response) {
        this.response = response;


    }

    public ArrayList<News> parseJson(){

        XmlToJson xmlToJson = new XmlToJson.Builder(response).build();

        Log.d(TAG, "NewsParser: "+xmlToJson.toFormattedString());

        JSONObject jsonObject =xmlToJson.toJson();


        ArrayList<News> newsArrayList = new ArrayList<>();
        try {
            JSONObject jsonObjectChannel = jsonObject.getJSONObject("rss").getJSONObject("channel");

            JSONArray jsonArrayItem = jsonObjectChannel.getJSONArray("item");

            Log.d(TAG, "parseJson: "+jsonArrayItem);

            for (int i=0 ; i<jsonArrayItem.length(); i++){

                JSONObject itemObject= jsonArrayItem.getJSONObject(i);

                News news = new News();

                /*news.setTitle(itemObject.getString("title"));
                news.setDescription(itemObject.getString("description"));
                news.setLink(itemObject.getString("link"));
                news.setPubDate(itemObject.getString("pubDate"));
*/
                try {
                    news.setTitle(URLDecoder.decode(URLEncoder.encode(itemObject.getString("title"), "iso8859-1"), "UTF-8"));
                    news.setDescription(URLDecoder.decode(URLEncoder.encode(itemObject.getString("description"), "iso8859-1"), "UTF-8"));
                }catch (Exception e){
                    news.setTitle(itemObject.getString("title"));
                    news.setDescription(itemObject.getString("description"));
                }
                news.setLink(itemObject.getString("link"));
                news.setPubDate(itemObject.getString("pubDate"));


                newsArrayList.add(news);


            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return newsArrayList;
    }

}
