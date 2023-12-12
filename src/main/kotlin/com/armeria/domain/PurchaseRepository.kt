package com.armeria.domain

import kotlinx.serialization.json.Json
import org.springframework.core.io.ResourceLoader
import org.springframework.data.domain.*
import org.springframework.stereotype.Repository

@Repository
class PurchaseRepository(
    private val resourceLoader: ResourceLoader
) {
    suspend fun findOneByPurchaseNo(purchaseNo: String): Purchase {
        val resource = resourceLoader.getResource("classpath:dummy/purchase-dummy.json")
        val jsonContent = resource.inputStream.bufferedReader().use { it.readText() }
        return Json.decodeFromString(jsonContent)
    }

    suspend fun findPurchaseAll(purchseNo: String): Slice<Purchase> {
        //val pageable: Pageable = PageRequest.of(pageNo - 1, pageSize, Sort.Direction.DESC, "field")
        //return mongoTemplate.find(Query(Criteria.where("purchaseId").`is`(purchaseId)), Purchase::class.java).awaitSingle()
        val pageable: Pageable = PageRequest.of(1, 10, Sort.Direction.DESC, "purchaseDate")
        val resource = resourceLoader.getResource("classpath:dummy/purchases-dummy.json")
        val jsonContent = resource.inputStream.bufferedReader().use { it.readText() }
        val purchases: List<Purchase> = Json.decodeFromString(jsonContent)

        val hasNext = purchases.size > 10
        val newPurchases = if (hasNext) {
            purchases.dropLast(1)
        } else {
            purchases
        }

        return SliceImpl(newPurchases, pageable, hasNext)
    }
}