package com.romeodev.starter_features.auth.domain.repository

import com.romeodev.core.utils.network_utils.RequestState
import com.romeodev.starter_features.auth.domain.enums.SignInMethod
import com.romeodev.starter_features.auth.domain.models.UserData
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(
        name: String,
        email: String,
        password: String,
    ): Flow<RequestState<Boolean>>

    suspend fun signIn(
        email: String,
        password: String,
    ): Flow<RequestState<Boolean>>

    suspend fun createUserInDatabase(
        userData: UserData,
    )

    suspend fun checkIfUserAlreadyExistInDatabase(): Boolean

    suspend fun signOut(): Flow<RequestState<SignInMethod>>

    suspend fun forgetPassword(email: String): Flow<RequestState<Boolean>>

    suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
    ): Flow<RequestState<Boolean>>

    suspend fun deleteAccount(password: String): Flow<RequestState<Boolean>>
}
