package io.github.watertao.xyao.chatter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.watertao.xyao.infras.Chatter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

// @Component
public class TencentChatter implements Chatter {

  private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom().setConnectTimeout(3000)
    .setConnectionRequestTimeout(3000).setSocketTimeout(3000).build();

  private CloseableHttpClient httpClient;
  private ObjectMapper objectMapper;

  @Value("${tencent.ai.chat.host}")
  private String host;

  @Value("${tencent.ai.chat.appid}")
  private String appid;

  @Value("${tencent.ai.chat.appkey}")
  private String appkey;

  public TencentChatter() {
    this.httpClient = HttpClients.custom().setDefaultRequestConfig(REQUEST_CONFIG)
      .setMaxConnTotal(50)
      .setMaxConnPerRoute(20)
      .build();
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public String answer(String question, String context) {
    try {

      if (question.getBytes("UTF-8").length > 300) {
        return "您说的太多我听不明白";
      }

      Map<String, String> requestMap = new LinkedHashMap();
      requestMap.put("app_id", appid);
      requestMap.put("nonce_str", RandomStringUtils.randomAlphanumeric(16));
      requestMap.put("question", question);
      requestMap.put("session", context);
      requestMap.put("time_stamp", String.valueOf(System.currentTimeMillis() / 1000));

      requestMap.put("sign", makeSign(requestMap));

      URIBuilder builder = new URIBuilder(host);
      for (Map.Entry<String, String> entry : requestMap.entrySet()) {
        builder.setParameter(entry.getKey(), entry.getValue());
      }

      HttpGet httpGet = new HttpGet(builder.build());
      CloseableHttpResponse response = httpClient.execute(httpGet);
      Map<String, Object> responseData = objectMapper.readValue(EntityUtils.toString(response.getEntity()), HashMap.class);

      if (((Integer) responseData.get("ret")) == 0) {
        return (String) ((Map) responseData.get("data")).get("answer");
      } else {
        throw new IllegalStateException((String) responseData.get("msg") + "[ " + responseData.get("ret") + " ]");
      }

    } catch (Exception e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }


  private String makeSign(Map<String, String> requestMap) throws UnsupportedEncodingException {
    List<String> tmpParamList = new ArrayList<>();
    for (Map.Entry<String, String> entry : requestMap.entrySet()) {
      if (!StringUtils.isEmpty(entry.getValue())) {
        tmpParamList.add(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
      }
    }

    String literal4Sign = StringUtils.join(tmpParamList, "&") + "&app_key=" + appkey;
    return StringUtils.upperCase(DigestUtils.md5Hex(literal4Sign));

  }



}
