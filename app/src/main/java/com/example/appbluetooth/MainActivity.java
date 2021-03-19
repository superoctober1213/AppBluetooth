package com.example.appbluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.IDReader.IDPhotoHelper;
import com.example.IDReader.WLTService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ListView mList;
    private Button scanf, read, colse, sleep, week;
    private ImageView ic;
    private TextView tv;
    Handler mHandler = new Handler();
    BluetoothAdapter mBluetoothAdapter;
    ArrayList<String> list;
    ArrayAdapter<String> mAdapter;
    AlertDialog dialog;
    ProgressDialog pDialog;
    BluetoothSocket mBluetoothSocket;
    public static final UUID myuuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //public static final UUID myuuid = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    static InputStream mInputStream;
    static OutputStream mOutputStream;
    int Readflage = -99;
    String[] decodeInfo = new String[10];
    private Bitmap bitmap = null;

    byte[] cmd_SAM = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x12, (byte) 0xFF,
            (byte) 0xEE};
    byte[] cmd_find = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x01, 0x22};
    byte[] cmd_selt = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x02, 0x21};
    byte[] cmd_read = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x30, 0x01, 0x32};
    byte[] cmd_sleep = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x02, 0x00, 0x02};
    byte[] cmd_weak = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x02, 0x01, 0x03};
    byte[] recData = new byte[1500];

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            String action = arg1.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = arg1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String te = device.getName() + "|" + device.getAddress();
                if (!list.contains(te)) {
                    list.add(te);
                    mAdapter.notifyDataSetChanged();
                }
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                // setProgressBarIndeterminateVisibility(false);
                setTitle("搜索蓝牙设备");
            }
        }
    };

    android.view.View.OnClickListener scanfListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            list = new ArrayList<>();
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter.startDiscovery();
            mAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.tv, list);
            mList.setAdapter(mAdapter);
            dialog.show();
        }
    };

    android.view.View.OnClickListener readListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub

            tv.setText("");
            int readcount = 15;
            try {
                while (readcount > 1) {
                    ReadCard();
                    readcount = readcount - 1;
                    if (Readflage > 0) {
                        readcount = 0;
                        tv.setText("姓名：" + decodeInfo[0] + "\n" + "性别：" + decodeInfo[1] + "\n" + "民族：" + decodeInfo[2]
                                + "\n" + "出生日期：" + decodeInfo[3] + "\n" + "地址：" + decodeInfo[4] + "\n" + "身份号码："
                                + decodeInfo[5] + "\n" + "签发机关：" + decodeInfo[6] + "\n" + "有效期限：" + decodeInfo[7] + "-"
                                + decodeInfo[8] + "\n" + decodeInfo[9] + "\n");
                        if (Readflage == 1) {
                            ic.setImageBitmap(bitmap);
                        } else {
                            tv.append("照片解码失败");
                            ic.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.face));
                        }
                    } else {
                        if (Readflage == -2) {
                            tv.setText("蓝牙连接异常");
                        }
                        if (Readflage == -3) {
                            tv.setText("无卡或卡片已读过");
                        }
                        if (Readflage == -4) {
                            tv.setText("无卡或卡片已读过");
                        }
                        if (Readflage == -5) {
                            tv.setText("读卡失败");
                        }
                        if (Readflage == -99) {
                            tv.setText("操作异常");
                        }
                        ic.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.face));
                    }
                    Thread.sleep(100);
                }

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                tv.setText("读取数据异常！");
                ic.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.face));
            }
        }
    };

    android.view.View.OnClickListener colseListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            try {
                if ((mInputStream == null) || (mOutputStream == null)) {
                    return;
                }
                mOutputStream.close();
                mInputStream.close();
                mBluetoothSocket.close();
                tv.setText("已断开");
                ic.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.face));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    android.view.View.OnClickListener sleepListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            try {
                if ((mInputStream == null) || (mOutputStream == null)) {
                    tv.setText("设备未正常连接！");
                    return;
                }
                mOutputStream.write(cmd_sleep);
                tv.setText("睡眠模式！");
                ic.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.face));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    android.view.View.OnClickListener weekListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            try {
                if ((mInputStream == null) || (mOutputStream == null)) {
                    tv.setText("设备未正常连接！");
                    return;
                }
                mOutputStream.write(cmd_weak);
                tv.setText("唤醒模式！");
                ic.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.face));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        Listener();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        IntentFilter mFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, mFilter);
        // 注册搜索完时的receiver
        mFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, mFilter);
        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setTitle("正在读卡……");

        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog, null);
        dialog = new AlertDialog.Builder(MainActivity.this).setTitle("蓝牙列表").setView(view).setCancelable(false)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                }).create();
        mList = (ListView) view.findViewById(R.id.list);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                String text = list.get(arg2);
                System.out.println("BBB-" + text);
                int a = text.indexOf("|");
                String mac = text.substring(a + 1);
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac);
                try {
                    Boolean returnValue = false;
                    Method createBondMethod = device.getClass().getMethod("createBond");
                    connect(device);
                } catch (Exception e) {
                    // TODO: handle exception
                    Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void connect(BluetoothDevice device) {
        try {
            mBluetoothSocket = device.createRfcommSocketToServiceRecord(myuuid);
            mBluetoothSocket.connect();
            Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            mInputStream = mBluetoothSocket.getInputStream();
            mOutputStream = mBluetoothSocket.getOutputStream();

        } catch (Exception e) {
            // TODO: handle exception
            Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
            ic.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.face));
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(mReceiver);
        try {
            if ((mInputStream == null) || (mOutputStream == null)) {
                return;
            }
            mOutputStream.close();
            mInputStream.close();
            mBluetoothSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void Listener() {
        // TODO Auto-generated method stub
        scanf.setOnClickListener(scanfListener);
        read.setOnClickListener(readListener);
        colse.setOnClickListener(colseListener);
        sleep.setOnClickListener(sleepListener);
        week.setOnClickListener(weekListener);
    }

    private void init() {
        // TODO Auto-generated method stub
        scanf = (Button) findViewById(R.id.btscanf);
        read = (Button) findViewById(R.id.btread);
        colse = (Button) findViewById(R.id.btclose);
        sleep = (Button) findViewById(R.id.btsleep);
        week = (Button) findViewById(R.id.btweak);
        ic = (ImageView) findViewById(R.id.imageView1);
        tv = (TextView) findViewById(R.id.textView1);
    }

    private void ReadCard() {
        try {
            if ((mInputStream == null) || (mOutputStream == null)) {
                Readflage = -2;// 连接异常
                return;
            }
            mOutputStream.write(cmd_find);
            Thread.sleep(200);
            int datalen = mInputStream.read(recData);
            if (recData[9] == -97) {
                mOutputStream.write(cmd_selt);
                Thread.sleep(200);
                datalen = mInputStream.read(recData);
                if (recData[9] == -112) {
                    mOutputStream.write(cmd_read);
                    Thread.sleep(1000);
                    byte[] tempData = new byte[1500];
                    if (mInputStream.available() > 0) {
                        datalen = mInputStream.read(tempData);
                    } else {
                        Thread.sleep(500);
                        if (mInputStream.available() > 0) {
                            datalen = mInputStream.read(tempData);
                        }
                    }
                    int flag = 0;
                    if (datalen < 1294) {
                        for (int i = 0; i < datalen; i++, flag++) {
                            recData[flag] = tempData[i];
                        }
                        Thread.sleep(1000);
                        if (mInputStream.available() > 0) {
                            datalen = mInputStream.read(tempData);
                        } else {
                            Thread.sleep(500);
                            if (mInputStream.available() > 0) {
                                datalen = mInputStream.read(tempData);
                            }
                        }
                        for (int i = 0; i < datalen; i++, flag++) {
                            recData[flag] = tempData[i];
                        }

                    } else {
                        for (int i = 0; i < datalen; i++, flag++) {
                            recData[flag] = tempData[i];
                        }
                    }
                    tempData = null;
                    if (flag == 1295) {
                        if (recData[9] == -112) {

                            byte[] dataBuf = new byte[256];
                            for (int i = 0; i < 256; i++) {
                                dataBuf[i] = recData[14 + i];
                            }
                            String TmpStr = new String(dataBuf, "UTF16-LE");
                            TmpStr = new String(TmpStr.getBytes("UTF-8"));
                            decodeInfo[0] = TmpStr.substring(0, 15);
                            decodeInfo[1] = TmpStr.substring(15, 16);
                            decodeInfo[2] = TmpStr.substring(16, 18);
                            decodeInfo[3] = TmpStr.substring(18, 26);
                            decodeInfo[4] = TmpStr.substring(26, 61);
                            decodeInfo[5] = TmpStr.substring(61, 79);
                            decodeInfo[6] = TmpStr.substring(79, 94);
                            decodeInfo[7] = TmpStr.substring(94, 102);
                            decodeInfo[8] = TmpStr.substring(102, 110);
                            decodeInfo[9] = TmpStr.substring(110, 128);
                            if (decodeInfo[1].equals("1"))
                                decodeInfo[1] = "男";
                            else
                                decodeInfo[1] = "女";
                            try {
                                int code = Integer.parseInt(decodeInfo[2].toString());
                                decodeInfo[2] = decodeNation(code);
                            } catch (Exception e) {
                                decodeInfo[2] = "";
                            }

                            // 照片解码
                            byte[] wltData = new byte[1024];
                            System.arraycopy(recData, 256 + 14, wltData, 0, 1024);
                            byte[] bgrBuf = new byte[WLTService.imgLength];
                            if (1 == WLTService.wlt2Bmp(wltData, bgrBuf)) {
                                Readflage = 1;
                                bitmap = IDPhotoHelper.Bgr2Bitmap(bgrBuf);
                            } else {
                                Readflage = 6;
                            }

                        } else {
                            Readflage = -5;// 读卡失败！
                        }
                    } else {
                        Readflage = -5;// 读卡失败
                    }
                } else {
                    Readflage = -4;// 选卡失败
                }
            } else {
                Readflage = -3;// 寻卡失败
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            Readflage = -99;// 读取数据异常
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            Readflage = -99;// 读取数据异常
        }
    }

    private String decodeNation(int code) {
        String nation;
        switch (code) {
            case 1:
                nation = "汉";
                break;
            case 2:
                nation = "蒙古";
                break;
            case 3:
                nation = "回";
                break;
            case 4:
                nation = "藏";
                break;
            case 5:
                nation = "维吾尔";
                break;
            case 6:
                nation = "苗";
                break;
            case 7:
                nation = "彝";
                break;
            case 8:
                nation = "壮";
                break;
            case 9:
                nation = "布依";
                break;
            case 10:
                nation = "朝鲜";
                break;
            case 11:
                nation = "满";
                break;
            case 12:
                nation = "侗";
                break;
            case 13:
                nation = "瑶";
                break;
            case 14:
                nation = "白";
                break;
            case 15:
                nation = "土家";
                break;
            case 16:
                nation = "哈尼";
                break;
            case 17:
                nation = "哈萨克";
                break;
            case 18:
                nation = "傣";
                break;
            case 19:
                nation = "黎";
                break;
            case 20:
                nation = "傈僳";
                break;
            case 21:
                nation = "佤";
                break;
            case 22:
                nation = "畲";
                break;
            case 23:
                nation = "高山";
                break;
            case 24:
                nation = "拉祜";
                break;
            case 25:
                nation = "水";
                break;
            case 26:
                nation = "东乡";
                break;
            case 27:
                nation = "纳西";
                break;
            case 28:
                nation = "景颇";
                break;
            case 29:
                nation = "柯尔克孜";
                break;
            case 30:
                nation = "土";
                break;
            case 31:
                nation = "达斡尔";
                break;
            case 32:
                nation = "仫佬";
                break;
            case 33:
                nation = "羌";
                break;
            case 34:
                nation = "布朗";
                break;
            case 35:
                nation = "撒拉";
                break;
            case 36:
                nation = "毛南";
                break;
            case 37:
                nation = "仡佬";
                break;
            case 38:
                nation = "锡伯";
                break;
            case 39:
                nation = "阿昌";
                break;
            case 40:
                nation = "普米";
                break;
            case 41:
                nation = "塔吉克";
                break;
            case 42:
                nation = "怒";
                break;
            case 43:
                nation = "乌孜别克";
                break;
            case 44:
                nation = "俄罗斯";
                break;
            case 45:
                nation = "鄂温克";
                break;
            case 46:
                nation = "德昂";
                break;
            case 47:
                nation = "保安";
                break;
            case 48:
                nation = "裕固";
                break;
            case 49:
                nation = "京";
                break;
            case 50:
                nation = "塔塔尔";
                break;
            case 51:
                nation = "独龙";
                break;
            case 52:
                nation = "鄂伦春";
                break;
            case 53:
                nation = "赫哲";
                break;
            case 54:
                nation = "门巴";
                break;
            case 55:
                nation = "珞巴";
                break;
            case 56:
                nation = "基诺";
                break;
            case 97:
                nation = "其他";
                break;
            case 98:
                nation = "外国血统中国籍人士";
                break;
            default:
                nation = "";
                break;
        }
        return nation;
    }

}