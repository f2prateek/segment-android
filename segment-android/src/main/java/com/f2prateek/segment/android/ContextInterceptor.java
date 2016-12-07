package com.f2prateek.segment.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import com.f2prateek.segment.model.Message;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Future;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;
import static android.content.pm.PackageManager.FEATURE_TELEPHONY;
import static android.net.ConnectivityManager.TYPE_BLUETOOTH;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.provider.Settings.Secure.ANDROID_ID;
import static android.provider.Settings.Secure.getString;
import static com.f2prateek.segment.android.Utils.getSystemService;
import static com.f2prateek.segment.android.Utils.hasFeature;
import static com.f2prateek.segment.android.Utils.hasPermission;
import static com.f2prateek.segment.android.Utils.isNullOrEmpty;

public class ContextInterceptor implements Interceptor {
  private static final Map<String, Object> LIBRARY_CONTEXT;
  private static final Map<String, Object> OS_CONTEXT;

  private final Context application;

  static {
    Map<String, Object> library = new LinkedHashMap<>();
    library.put("name", "segment-android");
    library.put("version", BuildConfig.VERSION_NAME);
    LIBRARY_CONTEXT = Collections.unmodifiableMap(library);

    Map<String, Object> os = new LinkedHashMap<>();
    os.put("name", "Android");
    os.put("version", Build.VERSION.RELEASE);
    OS_CONTEXT = Collections.unmodifiableMap(os);
  }

  public static Interceptor with(Context context) {
    return new ContextInterceptor(context);
  }

  private ContextInterceptor(Context context) {
    this.application = context;
  }

  @Nullable @Override public Future<Message> intercept(Chain chain) {
    Message message = chain.message();
    Message.Builder builder = message.toBuilder();

    Map<String, Object> newContext = new LinkedHashMap<>();
    app(newContext);
    device(newContext);
    network(newContext);
    screen(newContext);
    newContext.put("library", LIBRARY_CONTEXT);
    newContext.put("os", OS_CONTEXT);
    newContext.put("userAgent", System.getProperty("http.agent"));
    newContext.put("timezone", TimeZone.getDefault().getID());
    Locale locale = Locale.getDefault();
    newContext.put("locale", locale.getLanguage() + "-" + locale.getCountry());

    Map<String, Object> context = message.context();
    if (!isNullOrEmpty(context)) {
      newContext.putAll(context);
    }

    return chain.proceed(builder.context(newContext).build());
  }

  private void app(Map<String, Object> context) {
    try {
      PackageManager packageManager = application.getPackageManager();
      PackageInfo packageInfo = packageManager.getPackageInfo(application.getPackageName(), 0);
      Map<String, Object> app = new LinkedHashMap<>();
      app.put("name", packageInfo.applicationInfo.loadLabel(packageManager));
      app.put("version", packageInfo.versionName);
      app.put("build", packageInfo.versionCode);
      context.put("app", app);
    } catch (PackageManager.NameNotFoundException e) {
      throw new AssertionError(e);
    }
  }

  private void device(Map<String, Object> context) {
    Map<String, Object> device = new LinkedHashMap<>();
    device.put("id", getDeviceId(application));
    device.put("manufacturer", Build.MANUFACTURER);
    device.put("model", Build.MODEL);
    device.put("name", Build.DEVICE);
    context.put("device", device);
  }

  private void screen(Map<String, Object> context) {
    Map<String, Object> screen = new LinkedHashMap<>();
    WindowManager manager = getSystemService(application, Context.WINDOW_SERVICE);
    Display display = manager.getDefaultDisplay();
    DisplayMetrics displayMetrics = new DisplayMetrics();
    display.getMetrics(displayMetrics);
    screen.put("density", displayMetrics.density);
    screen.put("height", displayMetrics.heightPixels);
    screen.put("width", displayMetrics.widthPixels);
    context.put("screen", screen);
  }

  /** Creates a unique device id. */
  static String getDeviceId(Context context) {
    @SuppressLint("HardwareIds") String androidId =
        getString(context.getContentResolver(), ANDROID_ID);
    if (!isNullOrEmpty(androidId)) {
      return androidId;
    }

    // Serial number, guaranteed to be on all non phones in 2.3+.
    if (!isNullOrEmpty(Build.SERIAL)) {
      return Build.SERIAL;
    }

    // Telephony ID, guaranteed to be on all phones, requires READ_PHONE_STATE permission.
    if (hasPermission(context, READ_PHONE_STATE) && hasFeature(context, FEATURE_TELEPHONY)) {
      TelephonyManager telephonyManager = getSystemService(context, TELEPHONY_SERVICE);
      @SuppressLint("HardwareIds") String telephonyId = telephonyManager.getDeviceId();
      if (!isNullOrEmpty(telephonyId)) {
        return telephonyId;
      }
    }

    // If this still fails, generate random identifier that does not persist across installations.
    return UUID.randomUUID().toString();
  }

  private void network(Map<String, Object> context) {
    Map<String, Object> network = new LinkedHashMap<>();
    if (hasPermission(application, ACCESS_NETWORK_STATE)) {
      ConnectivityManager connectivityManager = getSystemService(application, CONNECTIVITY_SERVICE);
      if (connectivityManager != null) {
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(TYPE_WIFI);
        network.put("wifi", wifiInfo != null && wifiInfo.isConnected());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
          NetworkInfo bluetoothInfo = connectivityManager.getNetworkInfo(TYPE_BLUETOOTH);
          network.put("bluetooth", bluetoothInfo != null && bluetoothInfo.isConnected());
        }
        NetworkInfo cellularInfo = connectivityManager.getNetworkInfo(TYPE_MOBILE);
        network.put("cellular", cellularInfo != null && cellularInfo.isConnected());
      }
    }

    TelephonyManager telephonyManager = getSystemService(application, TELEPHONY_SERVICE);
    if (telephonyManager != null) {
      network.put("carrier", telephonyManager.getNetworkOperatorName());
    } else {
      network.put("carrier", "unknown");
    }

    context.put("network", network);
  }
}
