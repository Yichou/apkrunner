package com.apkrunner.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.apkrunner.utils.Logger;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XmlResourceParser;
import android.os.SystemClock;

/**
 * 组件匹配器
 * 
 * @author Yichou 2013-11-11 15:03:54
 * 
 * <ul>
 * <li>2014-4-1 增加组件合并，同一个组件配置多次，合并成一个 </li>
 * </ul>
 *
 */
public class ComponentMatcher {
	static final String LOG_TAG = "ComponentMatcher";
	static final Logger log = Logger.create(false, "matcher");
	
	
	public static final String CMP_ACTIVITY = "activity";
	public static final String CMP_SERVICE = "service";
	public static final String CMP_RECEIVER = "receiver";

	public static final String TAG_NAME_CATEGORY = "category";
	public static final String TAG_NAME_ACTION = "action";
	public static final String TAG_NAME_DATA = "data";
	
	private static final String NAME_SPACE = "http://schemas.android.com/apk/res/android";
	private static final String ATTR_NAME_NAME = "name";
	private static final String TAG_NAME_FILTER = "intent-filter";
	
	
	private ParseInfo mParseInfo;
	
	
	public ComponentMatcher() {
	}
	
	public void loadMainfest(byte[] xmldata) throws Exception {
		long t0 = SystemClock.uptimeMillis();
		
		mParseInfo = parse(createParser(xmldata));
		
		QLog.d(LOG_TAG, "parse mainfest useTime=" + (SystemClock.uptimeMillis() - t0));
	}
	
	public String match(String cmp, Intent intent) {
		MatchResult ret = matchResult(cmp, intent);
		return ret!=null? ret.name : null;
	}
	
	public MatchResult matchResult(String cmp, Intent intent) {
		if(mParseInfo != null)
			return mParseInfo.match(cmp, intent);
		
		return null;
	}
	
	private XmlResourceParser createParser(byte[] data) throws Exception {
		Class<?> blockCls = Class.forName("android.content.res.XmlBlock");

		Constructor<?> constructor = blockCls.getDeclaredConstructor(byte[].class);
		constructor.setAccessible(true);
		Object block = constructor.newInstance(data);

		Method method = blockCls.getDeclaredMethod("newParser");
		method.setAccessible(true);
		return (XmlResourceParser) method.invoke(block);
	}
	
	public static final class MatchResult {
		public String name;
		public IntentFilter filter;
	}
	
	public ParseInfo getParseInfo() {
		return mParseInfo;
	}
	
	public static final class ParseInfo {
		final HashMap<String, List<IntentFilter>> mActivitys = new HashMap<String, List<IntentFilter>>();
		final HashMap<String, List<IntentFilter>> mReceivers = new HashMap<String, List<IntentFilter>>();
		final HashMap<String, List<IntentFilter>> mServices = new HashMap<String, List<IntentFilter>>();
		

		private HashMap<String, List<IntentFilter>> getMap(String cmp) {
			if(CMP_ACTIVITY.equals(cmp)) {
				return mActivitys;
			} else if (CMP_RECEIVER.equals(cmp)) {
				return mReceivers;
			} else if (CMP_SERVICE.equals(cmp)) {
				return mServices;
			}
			
			return null;
		}
		
		public void addCmp(String cmp, String name, List<IntentFilter> list) {
			HashMap<String, List<IntentFilter>> map = getMap(cmp);
			List<IntentFilter> tmpList = map.get(name);
			
			log.i("addCmp " + cmp + ", " + name + ", filters=" + list.size());
			
			if(tmpList != null) {
				for(IntentFilter f : list)
					tmpList.add(f);
				
				log.w("add to cache " + name + ", " + tmpList.size());
			} else {
				map.put(name, list);
			}
		}
		
		public List<IntentFilter> getReceiver(String name) {
			return getMap(CMP_RECEIVER).get(name);
		}
		
		public MatchResult match(String cmp, Intent intent) {
			for(Entry<String, List<IntentFilter>> entry: getMap(cmp).entrySet()) {
				List<IntentFilter> list = entry.getValue();
				
				if(list == null || list.size() == 0)
					continue;
				
				for(IntentFilter filter : list) {
					boolean category = (intent.getCategories() != null)? 
							(filter.matchCategories(intent.getCategories()) == null) : true;
					
					boolean action = (intent.getAction() != null)? 
							filter.matchAction(intent.getAction()) : false;
					
					if (category && action) {
						MatchResult ret = new MatchResult();
						ret.filter = filter;
						ret.name = entry.getKey();
						
						return ret;
					}
				}
			}
			
			return null;
		}
	}
	
	private ParseInfo parse(XmlResourceParser parser) throws Exception {
		int tag;
		String curCmp = null;
		String curCmpName = null;
		
		ParseInfo parseInfo = new ParseInfo();
		IntentFilter filter = null;
		List<IntentFilter> list = null;
		
		while ((tag = parser.next()) != XmlResourceParser.END_DOCUMENT) {
			if (tag == XmlResourceParser.START_TAG) {
				final String tagName = parser.getName();
				
				if (tagName.equals(CMP_ACTIVITY) 
						|| tagName.equals(CMP_RECEIVER)
						|| tagName.equals(CMP_SERVICE)) {
					log.i("cmp start>>");
					curCmp = tagName;
					curCmpName = parser.getAttributeValue(NAME_SPACE, ATTR_NAME_NAME);
					list = new ArrayList<IntentFilter>(3);
				} else if (curCmp != null && tagName.equals(TAG_NAME_FILTER)) { //<intent-filter>
					log.i(" filter start>>");
					filter = new IntentFilter();
					filter.setPriority(parser.getAttributeIntValue(NAME_SPACE, "priority", 0));
				} else if (filter != null && tagName.equals(TAG_NAME_ACTION)) {
					log.i("  action start ");
					filter.addAction(parser.getAttributeValue(0));
				} else if (filter != null && tagName.equals(TAG_NAME_CATEGORY)) {
					log.i("  category start ");
					filter.addCategory(parser.getAttributeValue(0));
				} else if (filter != null && tagName.equals(TAG_NAME_DATA)) {
					log.i("  data start ");
					String aName = parser.getAttributeName(0);
					
					if("mimeType".equals(aName)) {
						filter.addDataType(parser.getAttributeValue(0));
					} else if ("scheme".equals(aName)) {
						filter.addDataScheme(parser.getAttributeValue(0));
					}
				}
			} else if (tag == XmlResourceParser.END_TAG) {
				final String tagName = parser.getName();
				
				if(curCmp != null && curCmp.equals(parser.getName())) {
					log.i("<< cmp end");
					parseInfo.addCmp(curCmp, curCmpName, list);
					curCmp = null;
					curCmpName = null;
					list = null;
				} else if (filter != null && tagName.equals(TAG_NAME_FILTER)) { //<intent-filter>
					log.i(" <<filter end ");
					if(list != null)
						list.add(filter);
					filter = null;
				} 
			}
		}

		return parseInfo;
	}
}
