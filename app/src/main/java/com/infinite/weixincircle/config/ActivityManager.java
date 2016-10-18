package com.infinite.weixincircle.config;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * activity管理类
 *
 */
public class ActivityManager {

	private final String TAG = "ActivityManager";
	
	private static ActivityManager instance;
	
	private List<Activity> activityList = new ArrayList<Activity>();

	private ActivityManager() {
	}

	public synchronized static ActivityManager getInstance() {
		if (instance == null) {
			instance = new ActivityManager();
		}
		
		return instance;
	}
	
	public synchronized void addActivity(Activity activity) {
		activityList.add(activity);
	}
	
    public synchronized void removeActivity(Activity activity){
        if(activity != null) {
        	activityList.remove(activity);
        }
    }
    
    public boolean isActivityExist(String activityClassName) {
    	if (activityClassName == null) {
    		return false;
    	}
    	
    	for (Activity activity : activityList) {
    		String shorClassName = activity.getComponentName().getClassName();
    		if (shorClassName.equals(activityClassName)) {
    			return true;
    		}
		}
    	
    	return false;
    }

	public void exitAllActivity() {
		for (Activity activity : activityList) {
			activity.finish();
		}
	}

}
