package android.hide;

import android.graphics.Point;
import android.view.Display;

public class DisplayUtils {

	public static void getPhysicalSize(Display display,  Point size) {
		try {
			android.view.DisplayInfo displayInfo = new android.view.DisplayInfo();
			display.getDisplayInfo(displayInfo);
			
			System.out.println("displayInfo" + displayInfo.toString());
			
			size.x = displayInfo.logicalWidth;
			size.y = displayInfo.logicalHeight;
		} catch (Exception e) {
			display.getRealSize(size);
		}
	}
}
