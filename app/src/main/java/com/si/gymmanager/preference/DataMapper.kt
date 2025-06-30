package com.si.gymmanager.preference

import com.si.gymmanager.datamodels.UserDataModel

// User mappings
fun UserDataModel.toEntity(): UserEntity {
    return UserEntity(
        id = this.id ?: "",
        name = this.name,
        subscriptionStart = this.subscriptionStart,
        subscriptionEnd = this.subscriptionEnd,
        amountPaid = this.amountPaid,
        aadhaarNumber = this.aadhaarNumber,
        address = this.address,
        phone = this.phone,
        lastUpdateDate = this.lastUpdateDate
    )
}

fun UserEntity.toDataModel(): UserDataModel {
    return UserDataModel(
        id = this.id,
        name = this.name,
        subscriptionStart = this.subscriptionStart,
        subscriptionEnd = this.subscriptionEnd,
        amountPaid = this.amountPaid,
        aadhaarNumber = this.aadhaarNumber,
        address = this.address,
        phone = this.phone,
        lastUpdateDate = this.lastUpdateDate
    )
}

fun List<UserEntity>.toDataModels(): List<UserDataModel> {
    return this.map { it.toDataModel() }
}

fun List<UserDataModel>.toEntities(): List<UserEntity> {
    return this.map { it.toEntity() }
}

