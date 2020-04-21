package com.imlaidian.laidianclient.data.remote.model

import com.google.gson.annotations.SerializedName

object TerminalDetailBean {

    data class Response(
            @SerializedName("status") val status: String?,
            @SerializedName("data") val data: TerminalDetailData
    )

    data class TerminalDetailData(
            @SerializedName("TERMINAL") val terminalDetail: Terminal,
            @SerializedName("TERMINAL_CATEGORY") val terminalCategory: TerminalCategory,
            @SerializedName("TERMINAL_PAYMENT") val terminalPayment: TerminalPayment
    )

    data class Terminal(
            @SerializedName("TERMINAL_ID") val terminalId: String,
            @SerializedName("TERMINAL_NAME") val terminalName: String,
            @SerializedName("TERMINAL_TYPE_ID") val terminalTypeId: Int,
            @SerializedName("TERMINAL_OPEN_TIME") val terminalOpenTime: String?,
            @SerializedName("TERMINAL_CLOSED_TIME") val terminalClosedTime: String?,
            @SerializedName("TERMINAL_ACTIVE_STATUS") val terminalActiveStatus: String,
            @SerializedName("TERMINAL_LONGITUDE") val terminalLongitude: Double,
            @SerializedName("TERMINAL_LATITUDE") val terminalLatitude: Double,
            @SerializedName("TERMINAL_ADDRESS") val terminalAddress: String?,
            @SerializedName("NETWORK_TYPE") val networkType: String?,
            @SerializedName("PROVINSI_ID") val provinsiId: String?,
            @SerializedName("KABUPATEN_ID") val kabupatenId: String?,
            @SerializedName("KECAMATAN_ID") val kecamatanId: String?,
            @SerializedName("KELURAHAN_ID") val kelurahanId: String?,
            @SerializedName("POSTAL_CODE") val postalCode: String?,
            @SerializedName("AVATAR_PICTURE") val avatarPicture: String?,
            @SerializedName("BANNER_PICTURE") val bannerPicture: String?,
            @SerializedName("DESCRIPTION") val description: String?,
            @SerializedName("METADATA") val metadata: String?
    )

    data class TerminalCategory(
            @SerializedName("TERMINAL_ID") val terminalId: String?,
            @SerializedName("TERMINAL_NAME") val terminalName: String?,
            @SerializedName("ESTABLISHMENT_ID") val establishmentId: Int,
            @SerializedName("TRAFFIC_ID") val trafficId: Int,
            @SerializedName("PARTNER_ID") val partnerId: Int,
            @SerializedName("DEPLOYMENT_DATE") val deploymentDate: String?,
            @SerializedName("SALES_ID") val salesId: Int,
            @SerializedName("CATEGORY_ID") val categoryId: Int,
            @SerializedName("CREATED_AT") val createdAt: String?,
            @SerializedName("UPDATED_AT") val updatedAt: String?
    )

    data class TerminalPayment(
            @SerializedName("ID") val id: Int,
            @SerializedName("TERMINAL_ID") val terminalId: String?,
            @SerializedName("PAYMENT_ID") val paymentId: Int,
            @SerializedName("STATUS") val status: Int,
            @SerializedName("CREATED_AT") val createdAt: String?,
            @SerializedName("CREATED_BY") val createdBy: String?,
            @SerializedName("UPDATED_AT") val updatedAt: String?,
            @SerializedName("UPDATED_BY") val updatedBy: String?
    )

}