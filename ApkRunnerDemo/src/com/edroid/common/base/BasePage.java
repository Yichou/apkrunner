package com.edroid.common.base;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public abstract class BasePage extends Fragment {
	private boolean sdks = true;
	
	public void setSdks(boolean sdks) {
		this.sdks = sdks;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home) {
			finish();
        	return true;
        }
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(getLayoutId(), container, false);
	}
	
	public AppCompatActivity getSupportActivity() {
		return (AppCompatActivity) getActivity();
	}
	
	public ActionBar getSupportActionBar() {
		return getSupportActivity().getSupportActionBar();
	}
	
	public boolean onBackPressed() {
		return false;
	}
	
	public Application getApplication() {
		return getActivity().getApplication();
	}
	
	public String getTitle() {
		CharSequence s = getActivity().getTitle();
		return TextUtils.isEmpty(s)? getClass().getSimpleName() : s.toString();
	}
	
	public void setTitle(CharSequence s) {
		getActivity().setTitle(s);
	}
	
	public Intent getIntent() {
		return getActivity().getIntent();
	}
	
	protected void finish() {
		getActivity().finish();
	}
	
	public View findViewById(int id) {
		return getView().findViewById(id);
	}
	
	public LayoutInflater getLayoutInflater() {
		return getActivity().getLayoutInflater();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
//		if(sdks) SdkUtils.onPageEnd(getActivity(), getTitle());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
//		if(sdks) SdkUtils.onPageStart(getActivity(), getTitle());
	}
	
	protected abstract int getLayoutId();
	
	public void setResult(int code) {
		getActivity().setResult(code);
	}
}
