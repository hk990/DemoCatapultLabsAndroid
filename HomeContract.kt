package io.connect.app.activities.home

import io.connect.app.base.BaseContract
import io.connect.app.base.BaseModel
import java.util.*

interface HomeContract {
    interface View : BaseContract.View {
        fun updateUI(certificate: String)
        fun updateUIOnError1(message: String)
        fun updateUserDetail(userDetailModel: UserDetailModel)
    }

    interface Actions : BaseContract.Actions {
        fun getVPNConfigFile()
        fun getUserDetails()
    }
}