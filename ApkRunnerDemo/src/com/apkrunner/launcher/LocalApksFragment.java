package com.apkrunner.launcher;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import com.apkrunner.ApkRunner;
import com.edroid.common.utils.ApkUtils;
import com.edroid.common.utils.FileUtils;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class LocalApksFragment extends BaseApksFragment2 {
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
//		File file = new File(ROOT, apkList.get(position).fileName);
		
		startActivity(new Intent(getActivity(), LoadingActivity.class)
				.setData(Uri.fromFile(new File(apkList.get(position).path)))
				.setAction("mutil"));
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(0, 0, 0, "安装应用");
		menu.add(0, 1, 1, "清除数据");
		menu.add(0, 2, 2, "删除APK");
		menu.add(0, 3, 3, "run in main proc");
		menu.add(0, 4, 4, "run in process");
		menu.add(0, 6, 6, "导出签名");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final File file = new File(MainActivity.ROOT, apkList.get(pressIndex).fileName);
		
		if(item.getItemId() == 0) {
			Intent intent = ApkUtils.getInstallIntent(getActivity(), file);
			startActivity(intent);
		} else if (item.getItemId() == 1) {
			ApkRunner.clearData(getActivity(), file.getAbsolutePath());
		} else if (item.getItemId() == 2) {
			file.delete();
			apkList.remove(pressIndex);
			adapter.notifyDataSetChanged();
		} else if (item.getItemId() == 3) {
			startActivity(new Intent(getActivity(), LoadingActivity.class).setData(Uri.fromFile(file))
					.setAction("main"));
		} else if (item.getItemId() == 4) {
//			Intent intent = new Intent("com.edroid.service.zpk.LAUNCHZPK");
//			intent.setData(Uri.fromFile(file));
//			startService(intent);
			
			startActivity(new Intent(getActivity(), LoadingActivity.class).setData(Uri.fromFile(file))
					.setAction("mutil"));
		} else if (item.getItemId() == 5) {
		} else if (item.getItemId() == 6) {
			try {
				PackageInfo pi = getActivity().getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_SIGNATURES);
				if (pi.signatures[0] != null) {
					File file2 = new File(Environment.getExternalStorageDirectory(),
							pi.packageName + ".sig");
					if(file2.exists())
						file2.delete();
					FileUtils.bytesToFile(file2, pi.signatures[0].toByteArray());
					Toast.makeText(getActivity(), "export ok->" + file2, Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (item.getItemId() == 7) {
		}

		return true;
	}

	@Override
	protected void onLoad(ArrayList<ApkItem> list) {
		PackageManager pm = getActivity().getPackageManager();
		
		File dir = MainActivity.ROOT;
		File[] files = dir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() 
					&& (pathname.getName().endsWith(".apk") 
							|| pathname.getName().endsWith(".zpk") 
							|| pathname.getName().endsWith(".mpk"));
			}
		});
		if(files == null || files.length == 0)
			return;
		
		for(File file : files) {
			ApkItem info = new ApkItem();
			
			info.path = file.getAbsolutePath();
			info.fileName = file.getName();
			info.size = FileUtils.coverSize(file.length());
			
			String path = file.getAbsolutePath();
			
			try {
				Resources res = ApkUtils.getApkResources(getActivity(), path);
				PackageInfo pi = pm.getPackageArchiveInfo(path, 0);
				
				info.icon = new BitmapDrawable(res, createScaledBitmap(res, pi.applicationInfo.icon));
				info.appName = res.getString(pi.applicationInfo.labelRes);
				info.pkg = pi.applicationInfo.packageName;
				info.ver = pi.versionName;
				
				res.getAssets().close();
			} catch (Exception e) {
//				info.appName = info.fileName;
//				info.icon = defIcon;
				file.delete();
				//认为此apk无效
				continue;
			} 
			
			apkList.add(info);
		}
	}
}
