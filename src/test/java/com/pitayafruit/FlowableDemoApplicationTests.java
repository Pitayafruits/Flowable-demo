package com.pitayafruit;

import com.pitayafruit.domain.User;
import com.pitayafruit.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FlowableDemoApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelect(){
        User user = userMapper.selectById(1L);
        System.out.println(user);
    }

}
