package com.cescit.integrity;

import android.content.res.AssetManager;
import android.util.Base64;
import android.app.Activity;
import android.content.Context;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


class AssetsIntegrity {

    private static final String MESSAGE_DIGEST_ALGORITHM = "SHA-256";
    private static final String ASSETS_BASE_PATH = "";

    private static final Map<String, String> hashList = Collections.unmodifiableMap(
        new HashMap<String, String>()
    );

    public static JSONObject check(Context context) throws Exception {
        AssetManager assets = context.getAssets();
        for (Map.Entry<String, String> entry : hashList.entrySet()) {
            // byte[] fileNameDecode = Base64.decode(entry.getKey(), 0);
            // String fileName = new String(fileNameDecode, StandardCharsets.UTF_8);
            // Log.d("AntiTampering", fileName + " -> " + entry.getValue());
            String filePath = entry.getKey();
            InputStream file = assets.open(filePath);
            String hash = getFileHash(file);
            if (entry.getValue() == null || !entry.getValue().equals(hash)) {
                throw new Exception("Content of " + filePath + " has been tampered");
            }
        }
        JSONObject result = new JSONObject();
        result.put("count", hashList.size());
        return result;
    }

    // 获取Res文件对应的hash值构造的String
    public static String getHashString(Context context) throws Exception{
        AssetManager assets = context.getAssets();
        String str = "";
        // 遍历对比文件hash
        for (Map.Entry<String, String> entry : hashList.entrySet()) {
            String fileName = entry.getKey();
            String presetHash = entry.getValue();
            if (!presetHash.equals("")) {
                str += "put(\"" + fileName + "\", \"" + presetHash + "\");";
            }
        }
        // 用默认字符编码解码字符串。
        byte[] bs = str.getBytes();
        str = new String(bs, StandardCharsets.UTF_8);
        return str;
    }

    private static String getFileHash(InputStream file) throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = file.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        MessageDigest digest = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM);
        byte[] hashBytes = digest.digest(buffer.toByteArray());
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hashBytes.length; i++) {
            if ((0xff & hashBytes[i]) < 0x10) {
                hexString.append("0");
            }
            hexString.append(Integer.toHexString(0xFF & hashBytes[i]));
        }
        // Log.d("AntiTampering", String(hexString));
        return new String(hexString);
    }

}
