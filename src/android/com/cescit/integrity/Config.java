package com.cescit.integrity;

import java.util.HashMap;
import java.util.Map;

class Config {
  private static final String APK_HASH_URL = "http://webf.cewater.com.cn/apk-hash/jm.json";

  // 获取配置
  public static <T> T getConfig(String key) throws Exception{
    // res资源（路径：文件hash）键值对
    Map<String, T> config = new HashMap<String,T>();
    config.put("APK_HASH_URL", (T) APK_HASH_URL);
    T result = null;
    for(String configKey : config.keySet()){
      result = config.get(configKey);
    }
    return (T) result;
  }
}