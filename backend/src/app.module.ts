import { Module } from '@nestjs/common';
import { PrismaModule } from './prisma/prisma.module';
import { AuthModule } from './auth/auth.module';
import { UsersModule } from './users/users.module';
import { CompaniesModule } from './companies/companies.module';
import { ProductsModule } from './products/products.module';
import { AiModule } from './ai/ai.module';
import { BillingModule } from './billing/billing.module';

@Module({
  imports: [
    PrismaModule,
    AuthModule,
    UsersModule,
    CompaniesModule,
    ProductsModule,
    AiModule,
    BillingModule,
  ],
})
export class AppModule {}
