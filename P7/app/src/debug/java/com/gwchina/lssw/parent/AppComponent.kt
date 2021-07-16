package com.gwchina.lssw.parent

import com.gwchina.parent.account.injection.AccountModule
import com.gwchina.parent.apps.injection.AppGuardModule
import com.gwchina.parent.binding.injection.BindingModule
import com.gwchina.parent.daily.injection.DailyModule
import com.gwchina.parent.family.injection.FamilyPhoneModel
import com.gwchina.parent.level.injection.GuardLevelModule
import com.gwchina.parent.main.injection.ChildLocationModule
import com.gwchina.parent.main.injection.MainModule
import com.gwchina.parent.member.injection.MemberModule
import com.gwchina.parent.message.injection.MessageModule
import com.gwchina.parent.migration.injection.MigrationModule
import com.gwchina.parent.net.injection.NetGuardModule
import com.gwchina.parent.profile.injection.ProfileCenterModule
import com.gwchina.parent.recommend.injection.RecommendModule
import com.gwchina.parent.screenshot.injection.ScreenshotModule
import com.gwchina.parent.times.injection.TimeGuardModule
import com.gwchina.sdk.base.di.AppModule
import com.gwchina.sdk.base.di.DataModule
import com.gwchina.sdk.debug.DebugActivityModule
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-10-12 21:53
 */
@Component(modules = [
    //base
    AppModule::class,
    DataModule::class,
    AndroidInjectionModule::class,
    AndroidSupportInjectionModule::class,
    //debug
    DebugActivityModule::class,
    //modules
    MainModule::class,
    ChildLocationModule::class,
    AccountModule::class,
    BindingModule::class,
    GuardLevelModule::class,
    TimeGuardModule::class,
    AppGuardModule::class,
    NetGuardModule::class,
    FamilyPhoneModel::class,
    RecommendModule::class,
    MemberModule::class,
    ProfileCenterModule::class,
    MessageModule::class,
    DailyModule::class,
    MigrationModule::class,
    ScreenshotModule::class
])
@Singleton
interface AppComponent {

    fun inject(gwAppContext: GWAppContext)

}


