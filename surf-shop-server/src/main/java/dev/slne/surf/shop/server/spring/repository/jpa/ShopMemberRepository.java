package dev.slne.surf.shop.server.spring.repository.jpa;

import dev.slne.surf.shop.server.shop.member.ServerShopMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopMemberRepository extends JpaRepository<ServerShopMember, Long> {
}
