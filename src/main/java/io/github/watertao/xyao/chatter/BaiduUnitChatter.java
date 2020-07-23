package io.github.watertao.xyao.chatter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.watertao.xyao.infras.Chatter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

@Service
public class BaiduUnitChatter implements Chatter {

  private static final Logger logger = LoggerFactory.getLogger(BaiduUnitChatter.class);

  private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
    .setConnectTimeout(3000)
    .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
    .setConnectionRequestTimeout(3000)
    .setSocketTimeout(3000)
    .build();

  private CloseableHttpClient httpClient;
  private ObjectMapper objectMapper;

  @Value("${baidu.ai.chat.host.token}")
  private String tokenHost;

  @Value("${baidu.ai.chat.host.answer}")
  private String answerHost;

  @Value("${baidu.ai.chat.appid}")
  private String appid;

  @Value("${baidu.ai.chat.apikey}")
  private String apikey;

  @Value("${baidu.ai.chat.secretkey}")
  private String secretkey;

  @Value("${baidu.ai.chat.serviceid}")
  private String serviceid;

  private Long nextTokenRetrieveTime;

  private String accessToken;


  public BaiduUnitChatter() {
    this.httpClient = HttpClients.custom().setDefaultRequestConfig(REQUEST_CONFIG)
      .setMaxConnTotal(50)
      .setMaxConnPerRoute(20)
      .build();
    this.objectMapper = new ObjectMapper();
  }


  @Override
  public String answer(String question, String context) {

    try {
      String accessToken = retrieveAccessToken();
      logger.debug("access token: {}", accessToken);

      Map<String, Object> entityMap = new HashMap<>();
      // {"log_id":"UNITTEST_10000","version":"2.0","service_id":"S10000","session_id":"","request":{"query":"你好","user_id":"88888"},"dialog_state":{"contexts":{"SYS_REMEMBERED_SKILLS":["1057"]}}}
      entityMap.put("log_id", UUID.randomUUID());
      entityMap.put("version", "2.0");
      entityMap.put("service_id", serviceid);
      entityMap.put("session_id", context);
      entityMap.put("request", new HashMap<String, String>(){{
        put("query", question);
        put("user_id", context);
      }});
      HttpPost httpPost = new HttpPost(answerHost + "?access_token=" + accessToken);
      httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
      httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(entityMap), "UTF-8"));
      CloseableHttpResponse response = httpClient.execute(httpPost);
      Map<String, Object> responseData = objectMapper.readValue(EntityUtils.toString(response.getEntity()), HashMap.class);

      if ((Integer) responseData.get("error_code") != 0) {
        throw new IllegalStateException((String) responseData.get("error_msg"));
      }

      for (Map responseEntry : (List<Map>) ((Map) responseData.get("result")).get("response_list")) {
        if ((Integer) responseEntry.get("status") == 0) {
          List actionList = (List) responseEntry.get("action_list");
          if (actionList.size() == 0) continue;
          return (String) ((Map)actionList.get(0)).get("say");
        }
      }

      return "no satisfied answer";

    } catch (Exception e) {
      throw new IllegalStateException(e.getMessage(), e);
    }

  }

  private String retrieveAccessToken() throws URISyntaxException, IOException {
    if (nextTokenRetrieveTime == null || System.currentTimeMillis() > nextTokenRetrieveTime) {
      // a new token should be generated

      URIBuilder builder = new URIBuilder(tokenHost)
        .addParameter("grant_type", "client_credentials")
        .addParameter("client_id", apikey)
        .addParameter("client_secret", secretkey);

      HttpGet httpGet = new HttpGet(builder.build());
      CloseableHttpResponse response = httpClient.execute(httpGet);
      Map<String, Object> responseData = objectMapper.readValue(EntityUtils.toString(response.getEntity()), HashMap.class);

      if (responseData.get("error") != null) {
        throw new IllegalStateException((String) responseData.get("error_description"));
      } else {
        accessToken = (String) responseData.get("access_token");
        Integer expires = (Integer) responseData.get("expires_in");
        nextTokenRetrieveTime = System.currentTimeMillis() + (expires * 1000l) - (24 * 60 * 60 * 1000l);
        return accessToken;
      }

    } else {
      return accessToken;
    }
  }



}
