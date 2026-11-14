package com.roohu.clipforge;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.webkit.JavascriptInterface;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.PermissionRequest;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.webkit.WebViewAssetLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ComponentActivity {
    private static final String MAIN_URL = "https://appassets.androidplatform.net/assets/Roohu-ClipForge.html";
    private static final long MIN_SPLASH_MS = 900;

    private WebView web;
    private View splash, glow, logo, title, tag, loader;
    private ValueCallback<Uri[]> chooserCb;
    private boolean splashGone = false;
    private long t0;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private final List<Animator> loops = new ArrayList<>();

    private final ActivityResultLauncher<Intent> chooser = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), r -> {
                if (chooserCb == null) return;
                Uri[] res = WebChromeClient.FileChooserParams.parseResult(r.getResultCode(), r.getData());
                chooserCb.onReceiveValue(res);
                chooserCb = null;
            });

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        t0 = SystemClock.elapsedRealtime();
        web = findViewById(R.id.web);
        splash = findViewById(R.id.splash);
        glow = findViewById(R.id.glow);
        logo = findViewById(R.id.logo);
        title = findViewById(R.id.title);
        tag = findViewById(R.id.tag);
        loader = findViewById(R.id.loader);
        startSplash();
        setupWeb();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                web.evaluateJavascript("(window.cfBack&&cfBack())?'1':'0'", v -> {
                    if ("\"1\"".equals(v)) return;
                    if (web.canGoBack()) { web.goBack(); return; }
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                });
            }
        });
        web.loadUrl(MAIN_URL);
        ui.postDelayed(this::hideSplash, 8000); // failsafe: never trap the user behind the splash
    }

    private void setupWeb() {
        final WebViewAssetLoader assets = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);
        s.setSupportZoom(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.setBackgroundColor(0xFF0A1A1F);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        web.addJavascriptInterface(new Bridge(), "AndroidBridge");
        web.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                return assets.shouldInterceptRequest(r.getUrl());
            }
            @Override public void onPageFinished(WebView v, String u) {
                long left = Math.max(0, MIN_SPLASH_MS - (SystemClock.elapsedRealtime() - t0));
                ui.postDelayed(MainActivity.this::hideSplash, left);
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if ("appassets.androidplatform.net".equals(u.getHost())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) { }
                return true;
            }
            @Override public boolean onRenderProcessGone(WebView v, RenderProcessGoneDetail d) {
                runOnUiThread(MainActivity.this::recreate);
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (chooserCb != null) chooserCb.onReceiveValue(null);
                chooserCb = cb;
                Intent i = p.createIntent();
                for (String a : p.getAcceptTypes()) {
                    if (a != null && a.startsWith(".")) { i.setType("*/*"); break; }
                }
                try { chooser.launch(i); }
                catch (Exception e) { chooserCb = null; cb.onReceiveValue(null); toast("No file picker is available on this device."); }
                return true;
            }
            @Override public void onPermissionRequest(PermissionRequest r) { r.deny(); }
        });
    }

    private void startSplash() {
        logo.setAlpha(0f); logo.setScaleX(0.8f); logo.setScaleY(0.8f);
        title.setAlpha(0f); title.setTranslationY(24f); tag.setAlpha(0f); loader.setAlpha(0f);
        logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(600).setInterpolator(new DecelerateInterpolator()).start();
        title.animate().alpha(1f).translationY(0f).setStartDelay(250).setDuration(500).start();
        tag.animate().alpha(1f).setStartDelay(450).setDuration(500).start();
        loader.animate().alpha(1f).setStartDelay(600).setDuration(400).start();
        ObjectAnimator g = ObjectAnimator.ofPropertyValuesHolder(glow,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.35f, 0.95f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0.9f, 1.15f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.9f, 1.15f));
        g.setDuration(1400); g.setRepeatMode(ValueAnimator.REVERSE); g.setRepeatCount(ValueAnimator.INFINITE);
        g.setInterpolator(new AccelerateDecelerateInterpolator()); g.start(); loops.add(g);
    }

    private void hideSplash() {
        if (splashGone) return;
        splashGone = true;
        splash.animate().alpha(0f).setDuration(350).withEndAction(() -> {
            splash.setVisibility(View.GONE);
            for (Animator a : loops) a.cancel();
            loops.clear();
        }).start();
    }

    private void toast(String m) { runOnUiThread(() -> Toast.makeText(this, m, Toast.LENGTH_LONG).show()); }

    /** Receives exported clips from the page in chunks and writes them to Downloads/RoohuClipForge. */
    private class Bridge {
        private OutputStream os; private Uri uri; private File file; private String shown = "";

        @JavascriptInterface public synchronized boolean begin(String fn, String mime) {
            closeQuiet(true);
            try {
                String name = fn == null ? "clip" : fn.replaceAll("[^A-Za-z0-9._ -]", "_");
                if (name.isEmpty()) name = "clip";
                if (Build.VERSION.SDK_INT >= 29) {
                    ContentValues v = new ContentValues();
                    v.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                    v.put(MediaStore.MediaColumns.MIME_TYPE, mime == null || mime.isEmpty() ? "application/octet-stream" : mime);
                    v.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/RoohuClipForge");
                    v.put(MediaStore.MediaColumns.IS_PENDING, 1);
                    uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                    if (uri == null) return false;
                    os = getContentResolver().openOutputStream(uri);
                    shown = "Downloads/RoohuClipForge/" + name;
                } else {
                    File dir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
                    if (dir == null) return false;
                    file = new File(dir, name);
                    os = new FileOutputStream(file);
                    shown = file.getAbsolutePath();
                }
                return os != null;
            } catch (Exception e) { closeQuiet(true); return false; }
        }
        @JavascriptInterface public synchronized boolean chunk(String b64) {
            try { os.write(Base64.decode(b64, Base64.DEFAULT)); return true; }
            catch (Exception e) { closeQuiet(true); return false; }
        }
        @JavascriptInterface public synchronized boolean end() {
            try {
                os.flush(); os.close(); os = null;
                if (uri != null) {
                    ContentValues v = new ContentValues();
                    v.put(MediaStore.MediaColumns.IS_PENDING, 0);
                    getContentResolver().update(uri, v, null, null);
                }
                toast("Saved to " + shown);
                uri = null; file = null;
                return true;
            } catch (Exception e) { closeQuiet(true); return false; }
        }
        @JavascriptInterface public synchronized void abort() { closeQuiet(true); }

        private void closeQuiet(boolean deletePartial) {
            try { if (os != null) os.close(); } catch (Exception ignored) { }
            os = null;
            try {
                if (deletePartial) {
                    if (uri != null) getContentResolver().delete(uri, null, null);
                    if (file != null) file.delete();
                }
            } catch (Exception ignored) { }
            uri = null; file = null;
        }
    }

    @Override protected void onResume() { super.onResume(); if (web != null) web.onResume(); }
    @Override protected void onPause() { if (web != null) web.onPause(); super.onPause(); }
    @Override protected void onDestroy() {
        ui.removeCallbacksAndMessages(null);
        for (Animator a : loops) a.cancel();
        if (web != null) {
            ViewGroup p = (ViewGroup) web.getParent();
            if (p != null) p.removeView(web);
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}
