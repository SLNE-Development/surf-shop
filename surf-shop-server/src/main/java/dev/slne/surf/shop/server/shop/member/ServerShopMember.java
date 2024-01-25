package dev.slne.surf.shop.server.shop.member;

import dev.slne.data.api.DataApi;
import dev.slne.surf.shop.api.ShopApi;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.member.ShopMember;
import dev.slne.surf.shop.server.shop.ServerShop;
import dev.slne.surf.shop.server.spring.repository.jpa.ShopMemberRepository;
import jakarta.persistence.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Entity(name = "ShopMember")
@Table(name = "shop_members")
public class ServerShopMember implements ShopMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @JdbcTypeCode(SqlTypes.BIGINT)
    private Long id = -1L;

    @Column(name = "member_uuid", nullable = false, length = 36)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID memberUuid;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private ServerShop serverShop;

    protected ServerShopMember() {
        // JPA
    }

    /**
     * A new {@link ServerShopMember} instance
     *
     * @param shop       the shop
     * @param memberUuid the member uuid
     */
    public ServerShopMember(ServerShop shop, UUID memberUuid) {
        this.serverShop = shop;
        this.memberUuid = memberUuid;
    }

    public CompletableFuture<ServerShopMember> save() {
        return CompletableFuture.supplyAsync(() -> ShopApi.getContext().getBean(ShopMemberRepository.class).save(this))
                .exceptionally(throwable -> {
                    DataApi.getDataInstance().logError(getClass(), "Failed to save shop member", throwable);
                    return null;
                });
    }

    public CompletableFuture<Void> delete() {
        return CompletableFuture.runAsync(() -> ShopApi.getContext().getBean(ShopMemberRepository.class).delete(this))
                .exceptionally(throwable -> {
                    DataApi.getDataInstance().logError(getClass(), "Failed to delete shop member", throwable);
                    return null;
                });
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @return the memberUuid
     */
    public UUID getUuid() {
        return memberUuid;
    }

    /**
     * Returns the {@link Player} instance of the member
     *
     * @return the {@link Player} instance
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(memberUuid);
    }

    /**
     * Returns the {@link ServerShop} instance of the member
     *
     * @return the {@link ServerShop} instance
     */
    public Shop getShop() {
        return serverShop;
    }

    @Override
    public ShopMember inter() {
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ServerShopMember that = (ServerShopMember) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
