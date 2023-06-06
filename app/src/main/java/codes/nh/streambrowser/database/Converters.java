package codes.nh.streambrowser.database;

import androidx.room.TypeConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codes.nh.streambrowser.utils.AppUtils;

public class Converters {

    //lists

    @TypeConverter
    public static String listToString(List<String> list) {
        return AppUtils.listToJson(list).toString();
    }

    @TypeConverter
    public static List<String> stringToList(String json) {
        List<String> list = new ArrayList<>();
        try {
            list = AppUtils.jsonToList(new JSONArray(json));
        } catch (JSONException e) {
            AppUtils.log("stringToList()", e);
        }
        return list;
    }

    //maps

    @TypeConverter
    public static String mapToString(Map<String, String> map) {
        return AppUtils.mapToJson(map).toString();
    }

    @TypeConverter
    public static Map<String, String> stringToMap(String json) {
        Map<String, String> map = new HashMap<>();
        try {
            map = AppUtils.jsonToMap(new JSONObject(json));
        } catch (JSONException e) {
            AppUtils.log("stringToMap()", e);
        }
        return map;
    }
}
