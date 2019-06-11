package com.ernesto.permission;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {

    private static PermissionUtil mInstance = new PermissionUtil();

    private List<String> dangerousPermissions;

    private PermissionListener mListener;

    private PermissionUtil(){}

    public static PermissionUtil getInstance(){
        return mInstance;
    }

    public void initialize(Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo("android", PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null && packageInfo.permissions != null) {
            for (PermissionInfo permission : packageInfo.permissions) {
                String protectionLevel;
                switch(permission.protectionLevel) {
                    case PermissionInfo.PROTECTION_NORMAL : protectionLevel = "normal"; break;
                    case PermissionInfo.PROTECTION_DANGEROUS : protectionLevel = "dangerous"; break;
                    case PermissionInfo.PROTECTION_SIGNATURE : protectionLevel = "signature"; break;
                    case PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM : protectionLevel = "signatureOrSystem"; break;
                    default : protectionLevel = "<unknown>"; break;
                }

                if ("dangerous".equals(protectionLevel)) {
                    if (dangerousPermissions == null)
                        dangerousPermissions = new ArrayList<>();
                    dangerousPermissions.add(permission.name);
                }
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length > 0 && grantResults.length > 0) {
            if (grantResults[0] == -1) {
                if (mListener != null) {

                    mListener = null;
                }

            } else if (grantResults[0] == 0) {
                if (mListener != null) {
                    mListener.onPermissionGranted();
                    mListener = null;
                }
            }
        }
    }

    public void checkAndRequestPermission(Activity context,String permission,PermissionListener listener) {
        int result = ContextCompat.checkSelfPermission(context,permission);
        this.mListener = listener;

        if (PackageManager.PERMISSION_DENIED == result) {
            String[] permissions = {permission};
            ActivityCompat.requestPermissions(context,permissions,8879);
        } else if (PackageManager.PERMISSION_GRANTED == result) {
            mListener.onPermissionGranted();
            mListener = null;
        }
    }

    public interface PermissionListener {
        void onPermissionGranted();
        void onPermissionDenied();
        void noNecessary();
    }
}
