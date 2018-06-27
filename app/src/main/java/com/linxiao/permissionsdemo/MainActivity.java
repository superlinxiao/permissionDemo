package com.linxiao.permissionsdemo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * 步骤：
 * 1.检查权限
 * 2.如果有权限，执行后续操作，如果没有权限，请求权限
 * 3.权限回调结果，并根据回调结果处理后续逻辑。
 * <p>
 * ps：可以根据相应需求，添加对不再提示按钮的判断
 * <p>
 * shouldShowRequestPermissionRationale在两种情况下会返回false。
 * 1.用户第一次发起请求
 * 2.用户点击了不再提示按钮后
 */
public class MainActivity extends AppCompatActivity {

  private static final int MY_PERMISSION_REQUEST_CODE = 10000;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  /**
   * 点击按钮，将通讯录备份保存到外部存储器备。
   * <p>
   * 需要3个权限(都是危险权限):
   * 1. 读取通讯录权限;
   * 2. 读取外部存储器权限;
   * 3. 写入外部存储器权限.
   */
  public void click(View view) {
    //1, 检查是否有相应的权限
    String[] permissions = {
        Manifest.permission.READ_CONTACTS,
        //WRITE_EXTERNAL_STORAGE和READ_EXTERNAL_STORAGE是属于同一个权限组，可以只申请一个，这样所在权限组的权限自动赋予。
//                        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    boolean isAllGranted = true;
    for (String permission : permissions) {
      if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
        isAllGranted = false;
        break;
      }
    }
    // 如果这3个权限全都拥有, 则直接执行备份代码
    if (isAllGranted) {
      doBackUp();
      return;
    }

    //2, 请求权限,一次请求多个权限, 如果其他有权限是已经授予的将会自动忽略掉
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      requestPermissions(permissions, MY_PERMISSION_REQUEST_CODE);
    } else {
      ActivityCompat.requestPermissions(
          this,
          permissions,
          MY_PERMISSION_REQUEST_CODE
      );
    }
  }

  /**
   * 申请权限结果返回处理
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    //3.权限回调
    if (requestCode == MY_PERMISSION_REQUEST_CODE) {
      boolean isAllGranted = true;

      // 判断是否所有的权限都已经授予了
      for (int i = 0; i < grantResults.length; i++) {
        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
          isAllGranted = false;
          //如果用户对某一个权限设置了不再提示，那么只能引导用户手动开启
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!shouldShowRequestPermissionRationale(permissions[i])) {
              openAppDetails();
              return;
            }
          } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
              openAppDetails();
              return;
            }
          }
        }
      }
      if (isAllGranted) {
        // 如果所有的权限都授予了, 则执行备份代码
        doBackUp();

      } else {
        Toast.makeText(this, "权限申请失败,请尝试重新获取...", Toast.LENGTH_SHORT).show();
        //如果不需要检查不再提示操作，也可以直接弹框
//                openAppDetails();
      }
    }
  }

  /**
   * 备份通讯录操作
   */
  private void doBackUp() {
    // 本文主旨是讲解如果动态申请权限, 具体备份代码不再展示, 就假装备份一下
    Toast.makeText(this, "正在备份通讯录...", Toast.LENGTH_SHORT).show();
  }

  /**
   * 打开 APP 的详情设置
   */
  private void openAppDetails() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage("备份通讯录需要访问 “通讯录” 和 “外部存储器”，请到 “应用信息 -> 权限” 中授予！");
    builder.setPositiveButton("去手动授权", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + getPackageName()));
        //开启新栈或者执行清空栈内上方activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //离开后关闭
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        //不在历史记录里面显示
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
      }
    });
    builder.setNegativeButton("取消", null);
    builder.show();

    //另外一种方式
//        new PermissionOpenUtils(this).jumpPermissionPage();
  }

}
