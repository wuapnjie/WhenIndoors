package com.xiaopo.flying.whenindoors.model

/**
 * @author wupanjie
 */

data class ResponseTemplate(var status: Int, var message: String)

data class DataResponseTemplate<T>(var status: Int, var message: String, var data: T)