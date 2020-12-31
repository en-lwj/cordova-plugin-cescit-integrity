package com.cescit.integrity;

import org.json.JSONObject;
import android.content.Context;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;

// apk完整性检验
class ApkIntegrity {

    private static final String MESSAGE_DIGEST_ALGORITHM = "SHA-256";
    private static final String ASSETS_BASE_PATH = "";

    // Collections.unmodifiableMap 使得返回的内容只能只读访问
    private static final Map<String, String> hashList = Collections.unmodifiableMap(
        new HashMap<String, String>()
    );

    public static JSONObject check(Context context) throws Exception {
        JSONObject result = new JSONObject();
        Map<String, String> nowHashList = getHashMap(context);
        String ret = HttpUtil.getHttpRequestData((String) Config.getConfig("APK_HASH_URL"));
        JSONObject obj = new JSONObject(ret);
        String upHash = obj.getString("apk");
        String nowHash = nowHashList.get("apk");
        if (!upHash.equals(nowHash)) {
            throw new Exception("Content of APK has been tampered");
        }
        result.put("res conunt", hashList.size());
        return result;
    }

    // 获取文件对应的hash值
    public static Map getHashMap(Context context) throws Exception{
        File file = new File(context.getPackageCodePath());
        InputStream fis = new FileInputStream(file);
        String fileHash = getFileHash(fis);
        // res资源（路径：文件hash）键值对
        Map<String, String> nowHashList = new HashMap<String,String>();
        nowHashList.put("apk", fileHash);
        return nowHashList;
    }

    // 获取Res文件对应的hash值构造的String
    public static String getHashString(Context context) throws Exception{
        Map<String, String> nowHashList = getHashMap(context);
        String str = "";
        // 遍历对比文件hash
        for (Map.Entry<String, String> entry : nowHashList.entrySet()) {
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
