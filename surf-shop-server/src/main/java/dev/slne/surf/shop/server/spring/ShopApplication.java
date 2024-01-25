package dev.slne.surf.shop.server.spring;

import dev.slne.data.api.DataApi;
import dev.slne.data.api.spring.SurfSpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SurfSpringApplication(
        scanBasePackages = "dev.slne.surf.shop",
        entityScanPackages = "dev.slne.surf.shop.server",
        baseJpaPackages = "dev.slne.surf.shop.server.spring.repository.jpa",
        baseRedisPackages = "dev.slne.surf.shop.server.spring.repository.redis"
)
public class ShopApplication {

    public static ConfigurableApplicationContext run(ClassLoader classLoader) {
        return DataApi.run(ShopApplication.class, classLoader);
    }
}
