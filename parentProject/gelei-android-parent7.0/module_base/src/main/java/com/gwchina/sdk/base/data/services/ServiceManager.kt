package com.gwchina.sdk.base.data.services

import com.android.base.rx.SchedulerProvider
import com.android.sdk.net.service.ServiceFactory

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-15 12:44
 */
class ServiceManager internal constructor(
        private val serviceFactory: ServiceFactory,
        private val schedulerProvider: SchedulerProvider
) {

    val instructionSyncService: InstructionSyncService by lazy {
        InstructionSyncServiceImpl(serviceFactory)
    }

    fun newFileUploadService(): FileUploadService {
        return FileUploadServiceImpl(serviceFactory, schedulerProvider)
    }

}