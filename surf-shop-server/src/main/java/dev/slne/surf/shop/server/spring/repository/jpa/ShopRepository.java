package dev.slne.surf.shop.server.spring.repository.jpa;

import dev.slne.surf.shop.server.shop.ServerShop;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<ServerShop, Long> {

    @CachePut(cacheNames = "Shop", key = "#result.uuid")
    @Override
    <S extends ServerShop> @NotNull S save(@NotNull S entity);

    @Cacheable(cacheNames = "Shop")
    @Override
    @NotNull
    List<ServerShop> findAll();
}
