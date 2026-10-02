import { Injectable, InternalServerErrorException, Logger, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class BillingService {
  private readonly logger = new Logger(BillingService.name);

  // Stripe Price IDs mapped to Linkora tiers
  private readonly PLANS: Record<string, string> = {
    STARTER: process.env.STRIPE_PRICE_STARTER || 'price_starter_999', // ₹999/month
    GROWTH: process.env.STRIPE_PRICE_GROWTH || 'price_growth_4999',   // ₹4,999/month
  };

  constructor(private prisma: PrismaService) {}

  /**
   * Create a Stripe Checkout Session for a new subscription
   */
  async createSubscriptionCheckout(userId: string, planType: 'STARTER' | 'GROWTH') {
    try {
      const user = await this.prisma.user.findUnique({ where: { id: userId } });
      if (!user) {
        throw new NotFoundException(`User with ID ${userId} not found.`);
      }

      const frontendUrl = process.env.FRONTEND_URL || 'http://localhost:3000';

      if (process.env.STRIPE_SECRET_KEY) {
        const { default: Stripe } = await import('stripe');
        const stripe = new Stripe(process.env.STRIPE_SECRET_KEY, {
          apiVersion: '2023-10-16' as any,
        });

        const session = await stripe.checkout.sessions.create({
          payment_method_types: ['card'],
          mode: 'subscription',
          customer_email: user.email,
          line_items: [
            {
              price: this.PLANS[planType] || this.PLANS.STARTER,
              quantity: 1,
            },
          ],
          success_url: `${frontendUrl}/dashboard?session_id={CHECKOUT_SESSION_ID}&plan=${planType}`,
          cancel_url: `${frontendUrl}/pricing`,
          metadata: {
            userId: user.id,
            plan: planType,
          },
        });

        return {
          success: true,
          url: session.url,
          sessionId: session.id,
          plan: planType,
        };
      }

      // Development / sandbox checkout session link
      this.logger.log(`Generated simulated sandbox checkout session for User ${userId}, Plan: ${planType}`);
      const mockSessionId = `cs_test_${Date.now()}`;
      return {
        success: true,
        url: `${frontendUrl}/dashboard?session_id=${mockSessionId}&plan=${planType}&mock_payment=success`,
        sessionId: mockSessionId,
        plan: planType,
        mode: 'sandbox_simulation',
      };
    } catch (error) {
      this.logger.error('Failed to initialize payment gateway checkout:', error);
      throw new InternalServerErrorException('Failed to initialize payment gateway');
    }
  }

  /**
   * Handle Stripe Webhooks (subscription renewal, checkout completed)
   */
  async handleWebhook(signature: string, payload: Buffer) {
    if (!process.env.STRIPE_SECRET_KEY || !process.env.STRIPE_WEBHOOK_SECRET) {
      this.logger.warn('Stripe secrets not provided; skipping cryptographic webhook verification.');
      return { received: true, note: 'Simulated webhook processed' };
    }

    try {
      const { default: Stripe } = await import('stripe');
      const stripe = new Stripe(process.env.STRIPE_SECRET_KEY, {
        apiVersion: '2023-10-16' as any,
      });

      const event = stripe.webhooks.constructEvent(
        payload,
        signature,
        process.env.STRIPE_WEBHOOK_SECRET,
      );

      if (event.type === 'checkout.session.completed') {
        const session = event.data.object as any;
        const userId = session.metadata?.userId;
        const plan = session.metadata?.plan || 'STARTER';

        if (userId) {
          // Log payment record in PostgreSQL
          await this.prisma.payment.create({
            data: {
              amount: (session.amount_total || 99900) / 100,
              currency: session.currency?.toUpperCase() || 'INR',
              paymentMethod: 'STRIPE_SUBSCRIPTION',
              paymentStatus: 'PAID',
              transactionRef: session.id,
            },
          });

          this.logger.log(`User ${userId} successfully upgraded to ${plan} tier.`);
        }
      }

      return { received: true };
    } catch (err: any) {
      this.logger.error(`Stripe Webhook Verification Error: ${err.message}`);
      throw new InternalServerErrorException(`Webhook Error: ${err.message}`);
    }
  }
}
