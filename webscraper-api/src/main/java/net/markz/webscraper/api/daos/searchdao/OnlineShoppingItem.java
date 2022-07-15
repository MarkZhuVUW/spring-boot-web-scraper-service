package net.markz.webscraper.api.daos.searchdao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.markz.webscraper.api.daos.converters.LocalDateTimeToStringConverter;
import net.markz.webscraper.model.OnlineShopDto;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Data
@DynamoDBTable(tableName = "OnlineShoppingItems")
@NoArgsConstructor
@ToString
@EqualsAndHashCode(exclude = {"ttl", "lastModifiedDate"})
public class OnlineShoppingItem {

    @SerializedName("version")
    @DynamoDBVersionAttribute
    private Integer version;

    @SerializedName("onlineShop")
    @DynamoDBAttribute(attributeName = "onlineShop")
    @DynamoDBTypeConvertedEnum
    private OnlineShopDto onlineShop;

    @SerializedName("onlineShopName                                                                                              ")
    @DynamoDBAttribute(attributeName = "onlineShopName")
    private String onlineShopName;

    @SerializedName("salePrice")
    @DynamoDBAttribute(attributeName = "salePrice")
    private String salePrice;

    @SerializedName("name")
    @DynamoDBAttribute(attributeName = "name")
    private String name;

    @SerializedName("isSaved")
    @DynamoDBAttribute(attributeName = "isSaved")
    private Boolean isSaved;

    @SerializedName("imageUrl")
    @DynamoDBAttribute(attributeName = "imageUrl")
    private String imageUrl;

    @SerializedName("href")
    @DynamoDBAttribute(attributeName = "href")
    private String href;

    @SerializedName("uuid")
    @DynamoDBAttribute(attributeName = "uuid")
    private String uuid;

    @SerializedName("userId")
    @DynamoDBAttribute(attributeName = "userId")
    private String userId;

    @SerializedName("lastModifiedDate")
    @DynamoDBAttribute(attributeName = "lastModifiedDate")
    @DynamoDBTypeConverted(converter = LocalDateTimeToStringConverter.class)
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
    private LocalDateTime lastModifiedDate;


    // Use this attribute to let dynamo delete expired records.
    @SerializedName("ttl")
    @DynamoDBAttribute(attributeName = "ttl")
    private long ttl;

    public LocalDateTime getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public void setLastModifiedDate(final LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    @DynamoDBHashKey(attributeName = "PK")
    public String getPK() {
        return String.format("USERID#%s", this.userId);
    }

    public void setPK(String pk) {
        // intentionally left blank
    }

    @DynamoDBRangeKey(attributeName = "SK")
    public String getSK() {
        return String.format("ITEM#%s#%s", this.onlineShopName, this.name);
    }

    public void setSK(String sk) {
        // intentionally left blank
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
