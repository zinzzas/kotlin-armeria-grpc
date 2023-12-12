package com.armeria.domain

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
data class Purchase(
    val purchaseNo: String,
    val customerId: String,
    val customerName: String,
    val productId: Long,
    val productName: String,
    val price: Int,
    val quantity: Int,
    @EncodeDefault val purchaseDate: String = "20231122102099",
)