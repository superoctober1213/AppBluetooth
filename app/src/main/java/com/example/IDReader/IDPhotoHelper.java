package com.example.IDReader;

import android.graphics.Bitmap;


public class IDPhotoHelper {
    public static Bitmap Bgr2Bitmap(byte[] bgrbuf)
    {
        int width = WLTService.imgWidth;
        int height = WLTService.imgHeight;
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int row = 0, col = 0;
        for (int i = bgrbuf.length-1; i >= 3; i -= 3) {
            int color = bgrbuf[i] & 0xFF;
            color += (bgrbuf[i-1] << 8) & 0xFF00;
            color += ((bgrbuf[i-2]) << 16) & 0xFF0000;
            bmp.setPixel(col++, row, color);
            if (col == width) {
                col = 0;
                row++;
            }
        }
        return bmp;
    }
	
}
