package io.github.watertao.xyao;

import io.github.watertao.xyao.infras.Chatter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class TestChatter {

  @Autowired
  private Chatter chatter;

  @Test
  public void test(){
    String answer = chatter.answer("你叫什么名字", "0000");

    System.out.println(answer);

    answer = chatter.answer("你好", "0000");

    System.out.println(answer);
  }

}
