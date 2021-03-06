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
                setTitle("??????????????????");
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
                        tv.setText("?????????" + decodeInfo[0] + "\n" + "?????????" + decodeInfo[1] + "\n" + "?????????" + decodeInfo[2]
                                + "\n" + "???????????????" + decodeInfo[3] + "\n" + "?????????" + decodeInfo[4] + "\n" + "???????????????"
                                + decodeInfo[5] + "\n" + "???????????????" + decodeInfo[6] + "\n" + "???????????????" + decodeInfo[7] + "-"
                                + decodeInfo[8] + "\n" + decodeInfo[9] + "\n");
                        if (Readflage == 1) {
                            ic.setImageBitmap(bitmap);
                        } else {
                            tv.append("??????????????????");
                            ic.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.face));
                        }
                    } else {
                        if (Readflage == -2) {
                            tv.setText("??????????????????");
                        }
                        if (Readflage == -3) {
                            tv.setText("????????????????????????");
                        }
                        if (Readflage == -4) {
                            tv.setText("????????????????????????");
                        }
                        if (Readflage == -5) {
                            tv.setText("????????????");
                        }
                        if (Readflage == -99) {
                            tv.setText("????????????");
                        }
                        ic.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.face));
                    }
                    Thread.sleep(100);
                }

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                tv.setText("?????????????????????");
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
                tv.setText("?????????");
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
                    tv.setText("????????????????????????");
                    return;
                }
                mOutputStream.write(cmd_sleep);
                tv.setText("???????????????");
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
                    tv.setText("????????????????????????");
                    return;
                }
                mOutputStream.write(cmd_weak);
                tv.setText("???????????????");
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
        // ?????????????????????receiver
        mFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, mFilter);
        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setTitle("??????????????????");

        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog, null);
        dialog = new AlertDialog.Builder(MainActivity.this).setTitle("????????????").setView(view).setCancelable(false)
                .setNegativeButton("??????", new DialogInterface.OnClickListener() {

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
                    Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void connect(BluetoothDevice device) {
        try {
            mBluetoothSocket = device.createRfcommSocketToServiceRecord(myuuid);
            mBluetoothSocket.connect();
            Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            mInputStream = mBluetoothSocket.getInputStream();
            mOutputStream = mBluetoothSocket.getOutputStream();

        } catch (Exception e) {
            // TODO: handle exception
            Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_SHORT).show();
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
                Readflage = -2;// ????????????
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
                                decodeInfo[1] = "???";
                            else
                                decodeInfo[1] = "???";
                            try {
                                int code = Integer.parseInt(decodeInfo[2].toString());
                                decodeInfo[2] = decodeNation(code);
                            } catch (Exception e) {
                                decodeInfo[2] = "";
                            }

                            // ????????????
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
                            Readflage = -5;// ???????????????
                        }
                    } else {
                        Readflage = -5;// ????????????
                    }
                } else {
                    Readflage = -4;// ????????????
                }
            } else {
                Readflage = -3;// ????????????
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            Readflage = -99;// ??????????????????
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            Readflage = -99;// ??????????????????
        }
    }

    private String decodeNation(int code) {
        String nation;
        switch (code) {
            case 1:
                nation = "???";
                break;
            case 2:
                nation = "??????";
                break;
            case 3:
                nation = "???";
                break;
            case 4:
                nation = "???";
                break;
            case 5:
                nation = "?????????";
                break;
            case 6:
                nation = "???";
                break;
            case 7:
                nation = "???";
                break;
            case 8:
                nation = "???";
                break;
            case 9:
                nation = "??????";
                break;
            case 10:
                nation = "??????";
                break;
            case 11:
                nation = "???";
                break;
            case 12:
                nation = "???";
                break;
            case 13:
                nation = "???";
                break;
            case 14:
                nation = "???";
                break;
            case 15:
                nation = "??????";
                break;
            case 16:
                nation = "??????";
                break;
            case 17:
                nation = "?????????";
                break;
            case 18:
                nation = "???";
                break;
            case 19:
                nation = "???";
                break;
            case 20:
                nation = "??????";
                break;
            case 21:
                nation = "???";
                break;
            case 22:
                nation = "???";
                break;
            case 23:
                nation = "??????";
                break;
            case 24:
                nation = "??????";
                break;
            case 25:
                nation = "???";
                break;
            case 26:
                nation = "??????";
                break;
            case 27:
                nation = "??????";
                break;
            case 28:
                nation = "??????";
                break;
            case 29:
                nation = "????????????";
                break;
            case 30:
                nation = "???";
                break;
            case 31:
                nation = "?????????";
                break;
            case 32:
                nation = "??????";
                break;
            case 33:
                nation = "???";
                break;
            case 34:
                nation = "??????";
                break;
            case 35:
                nation = "??????";
                break;
            case 36:
                nation = "??????";
                break;
            case 37:
                nation = "??????";
                break;
            case 38:
                nation = "??????";
                break;
            case 39:
                nation = "??????";
                break;
            case 40:
                nation = "??????";
                break;
            case 41:
                nation = "?????????";
                break;
            case 42:
                nation = "???";
                break;
            case 43:
                nation = "????????????";
                break;
            case 44:
                nation = "?????????";
                break;
            case 45:
                nation = "?????????";
                break;
            case 46:
                nation = "??????";
                break;
            case 47:
                nation = "??????";
                break;
            case 48:
                nation = "??????";
                break;
            case 49:
                nation = "???";
                break;
            case 50:
                nation = "?????????";
                break;
            case 51:
                nation = "??????";
                break;
            case 52:
                nation = "?????????";
                break;
            case 53:
                nation = "??????";
                break;
            case 54:
                nation = "??????";
                break;
            case 55:
                nation = "??????";
                break;
            case 56:
                nation = "??????";
                break;
            case 97:
                nation = "??????";
                break;
            case 98:
                nation = "???????????????????????????";
                break;
            default:
                nation = "";
                break;
        }
        return nation;
    }

}