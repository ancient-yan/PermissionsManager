package com.gwchina.sdk.base.linker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.android.base.app.activity.BaseActivity
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.models.logined
import com.gwchina.sdk.base.router.RouterPath
import timber.log.Timber

/**
 * 外部打开 app 内部页面的中间页，具体参考 [文档说明](http://172.168.50.230/chenhg/ios_document/wikis/%E5%8E%9F%E7%94%9F%E6%8E%A5%E5%8F%A3)
 *
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-05-16 17:27
 */
class SchemeActivity : BaseActivity() {

    override fun layout(): Nothing? = null

    override fun setupView(savedInstanceState: Bundle?) {
        try {
            processIntent(intent)
        } catch (ignore: Exception) {
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        try {
            processIntent(intent)
        } catch (ignore: Exception) {
        }
    }

    private fun processIntent(intent: Intent) {
        val uri = intent.data
        if (uri != null) {
            Timber.d("receive scheme = $uri")
            val urlEntity = UrlEntity.from(uri.toString())
            if (urlEntity != null) {
                val id = urlEntity.id
                if (id != null) {
                    if (!jump(id, urlEntity.params)) {
                        supportFinishAfterTransition()
                    }
                    return
                }
            }
        }
        supportFinishAfterTransition()
    }

    private fun jump(id: String, params: Map<String, String>): Boolean {
        val jumpable = SchemeJumper.createJumpable(id)
        if (jumpable != null) {
            if (jumpable.requestLogin() && requireLogined()) {
                jumpable.jump(this, params)
                supportFinishAfterTransition()
            }
            return true
        }
        return false
    }

    private fun requireLogined(): Boolean {
        val appRouter = AppContext.appRouter()
        if (AppContext.appDataSource().user().logined()) {
            return true
        }
        appRouter.build(RouterPath.Account.PATH).navigation(this, RouterPath.Account.REQUEST_CODE)
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == RouterPath.Account.REQUEST_CODE) {
            processIntent(intent)
        } else {
            supportFinishAfterTransition()
        }
    }

}