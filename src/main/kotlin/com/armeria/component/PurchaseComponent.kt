package com.armeria.component

import com.armeria.domain.PurchaseRepository
import com.armeria.dto.PurchaseDTO
import com.armeria.dto.PurchaseDTO.Companion.toPurchaseDTO
import com.armeria.dto.PurchaseResponse
import org.springframework.stereotype.Component

@Component
class PurchaseComponent(private val purchaseRepository: PurchaseRepository) {
    suspend fun getPurchase(purchaseNo: String): PurchaseDTO {
        return toPurchaseDTO(purchaseRepository.findOneByPurchaseNo(purchaseNo))
    }

    suspend fun listPurchases(purchaseNo: String): PurchaseResponse {
        return purchaseRepository.findPurchaseAll(purchaseNo).map { toPurchaseDTO(it) }
            .let {
                PurchaseResponse(
                    contents = it.content,
                    hasNext = if(it.hasNext()) "Y" else "N",
                )
            }
    }
}