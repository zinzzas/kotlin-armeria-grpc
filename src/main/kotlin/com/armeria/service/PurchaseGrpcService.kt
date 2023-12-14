package com.armeria.service

import com.armeria.component.PurchaseComponent
import com.armeria.dto.PurchaseDTO.Companion.toPurchaseProto
import com.armeria.dto.PurchaseResponse
import com.armeria.purchase.v1.PurchaseOuterClass
import com.armeria.purchase.v1.PurchaseOuterClass.ListPurchasesResponse
import com.armeria.purchase.v1.PurchaseServiceGrpcKt
import com.armeria.support.decorator.datadog.DatadogTracerService
import com.google.protobuf.Empty
import com.linecorp.armeria.server.grpc.GrpcExceptionHandler
import com.armeria.support.decorator.error.GlobalGrpcExceptionHandler
import com.linecorp.armeria.server.annotation.Decorator
import com.linecorp.armeria.server.annotation.Decorators
import org.springframework.stereotype.Service


@Service
@Decorator(DatadogTracerService::class)
@GrpcExceptionHandler(GlobalGrpcExceptionHandler::class)
class PurchaseGrpcService(private val purchaseComponent: PurchaseComponent): PurchaseServiceGrpcKt.PurchaseServiceCoroutineImplBase() {

    override suspend fun getPurchase(request: PurchaseOuterClass.GetPurchaseRequest): PurchaseOuterClass.GetPurchaseResponse {
        val purchase: PurchaseOuterClass.Purchase? = purchaseComponent.getPurchase(request.purchaseNo)?.let {
            toPurchaseProto(it)
        }

        return PurchaseOuterClass.GetPurchaseResponse.newBuilder()
            .setPurchase(purchase)
            .build()
    }

    override suspend fun listPurchases(request: PurchaseOuterClass.ListPurchasesRequest): PurchaseOuterClass.ListPurchasesResponse {
        val purchaseResponse: PurchaseResponse = purchaseComponent.listPurchases(request.purchaseNo)

        val purchases: List<PurchaseOuterClass.Purchase> = purchaseResponse.contents
            .mapNotNull { toPurchaseProto(it) }

        return ListPurchasesResponse.newBuilder()
            .addAllPurchases(purchases)
            .setHasNext(purchaseResponse.hasNext)
            .build()
    }

    override suspend fun createPurchase(request: PurchaseOuterClass.CreatePurchaseRequest): Empty {

        return Empty.getDefaultInstance()
    }

    override suspend fun deletePurchase(request: PurchaseOuterClass.DeletePurchaseRequest): Empty {

        return Empty.getDefaultInstance()
    }
}