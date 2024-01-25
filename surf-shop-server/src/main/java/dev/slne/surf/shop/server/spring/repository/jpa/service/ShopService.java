package dev.slne.surf.shop.server.spring.repository.jpa.service;

import dev.slne.surf.shop.server.shop.ServerShop;
import dev.slne.surf.shop.server.spring.repository.jpa.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.util.List;

@Service
public class ShopService {

    private final ShopRepository shopRepository;

    public ShopService(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }


    public List<ServerShop> findAll(Filter filter) {
        Class<ServerShop> serverShopClass = ServerShop.class;

        ReflectionUtils.accessibleConstructor();
        ReflectionUtils.setField();
    }


    public interface Filter {
        boolean filter(ServerShop shop);
    }
}
