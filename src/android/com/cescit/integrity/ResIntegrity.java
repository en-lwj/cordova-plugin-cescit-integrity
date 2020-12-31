package com.cescit.integrity;

import org.json.JSONObject;
import android.content.Context;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.regex.*;
import java.nio.charset.StandardCharsets;

// apk res资源完整性检验
class ResIntegrity {

    private static final String MESSAGE_DIGEST_ALGORITHM = "SHA-256";
    private static final String ASSETS_BASE_PATH = "";

    // Collections.unmodifiableMap 使得返回的内容只能只读访问
    private static final Map<String, String> hashList = Collections.unmodifiableMap(
        new HashMap<String, String>()
    );

    public static JSONObject check(Context context) throws Exception {
        JSONObject result = new JSONObject();
        Map<String, String> resHashList = getResHashMap(context);
        // 遍历对比文件hash
        for (Map.Entry<String, String> entry : hashList.entrySet()) {
            // byte[] fileNameDecode = Base64.decode(entry.getKey(), 0);
            // String fileName = new String(fileNameDecode, StandardCharsets.UTF_8);
            String fileName = entry.getKey();
            String presetHash = entry.getValue();
            String filePath = ASSETS_BASE_PATH.concat(fileName);
            String nowHash = resHashList.get(filePath);
            if (presetHash == null || !presetHash.equals(nowHash)) {
                throw new Exception("Content of " + filePath + " has been tampered");
            }
        }
        result.put("res conunt", hashList.size());
        return result;
    }

    // 获取Res文件对应的hash值
    public static Map getResHashMap(Context context) throws Exception{
        File file = new File(context.getPackageCodePath());
        ZipFile zf = new ZipFile(file);
        // res资源（路径：文件）键值对
        Map<String, ZipEntry> resList = new HashMap<String,ZipEntry>();
        // res资源（路径：文件hash）键值对
        Map<String, String> resHashList = new HashMap<String,String>();
        // 提取出apk里的res文件资源
        Enumeration<?> entries = zf.entries();
        while(entries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) entries.nextElement();
            String pattern = "^res\\/.*";
            boolean isMatch = Pattern.matches(pattern, zipEntry.getName());
            if(isMatch) {
                resList.put(zipEntry.getName(), zipEntry);
            }
        }
        // 获取文件hash
        for (Map.Entry<String, ZipEntry> resListItem : resList.entrySet()) {
            String key = resListItem.getKey();
            ZipEntry fileEntry = resListItem.getValue();
            InputStream fileSteam= zf.getInputStream(fileEntry);
            String fileHash = getFileHash(fileSteam);
            resHashList.put(key, fileHash);
        }
        return resHashList;
    }

    // 获取Res文件对应的hash值构造的String
    public static String getHashString(Context context) throws Exception{
        Map<String, String> resHashList = getResHashMap(context);
        String str = "";
        // 遍历对比文件hash
        for (Map.Entry<String, String> entry : resHashList.entrySet()) {
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
