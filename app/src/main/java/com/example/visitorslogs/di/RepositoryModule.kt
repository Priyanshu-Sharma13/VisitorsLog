package com.example.visitorslogs.di

import com.example.visitorslogs.data.repository.AdminUserRepositoryImpl
import com.example.visitorslogs.data.repository.AuthRepositoryImpl
import com.example.visitorslogs.data.repository.ComplaintRepositoryImpl
import com.example.visitorslogs.data.repository.DeliveryRepositoryImpl
import com.example.visitorslogs.data.repository.NoticeRepositoryImpl
import com.example.visitorslogs.data.repository.VisitorRepositoryImpl
import com.example.visitorslogs.domain.repository.AdminUserRepository
import com.example.visitorslogs.domain.repository.AuthRepository
import com.example.visitorslogs.domain.repository.ComplaintRepository
import com.example.visitorslogs.domain.repository.DeliveryRepository
import com.example.visitorslogs.domain.repository.NoticeRepository
import com.example.visitorslogs.domain.repository.SocietyRepository
import com.example.visitorslogs.data.repository.SocietyRepositoryImpl
import com.example.visitorslogs.domain.repository.VisitorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    abstract fun bindVisitorRepository(
        visitorRepositoryImpl: VisitorRepositoryImpl
    ): VisitorRepository

    @Binds
    abstract fun bindDeliveryRepository(
        deliveryRepositoryImpl: DeliveryRepositoryImpl
    ): DeliveryRepository

    @Binds
    abstract fun bindNoticeRepository(
        noticeRepositoryImpl: NoticeRepositoryImpl
    ): NoticeRepository

    @Binds
    abstract fun bindComplaintRepository(
        complaintRepositoryImpl: ComplaintRepositoryImpl
    ): ComplaintRepository

    @Binds
    abstract fun bindAdminUserRepository(
        adminUserRepositoryImpl: AdminUserRepositoryImpl
    ): AdminUserRepository

    @Binds
    abstract fun bindSocietyRepository(
        societyRepositoryImpl: SocietyRepositoryImpl
    ): SocietyRepository
}
