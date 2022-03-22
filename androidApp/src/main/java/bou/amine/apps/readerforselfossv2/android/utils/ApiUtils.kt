package bou.amine.apps.readerforselfossv2.android.utils

import bou.amine.apps.readerforselfossv2.android.api.selfoss.SuccessResponse
import retrofit2.Response

fun Response<SuccessResponse>.succeeded(): Boolean =
    this.code() === 200 && this.body() != null && this.body()!!.isSuccess