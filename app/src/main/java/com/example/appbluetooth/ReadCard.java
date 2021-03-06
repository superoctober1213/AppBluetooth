package com.example.appbluetooth;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.IDReader.IDPhotoHelper;
import com.example.IDReader.WLTService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReadCard extends Thread{

    InputStream mIn;
    OutputStream mOut;
    Context mContext;
    Handler mHandler;
    TextView tv;
    ImageView ic;
    ProgressDialog pDialog;
    String text = "";
    Bitmap bmp;
    int Readflage = -99;
    String[] decodeInfo = new String[10];
    byte[] cmd_SAM = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x12, (byte) 0xFF, (byte) 0xEE  };
    byte[] cmd_find  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x01, 0x22  };
    byte[] cmd_selt  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x02, 0x21  };
    byte[] cmd_read  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x30, 0x01, 0x32 };
    byte[] cmd_sleep  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x02, 0x00, 0x02};
    byte[] cmd_weak  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x02, 0x01, 0x03 };
    byte[] recData = new byte[1500];
    public ReadCard( Context mContext, Handler mHandler, TextView tv, ImageView ic, ProgressDialog pDialog) {
        super();
        this.mIn = MainActivity.mInputStream;
        this.mOut = MainActivity.mOutputStream;
        this.mContext = mContext;
        this.mHandler = mHandler;
        this.tv = tv;
        this.ic = ic;
        this.pDialog = pDialog;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();

        int readcount = 15;
        try
        {
            while(readcount > 1)
            {
                ReadCard();
                readcount = readcount - 1;
                if(Readflage > 0)
                {
                    readcount = 0;
                    text = "?????????" + decodeInfo[0] + "\n" + "?????????" + decodeInfo[1] + "\n" + "?????????" + decodeInfo[2] + "\n"
                            + "???????????????" + decodeInfo[3] + "\n" + "?????????" + decodeInfo[4] + "\n" + "???????????????" + decodeInfo[5] + "\n"
                            + "???????????????" + decodeInfo[6] + "\n" + "???????????????" + decodeInfo[7] + "-" + decodeInfo[8] + "\n"
                            + decodeInfo[9] + "\n";
                    if(Readflage != 1)
                    {
                        text +="????????????????????????????????????" + Environment.getExternalStorageDirectory() + "/wltlib/";
                        bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.face);
                    }
                }
                else
                {
                    text = "????????????";
                    bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.face);
                }
                Thread.sleep(100);
            }
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    pDialog.dismiss();
                    tv.setText(text);
                    ic.setImageBitmap(bmp);
                }
            });

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            text = "????????????";
            //bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.face);
        }
    }

    private void ReadCard()
    {
        try
        {
            if((mIn == null)||(mOut == null))
            {
                Readflage = -2;//????????????
                return;
            }
            mOut.write(cmd_find);
            Thread.sleep(50);
            int datalen = mIn.read(recData);
            if(recData[9] == -97)
            {
                mOut.write(cmd_selt);
                Thread.sleep(50);
                datalen = mIn.read(recData);
                if(recData[9] == -112)
                {
                    mOut.write(cmd_read);
                    Thread.sleep(1000);
                    byte[] tempData = new byte[1500];
                    if(mIn.available()>0)
                    {
                        datalen = mIn.read(tempData);
                    }
                    else
                    {
                        Thread.sleep(50);
                        if(mIn.available()>0)
                        {
                            datalen = mIn.read(tempData);
                        }
                    }
                    int flag = 0;
                    if(datalen <1294)
                    {
                        for(int i = 0;i<datalen ;i++,flag++)
                        {
                            recData[flag] = tempData[i];
                        }
                        Thread.sleep(500);
                        if(mIn.available()>0)
                        {
                            datalen = mIn.read(tempData);
                        }
                        else
                        {
                            Thread.sleep(50);
                            if(mIn.available()>0)
                            {
                                datalen = mIn.read(tempData);
                            }
                        }
                        for(int i = 0;i<datalen ;i++,flag++)
                        {
                            recData[flag] = tempData[i];
                        }

                    }
                    else
                    {
                        for(int i = 0;i<datalen ;i++,flag++)
                        {
                            recData[flag] = tempData[i];
                        }
                    }
                    tempData = null;
                    if(flag == 1295)
                    {
                        if(recData[9] == -112)
                        {

                            byte[] dataBuf = new byte[256];
                            for(int i = 0; i < 256; i++)
                            {
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
                            try
                            {
                                int code = Integer.parseInt(decodeInfo[2].toString());
                                decodeInfo[2] = decodeNation(code);
                            }
                            catch (Exception e)
                            {
                                decodeInfo[2] = "";
                            }

                            //????????????
                            byte[] wltData = new byte[1024];
                            System.arraycopy(recData, 256 + 14, wltData, 0, 1024);
                            byte[] bgrBuf = new byte[WLTService.imgLength];
                            if (1 == WLTService.wlt2Bmp(wltData, bgrBuf))
                            {
                                Readflage = 1;
                                bmp = IDPhotoHelper.Bgr2Bitmap(bgrBuf);
                            }
                            else
                            {
                                Readflage = 6;
                            }

                        }
                        else
                        {
                            Readflage = -5;//???????????????
                        }
                    }
                    else
                    {
                        Readflage = -5;//????????????
                    }
                }
                else
                {
                    Readflage = -4;//????????????
                }
            }
            else
            {
                Readflage = -3;//????????????
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            Readflage = -99;//??????????????????
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            Readflage = -99;//??????????????????
        }
    }

    private String decodeNation(int code)
    {
        String nation;
        switch (code)
        {
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
