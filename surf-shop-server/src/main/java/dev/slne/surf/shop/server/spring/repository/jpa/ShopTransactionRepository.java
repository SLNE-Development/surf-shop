package dev.slne.surf.shop.server.spring.repository.jpa;

import dev.slne.surf.shop.server.shop.transaction.ServerShopTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopTransactionRepository extends JpaRepository<ServerShopTransaction, Long> {
}
