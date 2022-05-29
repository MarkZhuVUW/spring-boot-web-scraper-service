package net.markz.webscraper.api.daos.searchdao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.markz.webscraper.api.daos.converters.LocalDateConverter;
import net.markz.webscraper.model.OnlineShopDto;

import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Data
@DynamoDBTable(tableName = "OnlineShoppingItems")
@NoArgsConstructor
public class OnlineShoppingItem {

    @DynamoDBAttribute(attributeName = "onlineShop")
    @DynamoDBTypeConvertedEnum
    private OnlineShopDto onlineShop;

    @DynamoDBAttribute(attributeName = "onlineShopName")
    private String onlineShopName;

    @DynamoDBAttribute(attributeName = "salePrice")
    private String salePrice;

    @DynamoDBAttribute(attributeName = "name")
    private String name;

    @DynamoDBAttribute(attributeName = "isSaved")
    private Boolean isSaved;

    @DynamoDBAttribute(attributeName = "imageUrl")
    private String imageUrl;

    @DynamoDBAttribute(attributeName = "href")
    private String href;

    @DynamoDBAttribute(attributeName = "uuid")
    private String uuid;

    @DynamoDBAttribute(attributeName = "userId")
    private String userId;

    @DynamoDBAttribute(attributeName = "lastModifiedDate")
    @DynamoDBTypeConverted(converter = LocalDateConverter.class)
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
    private LocalDate lastModifiedDate;

    // Use this attribute to let dynamo delete expired records.
    @DynamoDBAttribute(attributeName = "ttl")
    private long ttl;

    public LocalDate getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public void setLastModifiedDate(final LocalDate lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    @DynamoDBHashKey(attributeName = "PK")
    public String getPK() {
        return String.format("ITEM#%s", this.userId);
    }

    public void setPK(String pk) {
        // intentionally left blank: PK is set by setting liftNumber attribute
    }

    @DynamoDBRangeKey(attributeName = "SK")
    public String getSK() {
        return "DATE#" + this.lastModifiedDate;
    }

    public void setSK(String sk) {
        // intentionally left blank: SK is set by setting the date attribute
    }

//    @DynamoDBIndexHashKey(attributeName = "GSI_1_PK", globalSecondaryIndexName = "GSI_1")
//    public String getGSI1PK() {
//        return getPK();
//    }
//
//    public void setGSI1PK(String gsi1Pk) {
//        // intentionally left blank: PK is set by setting liftNumber attribute
//    }
//
//    @DynamoDBIndexRangeKey(attributeName = "GSI_1_SK", globalSecondaryIndexName = "GSI_1")
//    public String getGSI1SK() {
//        return "TOTAL_UNIQUE_LIF_RIDERS#" + totalUniqueLiftRiders;
//    }
//
//    public void setGSI1SK(String gsi1Sk) {
//        // intentionally left blank: SK is set by setting the totalUniqueLiftRiders attribute
//    }
}
