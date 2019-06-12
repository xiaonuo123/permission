# Download</br>
configure repositories via root Gradle:</br>
```Bash
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
download the latest AAR from Jitpack via Gradle:</br>
```Bash
implementation 'com.github.xiaonuo123:permission:1.0.3'
```
# Procedure</br>
1.call method onRequestPermissionsResult() in your base activity:</br>
```Java
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    PermissionUtil.getInstance().onRequestPermissionsResult(this,requestCode,permissions,grantResults);
}
```
2.asks the user to grant dangerous permissions:</br>
```Java
PermissionUtil.getInstance().checkAndRequestPermission(this, permission, new PermissionUtil.PermissionListener() {
    @Override
    public void onPermissionGranted() {
        // permission was granted, yay! Do the
        // contacts-related task you need to do.
    }

    @Override
    public void onPermissionDenied() {
        // permission denied, boo! Disable the
        // functionality that depends on this permission.
    }

});
```
