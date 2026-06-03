package com.vocabverse;

import com.vocabverse.auth.repository.RefreshTokenRepository;
import com.vocabverse.collection.repository.CollectionRepository;
import com.vocabverse.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class VocabVerseApplicationTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private CollectionRepository collectionRepository;

    @Test
    void contextLoads() {
    }
}
