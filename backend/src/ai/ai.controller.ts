import { Controller, Post, Body, UseGuards } from '@nestjs/common';
import { AiService } from './ai.service';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { RolesGuard } from '../auth/guards/roles.guard';
import { Roles } from '../auth/decorators/roles.decorator';

@Controller('ai')
@UseGuards(JwtAuthGuard, RolesGuard)
export class AiController {
  constructor(private readonly aiService: AiService) {}

  @Post('demand-forecast')
  @Roles('MANUFACTURER', 'ADMIN')
  async demandForecast(@Body() body: { productId: string; historicalData: any }) {
    return this.aiService.getDemandForecast(body.productId, body.historicalData);
  }

  @Post('inventory-prediction')
  @Roles('MANUFACTURER', 'ADMIN')
  async inventoryPrediction(@Body() body: { inventoryId: string; currentStock: number; depletionRate: number }) {
    return this.aiService.getInventoryPrediction(body.inventoryId, body.currentStock, body.depletionRate);
  }

  @Post('route-optimization')
  @Roles('LOGISTICS_PARTNER', 'ADMIN')
  async routeOptimization(@Body() body: { origin: string; destination: string; vehicleType: string }) {
    return this.aiService.getRouteOptimization(body.origin, body.destination, body.vehicleType);
  }
}
