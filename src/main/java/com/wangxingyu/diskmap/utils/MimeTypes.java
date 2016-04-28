package com.wangxingyu.diskmap.utils;

import android.content.Context;

import com.wangxingyu.diskmap.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;


public final class MimeTypes {
    private HashMap<String, String> extensionToMime;

    private void initExtensions(Context context) {
        extensionToMime = new HashMap<>();
        try {
            InputStream is = new GZIPInputStream(context.getResources().openRawResource(R.raw.mimes));
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buf = new byte[16384];//16KB
            while (true) {
                int r = is.read(buf);
                if (r <= 0)
                    break;
                os.write(buf, 0, r);
            }
            String[] lines = os.toString().split("\n");
            String mime = null;
            for (String val : lines) {
                if (val.length() == 0)
                    mime = null;
                else if (mime == null)
                    mime = val;
                else 
                    extensionToMime.put(val, mime);
            }
        } catch (Exception e) {
            throw new RuntimeException("failed to open mime db", e);
        }
    }

    public String getMimeByExtension(Context context, String extension) {
        if (extensionToMime == null) {
            initExtensions(context);
        }
        return extensionToMime.get(extension);
    }
}
