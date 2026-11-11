package local.ha.tvbacklight;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

final class QrCode {
    static ImageView view(Context context, String value) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(value, BarcodeFormat.QR_CODE, 256, 256);
            int[] pixels = new int[256 * 256];
            for (int y = 0; y < 256; y++) {
                for (int x = 0; x < 256; x++) pixels[y*256+x] = matrix.get(x,y) ? 0xff000000 : 0xffffffff;
            }
            ImageView view = new ImageView(context);
            view.setImageBitmap(Bitmap.createBitmap(pixels, 256, 256, Bitmap.Config.ARGB_8888));
            view.setContentDescription(value);
            return view;
        } catch (com.google.zxing.WriterException error) {
            throw new IllegalArgumentException(error);
        }
    }
}
