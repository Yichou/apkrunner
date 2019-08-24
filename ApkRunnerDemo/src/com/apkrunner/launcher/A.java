package com.apkrunner.launcher;

import java.util.List;

import android.app.AppOpsManager.PackageOps;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.internal.app.IAppOpsCallback;
import com.android.internal.app.IAppOpsService;

public class A extends IAppOpsService.Stub {

	@Override
	public int checkOperation(int code, int uid, String packageName) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int noteOperation(int code, int uid, String packageName) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int startOperation(IBinder token, int code, int uid, String packageName) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void finishOperation(IBinder token, int code, int uid, String packageName) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startWatchingMode(int op, String packageName, IAppOpsCallback callback) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopWatchingMode(IAppOpsCallback callback) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IBinder getToken(IBinder clientToken) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int checkPackage(int uid, String packageName) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<PackageOps> getPackagesForOps(int[] ops) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PackageOps> getOpsForPackage(int uid, String packageName, int[] ops) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMode(int code, int uid, String packageName, int mode) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

//	@Override
	public void resetAllModes() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

//	@Override
	public void resetAllModes(int reqUserId, String reqPackageName) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int checkAudioOperation(int code, int usage, int uid, String packageName) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAudioRestriction(int code, int usage, int uid, int mode, String[] exceptionPackages)
			throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUserRestrictions(Bundle restrictions, int userHandle) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeUser(int userHandle) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

}
