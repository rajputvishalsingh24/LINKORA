import { Controller, Post, Body, Req, Headers, UseGuards } from '@nestjs/common';
import { BillingService } from './billing.service';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { CurrentUser } from '../auth/decorators/current-user.decorator';

@Controller('billing')
export class BillingController {
  constructor(private readonly billingService: BillingService) {}

  @Post('checkout')
  @UseGuards(JwtAuthGuard)
  async createCheckout(
    @CurrentUser('id') userId: string,
    @Body('plan') plan: 'STARTER' | 'GROWTH',
  ) {
    return this.billingService.createSubscriptionCheckout(userId, plan || 'STARTER');
  }

  @Post('webhook')
  async webhook(
    @Headers('stripe-signature') signature: string,
    @Req() request: any,
  ) {
    return this.billingService.handleWebhook(signature, request.rawBody || Buffer.from(''));
  }
}
