package com.xiaoyu.BaZi.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.loopj.android.http.*;
import com.xiaoyu.BaZi.background.config.AppInfo;
import com.xiaoyu.BaZi.background.config.ServerConfig;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Request {
	public static final String POST = "post";
	public static final String GET = "get";
	private static AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
	private static List<AsyncHttpResponseHandler> asyncHttpResponseHandlers = new ArrayList<AsyncHttpResponseHandler>();

	/**
	 * 回调接口
	 */
	public static abstract class RequestListener {

		public abstract void onException(RequestException e);

		public abstract void onComplete(String response);
		public void transferred(float progress) {
		}
	}

	/**
	 * 文件下载回掉
	 */
	public static abstract class FileDownListener {
		public abstract void onDownException(RequestException e);

		public abstract void onDownComplete(String filePath);

		public void onStart(long requestId) {
		}
	}

	public static String getAbsoluteUrl(String relativeUrl) {
		return ServerConfig.BASE_URL + relativeUrl;
	}

	private static RequestParams buildRequestParams(List<NameValuePair> params, String url) {
		params.add(new BasicNameValuePair("client_id", AppInfo.clientid + ""));
		params.add(new BasicNameValuePair("cver", AppInfo.cver));
		params.add(new BasicNameValuePair("qudaoid", AppInfo.qudao_code));
		params.add(new BasicNameValuePair("via", AppInfo.via));
		params.add(new BasicNameValuePair("app", AppInfo.app));
		params.add(new BasicNameValuePair("uuid", AppInfo.uuid));
		params.add(new BasicNameValuePair("imei", AppInfo.imei));
		params.add(new BasicNameValuePair("mac", AppInfo.mac));
		params.add(new BasicNameValuePair("ver", AppInfo.ver));
		return createRequestParams(params);
	}

	protected static RequestParams createRequestParams(List<NameValuePair> params) {
		RequestParams requestParams = new RequestParams();
		if (params != null) {
			for (int i = 0; i < params.size(); i++) {
				NameValuePair nameValuePair = params.get(i);
				requestParams.add(nameValuePair.getName(), nameValuePair.getValue());
			}
		}
		return requestParams;
	}

	public static void cancelRequest(Context context) {
		asyncHttpClient.cancelRequests(context, true);
	}

	public static void doRequest(Context context,List<NameValuePair> params, final String url,String method, final RequestListener requestListener) {
		RequestParams requestParams = buildRequestParams(params, url);
		if (Debug.DEBUG) {
			Debug.info("url:" + getAbsoluteUrl(url));
			String key = "";
			for (NameValuePair pair : params) {
				key += "&" + pair.getName() + "=" + pair.getValue();
			}
			Debug.info("where:" + key);
			int length = key.length();
			Debug.info("NEWHTTP", getAbsoluteUrl(url) + "?" + key.substring(1, length));
		}

		TextHttpResponseHandler textHttpResponseHandler = new TextHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseBody) {
				if (requestListener != null) {
					requestListener.onComplete(responseBody);
				}
				if (asyncHttpResponseHandlers.contains(this)) {
					asyncHttpResponseHandlers.remove(this);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				if (requestListener != null) {
					requestListener.onException(new RequestException(responseString));
				}
				if (asyncHttpResponseHandlers.contains(this)) {
					asyncHttpResponseHandlers.remove(this);
				}
			}
		};

		if (!asyncHttpResponseHandlers.contains(textHttpResponseHandler)) {
			asyncHttpResponseHandlers.add(textHttpResponseHandler);
		}
		if (POST.equals(method)) {
			asyncHttpClient.post(context,getAbsoluteUrl(url), requestParams, textHttpResponseHandler);
		} else if (GET.equals(method)) {
			asyncHttpClient.get(context,getAbsoluteUrl(url), requestParams, textHttpResponseHandler);
		} else {
			asyncHttpClient.post(context,getAbsoluteUrl(url), requestParams, textHttpResponseHandler);
		}
	}

	public static void doFileDown(Context context,final String url, final String downDir,
			final String fileName, final FileDownListener fileDownListener) {
		final File file = new File(downDir, fileName);
		FileAsyncHttpResponseHandler fileAsyncHttpResponseHandler = new FileAsyncHttpResponseHandler(file) {
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable e, File response) {
				if (fileDownListener != null) {
					fileDownListener.onDownException(new RequestException(e
							.getMessage(), statusCode));
				}
				if (asyncHttpResponseHandlers.contains(this)) {
					asyncHttpResponseHandlers.remove(this);
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, File file) {
				if (fileDownListener != null) {
					fileDownListener.onDownComplete(file.getAbsolutePath());
				}
				if (asyncHttpResponseHandlers.contains(this)) {
					asyncHttpResponseHandlers.remove(this);
				}
			}

		};
		asyncHttpClient.get(context,url, fileAsyncHttpResponseHandler);
		if (!asyncHttpResponseHandlers.contains(fileAsyncHttpResponseHandler)) {
			asyncHttpResponseHandlers.add(fileAsyncHttpResponseHandler);
		}
	}

	/**
	 * 网络状况检查
	 *
	 * @param context
	 * @param needToast 是否需要Toast提示
	 * @return isConnect 是否联网
	 * @author yanbing.ye
	 */
	public static boolean isNetworkAvailable(Context context, boolean needToast) {
		boolean isConnect = false;
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info != null && info.isConnected()) {
				if (info.getState() == NetworkInfo.State.CONNECTED) {
					isConnect = true;
				}
			}
		}
		return isConnect;
	}

	public static class RequestException extends Exception {

		private static final long serialVersionUID = 1L;
		public static final int INVALID_CODE = -1;
		public int statusCode = INVALID_CODE;
		private String mResponse;

		public RequestException(String detailMessage, Throwable throwable) {
			super(detailMessage, throwable);
		}

		public RequestException(String detailMessage) {
			super(detailMessage);
			mResponse = detailMessage;
		}

		public RequestException(String detailMessage, int code) {
			super(detailMessage);
			mResponse = detailMessage;
			statusCode = code;
		}

		public RequestException(Throwable throwable) {
			super(throwable);
		}

		public RequestException() {
			super();
		}

		public int getStatusCode() {
			return statusCode;
		}

		public String getResponse() {
			return mResponse;
		}
	}
}
