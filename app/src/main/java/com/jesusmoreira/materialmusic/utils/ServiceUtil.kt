package com.jesusmoreira.materialmusic.utils

import android.app.Activity
import android.content.*
import android.os.IBinder
import com.jesusmoreira.materialmusic.services.IPlaybackService
import com.jesusmoreira.materialmusic.services.PlaybackService
import java.util.*

/**
 * Provides convenience methods for binding to the service
 */
object ServiceUtil {

    const val TAG: String = "ServiceUtil"

    var sService: IPlaybackService? = null

    private val sConnectionMap: WeakHashMap<Context, ServiceBinder> = WeakHashMap()

    /**
     * @param context  The {@link Context} to use
     * @param callback The {@link ServiceConnection} to use
     * @return The new instance of {@link ServiceToken}
     */
    fun bindToService(context: Context, callback: ServiceConnection): ServiceToken? {
        val realActivity: Activity = (context as Activity).parent ?: context

        val contextWrapper: ContextWrapper = ContextWrapper(realActivity)
        contextWrapper.startService(Intent(contextWrapper, PlaybackService::class.java))
        val binder: ServiceBinder = ServiceBinder(callback)
        if (contextWrapper.bindService(
                Intent().setClass(contextWrapper, PlaybackService::class.java),
                binder, 0)) {
            sConnectionMap.put(contextWrapper, binder)
            return ServiceToken(contextWrapper)
        }
        return null
    }

    /**
     * @param token The {@link ServiceToken} to unbind from
     */
    fun unbindFromService(token: ServiceToken) {
        val contextWrapper: ContextWrapper = token.wrappedContext ?: return
        val binder: ServiceBinder = sConnectionMap.remove(contextWrapper) ?: return

        contextWrapper.unbindService(binder)
        if (sConnectionMap.isEmpty()) sService = null
    }

    open class ServiceBinder(private val callback: ServiceConnection?): ServiceConnection {

        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            sService = IPlaybackService.Stub.asInterface(service)
            callback?.onServiceConnected(className, service)
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            callback?.onServiceDisconnected(className)
            sService = null
        }
    }

    class ServiceToken
    /**
     * Constructor of <code>ServiceToken</code>
     *
     * @param context The {@link ContextWrapper} to use
     */(context: ContextWrapper) {
        var wrappedContext: ContextWrapper? = context
    }
}