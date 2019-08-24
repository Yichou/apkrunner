package com.apkrunner.launcher;

import java.util.ArrayList;

import com.edroid.common.base.BaseListFragment;
import com.apkrunner.launcher.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class BaseApksFragment2 extends BaseListFragment {
	static final class ApkItem {
		String path;
		String fileName;
		String appName;
		Drawable icon;
		String pkg;
		String size;
		String ver;
		boolean installed;
	}

	static int iconSize;

	protected ArrayList<ApkItem> apkList = new ArrayList<ApkItem>(128);
	protected Bitmap defIcon;
	protected MyAdapter adapter;
	protected int pressIndex;
//	Handler mHandler = new Handler(this);
	
	
	
	public static Bitmap createScaledBitmap(Resources res, int resId) {
		Bitmap tmpBitmap = BitmapFactory.decodeResource(res, resId);
		if(tmpBitmap.getWidth() == iconSize)
			return tmpBitmap;
		
		Bitmap defIcon = Bitmap.createScaledBitmap(tmpBitmap, iconSize, iconSize, true);
		tmpBitmap.recycle();
		
		return defIcon;
	}
	
	protected abstract void onLoad(ArrayList<ApkItem> list);
	
//	static boolean loadSys = false;
//	
//	private void load() {

//	}
	
	private void clear() {
//		for(ApkItem info : apkList) {
//			if(info.icon != null)
//				info.icon.recycle();
//		}
		apkList.clear();
	}
	
	@SuppressWarnings("deprecation")
	private void showNotify2() {
		Intent intent = new Intent(getActivity(), MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		Notification n = new Notification(android.R.drawable.ic_menu_share,
				null, System.currentTimeMillis());
		
		PendingIntent contentIntent = PendingIntent.getActivity(getActivity(), 
				0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		n.setLatestEventInfo(getActivity(), "ApkRunner 正在运行", "点击返回", contentIntent);
		n.flags |= Notification.FLAG_NO_CLEAR;
		
		NotificationManager mNM = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
		mNM.notify(android.R.drawable.ic_menu_share, n);
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		iconSize = getResources().getDimensionPixelSize(R.dimen.icon_size);
		defIcon = createScaledBitmap(getResources(), R.drawable.ic_launcher);
		adapter = new MyAdapter();
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		setListAdapter(adapter);
		getListView().setOnItemLongClickListener(this);
		registerForContextMenu(getListView());
		
		reload();
//		showNotify2();
	}
	
	void reload() {
		clear();
		onLoad(apkList);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onRefresh() {
		reload();
		setRefreshing(false);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		clear();
		defIcon.recycle();
//		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		pressIndex = position;
		return false;
	}
	
	class MyAdapter extends BaseAdapter {
		
		@Override
		public int getCount() {
			return apkList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View cacheView, ViewGroup parent) {
			Context context = getActivity();
			
			ViewHolder holder = null;
			ApkItem info = apkList.get(position);
			
			if(cacheView == null) {
				holder = new ViewHolder();
				
				LayoutInflater inflater = LayoutInflater.from(context);
				
				cacheView = inflater.inflate(R.layout.list_item, null);
				
				holder.ver = (TextView) cacheView.findViewById(R.id.ver);
				holder.textView1 = (TextView) cacheView.findViewById(R.id.textView1);
				holder.textView2 = (TextView) cacheView.findViewById(R.id.textView2);
				holder.textView3 = (TextView) cacheView.findViewById(R.id.textView3);
				holder.textView4 = (TextView) cacheView.findViewById(R.id.textView4);
				holder.imageView = (ImageView) cacheView.findViewById(R.id.imageView1);
				cacheView.setTag(holder);
			} else {
				holder = (ViewHolder) cacheView.getTag();
			}
			
			holder.imageView.setImageDrawable(info.icon);
			holder.textView1.setText(info.appName);
			holder.textView2.setText(info.pkg);
			holder.textView3.setText(info.fileName);
			holder.textView4.setText(info.size);
			holder.ver.setText(info.ver);
			
			return cacheView;
		}
	}

	static class ViewHolder {
		TextView textView1;
		TextView textView2;
		TextView textView3;
		TextView textView4;
		TextView ver;
		ImageView imageView;
	}
}
