package com.apkrunner.launcher;

import java.io.File;

import com.apkrunner.ApkRunner;
import com.edroid.common.base.BaseActivity;
import com.edroid.common.ui.SlidingTabLayout;
import com.edroid.common.utils.FileUtils;
import com.apkrunner.launcher.R;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class MainActivity extends BaseActivity {
	
	
	static final File ROOT = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//	static final File ROOT = new File(Environment.getExternalStorageDirectory(), 
//			"tencent/tassistant/apk");
	
	static final String[] PAGE_TITLES = {
		"下载", "本机应用"
	};
		
	static final Class<?>[] F_CLASS = {
		LocalApksFragment.class, InstalledApksFragment.class
	};
	
	class MyFragmentPagerAdapter extends FragmentPagerAdapter {
		Fragment[] fragments;

		
		public MyFragmentPagerAdapter() {
			super(getSupportFragmentManager());
			fragments = new Fragment[getCount()];
		}

		@Override
		public Fragment getItem(int position) {
			if(fragments[position] == null) {
				try {
					fragments[position] = (Fragment) F_CLASS[position].newInstance();
				} catch (Exception e) {
				}
			}
			return fragments[position];
		}

		@Override
		public int getCount() {
			return PAGE_TITLES.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return PAGE_TITLES[position % PAGE_TITLES.length];
		}
	}
	
	static {
		ApkRunner.ENTRY_ACTIVITY_NAME = MainActivity.class.getName();
		FileUtils.createDir(ROOT);
	}
	
	ViewPager mPager;
	
	@Override
	protected int getLayoutId() {
		return R.layout.fragemnt_home;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(new MyFragmentPagerAdapter());
		
		final SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
		slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
//		slidingTabLayout.setCustomTabView(R.layout.tab_item, R.id.text);
		slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color._colorAccent));
		slidingTabLayout.setDistributeEvenly(true);
		slidingTabLayout.setViewPager(mPager);
	}
}
