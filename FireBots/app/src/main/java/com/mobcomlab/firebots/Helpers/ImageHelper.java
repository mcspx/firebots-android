package com.mobcomlab.firebots.Helpers;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class ImageHelper {

    public static File saveToInternalStorage(Bitmap bitmapImage, Context context) throws FileNotFoundException {
        // determine the image directory
        ContextWrapper cw = new ContextWrapper(context);
        File imageDirectory = cw.getDir("images", MODE_PRIVATE);

        if (imageDirectory.exists()) {
            // creates the directory if not present yet
            imageDirectory.mkdir();
        }

        // Create imageDir
        File path = new File(imageDirectory, "image" + new Date().getTime() + ".jpg");

        try {
            FileOutputStream outputStream = new FileOutputStream(path);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!path.exists()) {
            throw new FileNotFoundException();
        }
        return path;
    }


    // Get real path from gallery
    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    // Modify orientation image from gallery
    public static Bitmap modifyOrientation(String path, int resizeSize) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        bitmap = ImageHelper.getResizedBitmap(bitmap, resizeSize);

        ExifInterface ei = new ExifInterface(path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        Log.d("EXIF value", ei.getAttribute(ExifInterface.TAG_ORIENTATION));

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);
            default:
                return bitmap;
        }
    }

    // Resize image
    private static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
