package com.edroid.common.base;

import com.apkrunner.launcher.R;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ActionMode;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

public class BaseListFragment extends BasePage implements OnItemLongClickListener, 
	SwipeRefreshLayout.OnRefreshListener, OnItemClickListener {
	protected int pressedIndex;
	protected ListView mListView;
	protected SwipeRefreshLayout layout;
	
	
	@Override
	protected int getLayoutId() {
		return R.layout.fragment_list;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		layout = (SwipeRefreshLayout) view.findViewById(R.id.srl);
		layout.setOnRefreshListener(this);
		layout.setEnabled(true);
		layout.setSoundEffectsEnabled(true);
		layout.setColorSchemeResources(R.color.color_scheme_1_1, R.color.color_scheme_1_2, 
				R.color.color_scheme_1_3, R.color.color_scheme_1_4);
		
		mListView = (ListView) view.findViewById(android.R.id.list);
		mListView.setOnItemClickListener(this);
//		mListView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
//			
//			@Override
//			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//				return false;
//			}
//			
//			@Override
//			public void onDestroyActionMode(ActionMode mode) {
//			}
//			
//			@Override
//			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//				return onListCreateActionMode(mListView, mode, menu);
//			}
//			
//			@Override
//			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//				return false;
//			}
//			
//			@Override
//			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
//				
//			}
//		});
	}
	
	public boolean onListCreateActionMode(ListView lv, ActionMode mode, Menu menu) {
		return false;
	}
	
	protected void setRefreshing(boolean r) {
		layout.setRefreshing(r);
	}
	
	protected void setListAdapter(ListAdapter adapter) {
		mListView.setAdapter(adapter);
	}
	
	public ListView getListView() {
		return mListView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getListView().setOnItemLongClickListener(this);
		
		registerForContextMenu(getListView());
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		pressedIndex = position;
		return false;
	}

	/**
	 * 获取当前按下的列
	 * 
	 * @return
	 */
	public int getPressedIndex() {
		return pressedIndex;
	}

	@Override
	public void onRefresh() {
	}

	@Override
	public final void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		pressedIndex = position;
		onListItemClick(mListView, v, position, id);
	}
	
	public void onListItemClick(ListView l, View v, int position, long id){}
}
