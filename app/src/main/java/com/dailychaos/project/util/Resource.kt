package com.dailychaos.project.util

/**
 * Resource wrapper untuk handle loading states, success, dan error
 *
 * "Seperti quest di KonoSuba, setiap operasi bisa berhasil, gagal, atau sedang berjalan"
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {

    /**
     * State ketika operasi berhasil
     */
    class Success<T>(data: T, message: String? = null) : Resource<T>(data, message)

    /**
     * State ketika operasi gagal
     */
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)

    /**
     * State ketika operasi sedang berjalan
     */
    class Loading<T>(data: T? = null) : Resource<T>(data)

    /**
     * State idle (belum ada operasi)
     */
    class Idle<T> : Resource<T>()

    /**
     * Check apakah dalam state loading
     */
    fun isLoading(): Boolean = this is Loading

    /**
     * Check apakah dalam state success
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Check apakah dalam state error
     */
    fun isError(): Boolean = this is Error

    /**
     * Check apakah dalam state idle
     */
    fun isIdle(): Boolean = this is Idle

    /**
     * Get data safely, return null jika tidak ada
     */
    fun getDataOrNull(): T? = data

    /**
     * Get data with default value jika null
     */
    fun getDataOrDefault(defaultValue: T): T = data ?: defaultValue

    /**
     * Execute action hanya jika success
     */
    inline fun onSuccess(action: (T) -> Unit): Resource<T> {
        if (this is Success && data != null) {
            action(data)
        }
        return this
    }

    /**
     * Execute action hanya jika error
     */
    inline fun onError(action: (String) -> Unit): Resource<T> {
        if (this is Error) {
            action(message ?: Constants.ERROR_UNKNOWN)
        }
        return this
    }

    /**
     * Execute action hanya jika loading
     */
    inline fun onLoading(action: () -> Unit): Resource<T> {
        if (this is Loading) {
            action()
        }
        return this
    }

    companion object {
        /**
         * Create success resource
         */
        fun <T> success(data: T, message: String? = null): Resource<T> {
            return Success(data, message)
        }

        /**
         * Create error resource
         */
        fun <T> error(message: String, data: T? = null): Resource<T> {
            return Error(message, data)
        }

        /**
         * Create loading resource
         */
        fun <T> loading(data: T? = null): Resource<T> {
            return Loading(data)
        }

        /**
         * Create idle resource
         */
        fun <T> idle(): Resource<T> {
            return Idle()
        }
    }
}

/**
 * Extension function untuk map Resource data
 */
inline fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> {
    return when (this) {
        is Resource.Success -> Resource.success(transform(data!!), message)
        is Resource.Error -> Resource.error(message ?: Constants.ERROR_UNKNOWN, null)
        is Resource.Loading -> Resource.loading(null)
        is Resource.Idle -> Resource.idle()
    }
}

/**
 * Extension function untuk combine dua Resource
 */
inline fun <T, R, S> Resource<T>.combine(
    other: Resource<R>,
    transform: (T, R) -> S
): Resource<S> {
    return when {
        this is Resource.Success && other is Resource.Success -> {
            Resource.success(transform(this.data!!, other.data!!))
        }
        this is Resource.Error -> Resource.error(this.message ?: Constants.ERROR_UNKNOWN)
        other is Resource.Error -> Resource.error(other.message ?: Constants.ERROR_UNKNOWN)
        this is Resource.Loading || other is Resource.Loading -> Resource.loading()
        else -> Resource.idle()
    }
}