package com.apkrunner.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.edroid.common.utils.FileUtils;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.View;
import android.widget.ListView;

public class InstalledApksFragment extends BaseApksFragment2 {
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
//		File file = new File(ROOT, apkList.get(position).fileName);
		
		startActivity(new Intent(getActivity(), LoadingActivity.class)
				.setData(Uri.fromFile(new File(apkList.get(position).path)))
				.setAction("mutil"));
	}

	@Override
	protected void onLoad(ArrayList<ApkItem> list) {
		PackageManager pm = getActivity().getPackageManager();
		List<PackageInfo> apps = pm.getInstalledPackages(0);
		
		for(PackageInfo pi : apps) {
			boolean sys = (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
			
			if(sys)
				continue;
			
			File file = new File(pi.applicationInfo.sourceDir);
			
			ApkItem info = new ApkItem();
			
			info.path = pi.applicationInfo.sourceDir;
			info.fileName = file.getName();
			info.size = FileUtils.coverSize(file.length());
			info.icon = pi.applicationInfo.loadIcon(pm);
			info.appName = (String) pi.applicationInfo.loadLabel(pm);
			info.pkg = pi.applicationInfo.packageName;
			info.ver = pi.versionName;
			
			list.add(info);
		}
	}

}
