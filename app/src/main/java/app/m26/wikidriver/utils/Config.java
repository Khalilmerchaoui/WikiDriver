package app.m26.wikidriver.utils;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import app.m26.wikidriver.R;;
import app.m26.wikidriver.activities.MainActivity;
import app.m26.wikidriver.models.Airport;
import app.m26.wikidriver.models.App;
import app.m26.wikidriver.models.User;
import app.m26.wikidriver.services.CloseAppsService;
import app.m26.wikidriver.services.MyAccessibilityService;
import app.m26.wikidriver.services.WidgetService;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

    public static boolean appsStarted = false;
    public static boolean waitingState = false;
    public static boolean onCourseState = false;
    public static boolean finishedCourse = false;
    public static String currentApp = "";

    public static int  NOTIFICATION_RANK= 0;

    public static String FCM_ID = "";
    public static final String API_KEY = "AIzaSyDaxurwGLhyV3uDm9LWS4XKtOKrFTOn6Gg";

    public static final int PUBLICATION_TYPE_SALE = 1;
    public static final int PUBLICATION_TYPE_MECHANICAL = 2;
    public static final int PUBLICATION_TYPE_COURSE = 3;
    public static final int PUBLICATION_TYPE_OTHER = 4;

    public static final int HOME_FRAGMENT_RANK = 0;
    public static final int CHAT_FRAGMENT_RANK = 1;
    public static final int PROFILE_FRAGMENT_RANK = 2;
    public static final int SETTINGS_FRAGMENT_RANK = 3;

    public static final String FIREBASE_USERS_REFERENCE = "users";
    public static final String FIREBASE_MESSAGES_REFERENCE = "messages";
    public static final String FIREBASE_CHAT_REFERENCE = "Chat";
    public static final String FIREBASE_PUBLICATION_REFERENCE = "publications";
    public static final String FIREBASE_SOCIAL_REFERENCE = "socials";
    public static final String FIREBASE_PROFILE_PICS_REFERENCE = "profile_pics";
    public static final String FIREBASE_PUBLICATION_IMAGES_REFERENCE = "publication_pics";
    public static final String FIREBASE_SOCIAL_IMAGES_REFERENCE = "publication_pics";
    public static final String FIREBASE_SOCIAL_VIDEOS_REFERENCE = "publication_vids";

    public static final int PICK_IMAGE = 1;
    public static final int STORAGE_PERMISSION_CODE = 2;
    public static final int GPS_PERMISSION_CODE = 3;

    public static final String UBER_PACKAGE = "com.ubercab.driver";//com.app.janjan";//"";
    public static final String TAXIFY_PACKAGE = "ee.mtakso.driver";//"com.app.momcensor";
    public static final String MARCEL_PACKAGE = "com.classco.marcel";
    public static final String LE_CAB_PACKAGE = "fr.lecab.driver.android";
    public static final String ALLO_CAB_PACKAGE = "com.allocab.allocabpassenger";
    public static final String HEETCH_PACKAGE = "com.heetch";
    public static final String CHAUFFEUR_PRIVE = "com.chauffeurprive.driver";
    public static final String G7_TAXI_PACKAGE = "fr.taxisg7.affilies";

    public static final String[] driverApps = new String[]{MARCEL_PACKAGE, UBER_PACKAGE, TAXIFY_PACKAGE,
            LE_CAB_PACKAGE, ALLO_CAB_PACKAGE, HEETCH_PACKAGE, G7_TAXI_PACKAGE, CHAUFFEUR_PRIVE};

    private JsonObjectRequest getRequest;

    public static boolean isUserOnline(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Pref", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("online", false);
    }

    public static void setUserOnline(Context context, boolean state) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("online", state).apply();
    }

    public static User getCurrentUser(Context context) {
        try {
            Gson gson = new Gson();
            SharedPreferences sharedPreferences = context.getSharedPreferences("Pref", Context.MODE_PRIVATE);
            String json = sharedPreferences.getString("currentUser", "");
            return gson.fromJson(json, User.class);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static void setCurrentUser(Context context, User currentUser) {
        Gson gson = new Gson();
        SharedPreferences sharedPreferences = context.getSharedPreferences("Pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("currentUser", gson.toJson(currentUser));
        editor.apply();
    }

    public static void NetworkAlert(final Context context) {
        new LovelyInfoDialog(context)
                .setTopColorRes(R.color.colorAccent)
                .setIcon(R.drawable.ic_info_24dp)
                .setTitle(context.getResources().getString(R.string.no_connection))
                .setMessage(context.getResources().getString(R.string.check_connection))
                .show();
    }

    public static void updateLocalUser(final Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference users = database.getReference(FIREBASE_USERS_REFERENCE);

        users.child(getCurrentUser(context).getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                setCurrentUser(context, user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void updateOnlineUser(final Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference users = database.getReference(FIREBASE_USERS_REFERENCE);

        users.child(getCurrentUser(context).getUserId()).setValue(getCurrentUser(context));
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

    private static List<String> getSystemApps() throws IOException {
        ProcessBuilder builder = new ProcessBuilder("pm", "list", "packages", "-s");
        Process process = builder.start();

        InputStream in = process.getInputStream();
        Scanner scanner = new Scanner(in);
        Pattern pattern = Pattern.compile("^package:.+");
        int skip = "package:".length();

        List<String> systemApps = new ArrayList<>();
        while (scanner.hasNext(pattern)) {
            String pckg = scanner.next().substring(skip);
            systemApps.add(pckg);
        }

        scanner.close();
        process.destroy();
        return systemApps;
    }

    private static boolean isSystemApp(String packageName, List<String> systemApps) {
        return systemApps.contains(packageName);
    }

    public static boolean needPermissionForBlocking(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode != AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }

    public static String getForegroundProcess(Context context) {
        String topPackageName = null;
        UsageStatsManager usage = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> stats = usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
        if (stats != null) {
            SortedMap<Long, UsageStats> runningTask = new TreeMap<Long, UsageStats>();
            for (UsageStats usageStats : stats) {
                runningTask.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (runningTask.isEmpty()) {
                return null;
            }
            topPackageName = runningTask.get(runningTask.lastKey()).getPackageName();
        }
        if (topPackageName == null) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            context.startActivity(intent);
        }

        return topPackageName;
    }

    public static List<String> getRunningApps(Context context) {
        String topPackageName = null;
        List<String> runningApps = new ArrayList<>();
        UsageStatsManager usage = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> stats = usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - (1000 * 10000), time);
        if (stats != null) {
            SortedMap<Long, UsageStats> runningTask = new TreeMap<Long, UsageStats>();
            for (UsageStats usageStats : stats) {
                runningTask.put(usageStats.getLastTimeUsed(), usageStats);
                runningApps.add(runningTask.get(runningTask.lastKey()).getPackageName());
            }
            if (runningTask.isEmpty()) {
                return null;
            }
            topPackageName = runningTask.get(runningTask.lastKey()).getPackageName();
        }

        return runningApps;
    }

    public static List<App> getInstalledApps(Context context) {

        List<PackageInfo> apps = context.getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);
        List<App> appList = new ArrayList<>();

        for (int i = 0; i < apps.size(); i++) {
            PackageInfo p = apps.get(i);
            if (!isSystemPackage(p)) {
                for (int j = 0; j < driverApps.length; j++) {
                    if (driverApps[j].equals(p.packageName)) {
                        String name = p.applicationInfo.loadLabel(context.getPackageManager()).toString();
                        String packageName = p.packageName;
                        App app = new App(name, packageName, false);
                        Log.i("installedApps", app.getName());
                        appList.add(app);
                    }
                }
            }
        }
        return appList;
    }

    public static List<App> getAllInstalledApps(Context context) {

        List<PackageInfo> apps = context.getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);
        List<App> appList = new ArrayList<>();

        for (int i = 0; i < apps.size(); i++) {
            PackageInfo p = apps.get(i);
            if (!isSystemPackage(p) && !p.packageName.equals(context.getPackageName())) {
                String name = p.applicationInfo.loadLabel(context.getPackageManager()).toString();
                String packageName = p.packageName;
                App app = new App(name, packageName, false);
                appList.add(app);
            }
        }
        return appList;
    }

    private static boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public static void launchApps(Context context, List<App> appList) {
        for (App app : appList) {
            try {
                Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(app.getPackageName());
                LaunchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(LaunchIntent);
            } catch (RuntimeException e) {

            }
        }
    }

    public static void exitAllAppsFromSwitch(final Context context, List<App> appList, final String activity, final String appPackage) {
        try {
            ((Activity)context).finish();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        context.startService(new Intent(context, CloseAppsService.class));

        List<String> runningAppsList = getRunningApps(context);
        for(final App app : appList) {
            //if(runningAppsList.contains(app.getPackageName())) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.setData(Uri.parse("package:" + app.getPackageName()));
                context.startActivity(intent);
                }
            }, 500);

           //}
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("running", getRunningApps(context).toString());
                if(activity.equals("main"))
                    context.startActivity(new Intent(context, MainActivity.class).putExtra("annonce", true));
                else if(activity.equals("app")) {
                    Config.onCourseState = true;
                    Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(appPackage);
                    LaunchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(LaunchIntent);
                }
            }
        }, Config.getActivatedAppList(context).size() *  2300);
    }

    public static void exitAllAppsFromWidget(final Context context, List<App> appList, final String activity, final String appPackage) {
        try {
            ((Activity)context).finish();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        context.startService(new Intent(context, CloseAppsService.class));

        List<String> runningAppsList = getRunningApps(context);
        for(final App app : appList) {
            //if(runningAppsList.contains(app.getPackageName())) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.setData(Uri.parse("package:" + app.getPackageName()));
                    context.startActivity(intent);
                }
            }, 500);
            context.stopService(new Intent(context, WidgetService.class));
            //}
        }
    }

    public static boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/" + MyAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static List<String> getAllCountries(Context context){
        String text = "";
        try{
            InputStream inputStream = context.getAssets().open("countriestocities.txt");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            text = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> countries = new ArrayList<>();

        try {
            JSONObject object = new JSONObject(text);
            JSONArray array = object.names();
            Log.i("countries", array.toString());
            for(int i = 0; i< array.length(); i++) {
                if(!array.get(i).toString().equals(""))
                countries.add(array.get(i).toString());
            }

            /*for(int i = 0; i < object.length(); i++) {
                Log.i("countries", new JSONArray(object.))
            }*/
            Log.i("tagged", object.getJSONArray("China").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return countries;
    }

    public static List<String> getAllCities(Context context, String country) {
        String text = "";
        List<String> cities = new ArrayList<>();
        try{
            InputStream inputStream = context.getAssets().open("countriestocities.txt");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            text = new String(buffer);
            JSONObject object = new JSONObject(text);
            JSONArray citiesJSON = object.getJSONArray(country);
            for(int i = 0; i < citiesJSON.length(); i++) {
                if(!citiesJSON.get(i).toString().isEmpty())
                    cities.add(citiesJSON.get(i).toString());
            }
        } catch (IOException | JSONException ignored) {

        }
        return cities;
    }

    public static String epochToDate(long time, String formatString) {
        Log.i("timezone", Locale.getDefault().toString());
        SimpleDateFormat format = new SimpleDateFormat(formatString, Locale.getDefault());
        return format.format(new Date(time));

    }

    public static void setAppList(Context context, List<App> AppList){
        Gson gson = new Gson();
        String jsonApps = gson.toJson(AppList);
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("jsonApps", jsonApps).apply();
    }

    public static List<App> getAppList(Context context) {
        List<App> apps;
        SharedPreferences mPrefs = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("jsonApps", "");
        if (json.isEmpty()) {
            apps = new ArrayList<App>();
        } else {
            Type type = new TypeToken<List<App>>() {
            }.getType();
            apps = gson.fromJson(json, type);
        }
        return apps;
    }

    public static List<App> getActivatedAppList(Context context) {
        List<App> appList = new ArrayList<>();
        for(App app : getAppList(context)) {
            if(app.isActivated())
                appList.add(app);
        }
        return appList;
    }

    public static void setClosestAirport(Context context) {

        String coordinatesUrl;
        String APIKey = "AIzaSyB38uaewC1ygycX6P6SCHBMcGCkGMmNs-Q";//"AIzaSyDclUOxWsuzUmPlhWE8RWC4ylOOu9s4gjI";
        coordinatesUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=" + getCurrentUser(context).getCity() + "&key=" + APIKey;

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, coordinatesUrl, null,
                response -> {
                    // display response

                    try {
                        JSONObject jsonObject = new JSONObject(response.toString());
                        JSONArray results = jsonObject.getJSONArray("results");
                        JSONObject resultsObject  = results.getJSONObject(0);
                        JSONObject geometryObject = resultsObject.getJSONObject("geometry");
                        JSONObject locationObject = geometryObject.getJSONObject("location");
                        String longitude = locationObject.get("lng").toString();
                        String latitude = locationObject.get("lat").toString();

                        Log.i("response", "lng: " + longitude + "  lat: " + latitude);
                        String airportUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude +"," +  longitude + "&rankby=distance&type=airport&key=" + APIKey;
                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, airportUrl, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("response", response.toString());



                            }
                        }, error -> {
                        });
                        queue.add(request);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.d("Error.Response", error.getMessage())
        );
        queue.add(getRequest);

    }

    public static void setAirportList(Context context, String city, String country) {

        String text = "";

        List<Airport> airportList = new ArrayList<>();

        try{
            InputStream inputStream = context.getAssets().open("airports.txt");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            text = new String(buffer);
            JSONArray array = new JSONArray(text);
            for(int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                if(     jsonObject.getString("country").equals(country) &&
                        jsonObject.getString("type").equals("Airports") &&
                        !jsonObject.getString("icao").isEmpty() &&
                        jsonObject.getString("city").equals(city)) {
                    Airport airport = new Airport(jsonObject.getString("name"), jsonObject.getString("icao"));
                    Log.i("airports", airport.getName() + "   " + airport.getCode());
                    airportList.add(airport);
                }
            }
        } catch (IOException | JSONException ignored) {
        }

        Collections.sort(airportList, (o1, o2) -> o1.getName().compareTo(o2.getName()));

        Gson gson = new Gson();
        String jsonApps = gson.toJson(airportList);
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("jsonAirports", jsonApps).apply();

    }

    public static List<Airport> getAirportName(Context context) {
        List<Airport> airportsList;
        SharedPreferences mPrefs = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("jsonAirports", "");
        if (json.isEmpty()) {
            airportsList = new ArrayList<Airport>();
        } else {
            Type type = new TypeToken<List<Airport>>() {
            }.getType();
            airportsList = gson.fromJson(json, type);
        }
        return airportsList;
    }

    public static String toIATACode(Context context, String code) {
        String text = "";
        try{
            InputStream inputStream = context.getAssets().open("airports.txt");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            text = new String(buffer);
            JSONArray array = new JSONArray(text);
            for(int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                if(jsonObject.getString("icao").equals(code)) {
                    return jsonObject.getString("code");
                }
            }
        } catch (IOException | JSONException | NullPointerException ignored) {
        }
        return null;
    }


    public static boolean containsUrl(String content) {
        final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

        Pattern p = Pattern.compile(URL_REGEX);
        Matcher m = p.matcher(content);//replace with string to compare
        return m.find();
    }

    public static String getUrlFromString(String content) {
        final Pattern urlPattern = Pattern.compile(
                "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                        + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                        + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = urlPattern.matcher(content);
        while (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            // now you have the offsets of a URL match
            return content.substring(matchStart, matchEnd);
        }
        return null;
    }

    public static String getVideoId(String ytUrl) {
        String vId = null;
        Pattern pattern = Pattern.compile(
                "^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(ytUrl);
        if (matcher.matches()){
            vId = matcher.group(1);
        }
        return vId;
    }

    public final static String youTubeUrlRegEx = "^(https?)?(://)?(www.)?(m.)?((youtube.com)|(youtu.be))/";
    public final static String[] videoIdRegex = { "\\?vi?=([^&]*)","watch\\?.*v=([^&]*)", "(?:embed|vi?)/([^/?]*)", "^([A-Za-z0-9\\-]*)"};

    public static String getVideoIdFromUrl(String url) {
        String youTubeLinkWithoutProtocolAndDomain = youTubeLinkWithoutProtocolAndDomain(url);

        for(String regex : videoIdRegex) {
            Pattern compiledPattern = Pattern.compile(regex);
            Matcher matcher = compiledPattern.matcher(youTubeLinkWithoutProtocolAndDomain);

            if(matcher.find()){
                return matcher.group(1);
            }
        }

        return null;
    }

    public static String youTubeLinkWithoutProtocolAndDomain(String url) {
        Pattern compiledPattern = Pattern.compile(youTubeUrlRegEx);
        Matcher matcher = compiledPattern.matcher(url);

        if(matcher.find()){
            return url.replace(matcher.group(), "");
        }
        return url;
    }

}
