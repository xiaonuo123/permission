package com.ernesto.permission;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;


public class PermissionUtil {

    private static PermissionUtil mInstance = new PermissionUtil();

    private static PermissionListener sListener;

    private static String sDefPreliminaryExplanation;
    private static String sDefPreliminaryPositive;
    private static String sDefPreliminaryNegative;

    private static String sDefFurtherExplanation;
    private static String sDefFurtherPositive;
    private static String sDefFurtherNegative;

    private static boolean sIsDefaultStringInitialized;

    private static String sFurtherExplanation;
    private static String sFurtherPositive;
    private static String sFurtherNegative;

    private PermissionUtil(){}

    public static PermissionUtil getInstance(){
        return mInstance;
    }

    public void onRequestPermissionsResult(Activity context,int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length > 0 && grantResults.length > 0) {
            if (grantResults[0] == -1) {
                showDialogUnreasonable(context,permissions[0],true,sFurtherExplanation,sFurtherPositive,sFurtherNegative);
//                showDialog(context,permissions[0],true,sFurtherExplanation,sFurtherPositive,sFurtherNegative);
            } else if (grantResults[0] == 0) {
                if (sListener != null) {
                    sListener.onPermissionGranted();
                    sListener = null;
                }
            }
        }
    }

    public void checkAndRequestPermission(Activity context,String permission,PermissionListener listener,
                                          String title,String positiveBtn,String negativeBtn,
                                          String titleFurther,String positiveBtnFurther,String negativeBtnFurther) {
        initializeDefaultString(context);
        int result = ContextCompat.checkSelfPermission(context,permission);
        sListener = listener;

        sFurtherExplanation = titleFurther;
        sFurtherPositive = positiveBtnFurther;
        sFurtherNegative = negativeBtnFurther;

        if (PackageManager.PERMISSION_GRANTED == result) {
            if (sListener != null)
                sListener.onPermissionGranted();
            sListener = null;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                    permission)) {
                showDialogUnreasonable(context,permission,false,title,positiveBtn,negativeBtn);
//                showDialog(context,permission,false,title,positiveBtn,negativeBtn);
            } else {
                ActivityCompat.requestPermissions(context,
                        new String[]{permission},
                        8879);
            }
        }
    }

    private void initializeDefaultString(Context context){
        if (!sIsDefaultStringInitialized) {
            sDefPreliminaryExplanation = context.getResources().getString(R.string.permission_preliminary_explanation);
            sDefPreliminaryPositive = context.getResources().getString(R.string.permission_preliminary_positive);
            sDefPreliminaryNegative = context.getResources().getString(R.string.permission_preliminary_negative);

            sDefFurtherExplanation = context.getResources().getString(R.string.permission_further_explanation);
            sDefFurtherPositive = context.getResources().getString(R.string.permission_further_positive);
            sDefFurtherNegative = context.getResources().getString(R.string.permission_further_negative);
            sIsDefaultStringInitialized = true;
        }
    }

    public interface PermissionListener {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    private void showDialogUnreasonable(final Activity activity,final String permission,final boolean needJump2Setting,
                                        String title,String positiveBtn,String negativeBtn) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton(positiveBtn,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (needJump2Setting) {
                                    activity.startActivity(new Intent(Settings.ACTION_SETTINGS));
                                } else {
                                    if (activity != null)
                                        ActivityCompat.requestPermissions(activity,
                                                new String[]{permission},
                                                8879);
                                }
                            }
                        }
                )
                .setNegativeButton(negativeBtn,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (sListener != null)
                                    sListener.onPermissionDenied();
                                sListener = null;
                            }
                        }
                )
                .create().show();
    }

    private void showDialog(Activity activity,String permission,boolean needJump2Setting,
                            String title,String positiveBtn,String negativeBtn) {
        if (activity instanceof FragmentActivity) {
            DialogFragment newFragment = PermissionDialog.newInstance(permission,needJump2Setting,title,positiveBtn,negativeBtn);
            newFragment.show(((FragmentActivity)activity).getSupportFragmentManager(), "dialog");
        } else {

            try {
                android.app.DialogFragment newFragment = PermissionDialogOld.newInstance(permission,needJump2Setting,title,positiveBtn,negativeBtn);
                newFragment.show(activity.getFragmentManager(),"dialog");
            } catch (Exception e) {
                Log.d("kratos",e.getMessage());
            }

        }

    }

    public static class PermissionDialogOld extends android.app.DialogFragment {

        public static PermissionDialogOld newInstance(String permission,boolean needJump2Setting,
                                                   String title,String positiveBtn,String negativeBtn) {
            PermissionDialogOld dialog = new PermissionDialogOld();
            Bundle bundle = new Bundle();
            bundle.putString("permission",permission);
            bundle.putString("title", TextUtils.isEmpty(title) ?
                    (needJump2Setting ? sDefFurtherExplanation : sDefPreliminaryExplanation)
                    : title);
            bundle.putString("positiveBtn",TextUtils.isEmpty(positiveBtn) ?
                    (needJump2Setting ? sDefFurtherPositive : sDefPreliminaryPositive)
                    : positiveBtn);
            bundle.putString("negativeBtn",TextUtils.isEmpty(negativeBtn) ?
                    (needJump2Setting ? sDefFurtherNegative : sDefPreliminaryNegative)
                    : negativeBtn);
            bundle.putBoolean("needJump2Setting",needJump2Setting);
            dialog.setArguments(bundle);
            return dialog;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (getActivity() != null) {
                final String permission = getArguments() == null ? "" : getArguments().getString("permission");
                final String title = getArguments() == null ? "" : getArguments().getString("title");
                final String positiveBtn = getArguments() == null ? "" : getArguments().getString("positiveBtn");
                final String negativeBtn = getArguments() == null ? "" : getArguments().getString("negativeBtn");
                final boolean needJump2Setting = getArguments() == null || getArguments().getBoolean("needJump2Setting");
                return new AlertDialog.Builder(getActivity())
                        .setTitle(title)
                        .setCancelable(false)
                        .setPositiveButton(positiveBtn,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        if (needJump2Setting) {
                                            startActivity(new Intent(Settings.ACTION_SETTINGS));
                                        } else {
                                            if (getActivity() != null)
                                                ActivityCompat.requestPermissions(getActivity(),
                                                        new String[]{permission},
                                                        8879);
                                        }
                                    }
                                }
                        )
                        .setNegativeButton(negativeBtn,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        if (sListener != null)
                                            sListener.onPermissionDenied();
                                        sListener = null;
                                    }
                                }
                        )
                        .create();
            } else {
                return null;
            }

        }
    }

    public static class PermissionDialog extends DialogFragment {

        public static PermissionDialog newInstance(String permission,boolean needJump2Setting,
                                                   String title,String positiveBtn,String negativeBtn) {
            PermissionDialog dialog = new PermissionDialog();
            Bundle bundle = new Bundle();
            bundle.putString("permission",permission);
            bundle.putString("title", TextUtils.isEmpty(title) ?
                    (needJump2Setting ? sDefFurtherExplanation : sDefPreliminaryExplanation)
                    : title);
            bundle.putString("positiveBtn",TextUtils.isEmpty(positiveBtn) ?
                    (needJump2Setting ? sDefFurtherPositive : sDefPreliminaryPositive)
                    : positiveBtn);
            bundle.putString("negativeBtn",TextUtils.isEmpty(negativeBtn) ?
                    (needJump2Setting ? sDefFurtherNegative : sDefPreliminaryNegative)
                    : negativeBtn);
            bundle.putBoolean("needJump2Setting",needJump2Setting);
            dialog.setArguments(bundle);
            return dialog;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (getActivity() != null) {
                final String permission = getArguments() == null ? "" : getArguments().getString("permission");
                final String title = getArguments() == null ? "" : getArguments().getString("title");
                final String positiveBtn = getArguments() == null ? "" : getArguments().getString("positiveBtn");
                final String negativeBtn = getArguments() == null ? "" : getArguments().getString("negativeBtn");
                final boolean needJump2Setting = getArguments() == null || getArguments().getBoolean("needJump2Setting");
                return new AlertDialog.Builder(getActivity())
                        .setTitle(title)
                        .setCancelable(false)
                        .setPositiveButton(positiveBtn,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        if (needJump2Setting) {
                                            startActivity(new Intent(Settings.ACTION_SETTINGS));
                                        } else {
                                            if (getActivity() != null)
                                                ActivityCompat.requestPermissions(getActivity(),
                                                        new String[]{permission},
                                                        8879);
                                        }
                                    }
                                }
                        )
                        .setNegativeButton(negativeBtn,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        if (sListener != null)
                                            sListener.onPermissionDenied();
                                        sListener = null;
                                    }
                                }
                        )
                        .create();
            } else {
                return null;
            }

        }

    }











    //    private List<String> dangerousPermissions;

    //    public void initialize(Context context) {
//        PackageInfo packageInfo = null;
//        try {
//            packageInfo = context.getPackageManager().getPackageInfo("android", PackageManager.GET_PERMISSIONS);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        if (packageInfo != null && packageInfo.permissions != null) {
//            for (PermissionInfo permission : packageInfo.permissions) {
//                String protectionLevel;
//                switch(permission.protectionLevel) {
//                    case PermissionInfo.PROTECTION_NORMAL : protectionLevel = "normal"; break;
//                    case PermissionInfo.PROTECTION_DANGEROUS : protectionLevel = "dangerous"; break;
//                    case PermissionInfo.PROTECTION_SIGNATURE : protectionLevel = "signature"; break;
//                    case PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM : protectionLevel = "signatureOrSystem"; break;
//                    default : protectionLevel = "<unknown>"; break;
//                }
//
//                if ("dangerous".equals(protectionLevel)) {
//                    if (dangerousPermissions == null)
//                        dangerousPermissions = new ArrayList<>();
//                    dangerousPermissions.add(permission.name);
//                }
//            }
//        }
//    }
}
