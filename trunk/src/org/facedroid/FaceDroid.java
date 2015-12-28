package org.facedroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.webkit.CookieSyncManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class FaceDroid extends Activity {
	private static final String TAG = "FaceDroid";

	private WebView _homeweb;
	private WebView _profile;
	private WebView _friends;
	private WebView _inbox;
	private NotificationManager _notificationManager;

	private ConditionVariable mCondition;
	private final static int NOTIFICATION_ID = R.layout.main;

	// URL to the host that acts as a proxy to conncect to facebook
	private final static String HOST_URL = "http://84.123.66.77:8080";

	/** Called when the activity is first created. */

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.main);
		initNotifications();
		initComponents();
		showLoginDialog();
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		_notificationManager.cancel(NOTIFICATION_ID);
		// Stop the thread from generating further notifications
		mCondition.open();
	}

	private void initNotifications() {
		_notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Start up the thread running the service. Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.
		Thread notifyingThread = new Thread(null, mTask, "NotifyingService");
//		mCondition = new ConditionVariable(false);
		notifyingThread.start();
	}

	private void showLoginDialog() {
		WebView loginweb = new WebView(this);
		setUpWebView(loginweb);
		loginweb.loadUrl(HOST_URL + "/fbdroid2.html");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(loginweb);
		// builder.setMessage("Test alert dialog");
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void initComponents() {
		TabHost tabhost = (TabHost) this.findViewById(android.R.id.tabhost);
		tabhost.setup();

		TabHost.TabSpec home = tabhost.newTabSpec("Home");
		home.setIndicator("Home");
		home.setContent(R.id.home);
		tabhost.addTab(home);

		TabHost.TabSpec profile = tabhost.newTabSpec("Profile");
		profile.setIndicator("Profile");
		profile.setContent(R.id.profile);
		tabhost.addTab(profile);

		TabHost.TabSpec friends = tabhost.newTabSpec("Friends");
		friends.setIndicator("Friends");
		friends.setContent(R.id.friends);
		tabhost.addTab(friends);

		TabHost.TabSpec inbox = tabhost.newTabSpec("Inbox");
		inbox.setIndicator("Inbox");
		inbox.setContent(R.id.inbox);
		tabhost.addTab(inbox);

		tabhost.setCurrentTab(0);
		_homeweb = (WebView) this.findViewById(R.id.home);
		_profile = (WebView) this.findViewById(R.id.profile);
		_friends = (WebView) this.findViewById(R.id.friends);
		_inbox = (WebView) this.findViewById(R.id.inbox);
		setUpWebView(_homeweb);
		setUpWebView(_profile);
		setUpWebView(_friends);
		setUpWebView(_inbox);
		_homeweb.loadUrl(HOST_URL + "/home.html");
		_profile.loadUrl(HOST_URL + "/profile.html");
		_friends.loadUrl(HOST_URL + "friends.html");
		_inbox.loadUrl(HOST_URL + "/inbox.html");

		tabhost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String arg0) {
				// if(arg0.equalsIgnoreCase("friends")) _friends.reload();

			}

		});
	}

	private void setUpWebView(final WebView webview) {
		WebSettings wb = webview.getSettings();
		wb.setJavaScriptEnabled(true);
		wb.setJavaScriptCanOpenWindowsAutomatically(true);
		wb.setLoadsImagesAutomatically(true);
		wb.setPluginsEnabled(true);
		wb.setSupportMultipleWindows(true);
		wb.setNavDump(true);

		webview.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onCreateWindow(WebView view, boolean dialog,
					boolean userGesture, Message resultMsg) {
				resultMsg.obj = webview;
				resultMsg.sendToTarget();
				Log.e(TAG, "new window created");
				return true;
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message,
					String defaultValue, JsPromptResult result) {
				Log.e(TAG, "JSPrompt:" + message + "-" + defaultValue);
				return false;
			}

			@Override
			public boolean onJsConfirm(WebView view, String url,
					String message, JsResult result) {
				Log.e(TAG, "JSConfirm:" + message);
				return false;
			}

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
						newProgress * 100);
				if (newProgress == 100) {
					CookieSyncManager.getInstance().sync();
				}
			}
		});

		webview.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				Log.e(TAG, "Loading=" + url);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				Log.e(TAG, "Finished=" + url);
			}
		});

	}

	private void showNotification(String text) {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification

		// Set the icon, scrolling text and timestamp.
		Notification notification = new Notification(0, null, System
				.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, FaceDroid.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, "notificacion", text,
				contentIntent);

		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to
		// cancel.
		_notificationManager.notify(NOTIFICATION_ID, notification);
	}

	private Runnable mTask = new Runnable() {
		public void run() {
			showNotification("Facedroid notification");
		}
	};

}