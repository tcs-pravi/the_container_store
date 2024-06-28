package com.containerstore.offer.domain;

public class OfferOrderLineBuilder {

    private String lineId;
    private long skuNumber;
    private com.containerstore.sku.domain.Sku sku;
    private int quantity;
    private com.containerstore.common.base.money.Money preOfferPrice;
    private Integer registryId;
    private String splitId;
    private String spaceUseId;
    private String spaceSource;
    private String spaceId;
    private java.util.List<OrderLineAdjustment> lineAdjustments;
    private AdditionalShippingFeeAdjustment additionalShippingFeeAdjustment;

    public static OfferOrderLineBuilder builder() {
        return new OfferOrderLineBuilder();
    }

    public OfferOrderLineBuilder withLineId(String lineId) {
        this.lineId = lineId;
        return this;
    }

    public OfferOrderLineBuilder withSkuNumber(long skuNumber) {
        this.skuNumber = skuNumber;
        return this;
    }

    public OfferOrderLineBuilder withSku(com.containerstore.sku.domain.Sku sku) {
        this.sku = sku;
        return this;
    }

    public OfferOrderLineBuilder withQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public OfferOrderLineBuilder withPreOfferPrice(com.containerstore.common.base.money.Money preOfferPrice) {
        this.preOfferPrice = preOfferPrice;
        return this;
    }

    public OfferOrderLineBuilder withRegistryId(Integer registryId) {
        this.registryId = registryId;
        return this;
    }

    public OfferOrderLineBuilder withSplitId(String splitId) {
        this.splitId = splitId;
        return this;
    }

    public OfferOrderLineBuilder withSpaceUseId(String spaceUseId) {
        this.spaceUseId = spaceUseId;
        return this;
    }

    public OfferOrderLineBuilder withSpaceSource(String spaceSource) {
        this.spaceSource = spaceSource;
        return this;
    }

    public OfferOrderLineBuilder withSpaceId(String spaceId) {
        this.spaceId = spaceId;
        return this;
    }

    public OfferOrderLineBuilder withLineAdjustments(java.util.List<OrderLineAdjustment> lineAdjustments) {
        this.lineAdjustments = lineAdjustments;
        return this;
    }

    public OfferOrderLineBuilder withAdditionalShippingFeeAdjustment(AdditionalShippingFeeAdjustment additionalShippingFeeAdjustment) {
        this.additionalShippingFeeAdjustment = additionalShippingFeeAdjustment;
        return this;
    }

    public OfferOrderLine build() {
        return new OfferOrderLine(
                lineId,
                skuNumber,
                sku,
                quantity,
                preOfferPrice,
                registryId,
                splitId,
                spaceUseId,
                spaceSource,
                spaceId,
                lineAdjustments,
                additionalShippingFeeAdjustment
        );
    }
}
