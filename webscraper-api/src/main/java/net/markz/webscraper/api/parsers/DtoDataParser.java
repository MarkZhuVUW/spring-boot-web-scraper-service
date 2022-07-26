package net.markz.webscraper.api.parsers;

import net.markz.webscraper.api.daos.searchdao.OnlineShoppingItem;
import net.markz.webscraper.model.OnlineShoppingItemDto;

import java.time.LocalDateTime;

public record DtoDataParser() {

    public static OnlineShoppingItem parseDto(OnlineShoppingItemDto onlineShoppingItemDto) {
        return OnlineShoppingItem
                .builder()
                .href(onlineShoppingItemDto.getHref())
                .imageUrl(onlineShoppingItemDto.getImageUrl())
                .isSaved(onlineShoppingItemDto.isIsSaved())
                .name(onlineShoppingItemDto.getName())
                .salePrice(onlineShoppingItemDto.getSalePrice())
                .onlineShop(onlineShoppingItemDto.getOnlineShop())
                .onlineShopName(onlineShoppingItemDto.getOnlineShopName())
                .uuid(onlineShoppingItemDto.getUuid())
                .userId(onlineShoppingItemDto.getUserId())
                .lastModifiedDate(
                        onlineShoppingItemDto.getLastModifiedDate() != null ?
                                LocalDateTime.parse(onlineShoppingItemDto.getLastModifiedDate()) :
                                null
                )
                .build();
    }

    public static OnlineShoppingItemDto parseData(OnlineShoppingItem onlineShoppingItem) {
        return new OnlineShoppingItemDto()
                .href(onlineShoppingItem.getHref())
                .imageUrl(onlineShoppingItem.getImageUrl())
                .isSaved(onlineShoppingItem.getIsSaved())
                .name(onlineShoppingItem.getName())
                .salePrice(onlineShoppingItem.getSalePrice())
                .onlineShop(onlineShoppingItem.getOnlineShop())
                .onlineShopName(onlineShoppingItem.getOnlineShopName())
                .uuid(onlineShoppingItem.getUuid())
                .userId(onlineShoppingItem.getUserId())
                .lastModifiedDate(onlineShoppingItem.getLastModifiedDate().toString());
    }
}
