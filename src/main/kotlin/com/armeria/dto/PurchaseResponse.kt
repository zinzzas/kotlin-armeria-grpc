package com.armeria.dto

import com.armeria.domain.Purchase
import com.armeria.purchase.v1.PurchaseOuterClass

data class PurchaseResponse(
    val contents: List<PurchaseDTO> = emptyList(),
    val hasNext: String = "N",
)

data class PurchaseDTO(
    val purchaseNo: String,
    val customerId: String,
    val customerName: String,
    val productId: Long,
    val productName: String,
    val price: Int,
    val quantity: Int,
    val purchaseDate: String,
) {
    companion object {
        fun toPurchaseDTO(entity: Purchase): PurchaseDTO {
            return PurchaseDTO(
                purchaseNo = entity.purchaseNo,
                customerId = entity.customerId,
                customerName = entity.customerName,
                productId = entity.productId,
                productName = entity.productName,
                price = entity.price,
                quantity = entity.quantity,
                purchaseDate = entity.purchaseDate,
            )
        }

        fun toPurchaseProto(dto: PurchaseDTO): PurchaseOuterClass.Purchase {
            return PurchaseOuterClass.Purchase.newBuilder()
                .setPurchaseNo(dto.purchaseNo)
                .setCustomerId(dto.customerId)
                .setCustomerName(dto.customerName)
                .setProductId(dto.productId)
                .setProductName(dto.productName)
                .setPrice(dto.price)
                .setQuantity(dto.quantity)
                .setPurchaseDate(dto.purchaseDate)
                .build()
        }
    }
}

