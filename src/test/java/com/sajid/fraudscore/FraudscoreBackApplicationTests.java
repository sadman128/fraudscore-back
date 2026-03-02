package com.sajid.fraudscore;

import com.sajid.fraudscore.service.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
class FraudscoreBackApplicationTests {


    @Autowired
    public AiService aiService;

}
