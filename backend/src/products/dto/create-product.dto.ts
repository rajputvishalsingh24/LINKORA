import { IsNotEmpty, IsString, IsNumber, IsOptional, Min } from 'class-validator';

export class CreateProductDto {
  @IsNotEmpty()
  @IsString()
  supplierId: string;

  @IsNotEmpty()
  @IsString()
  name: string;

  @IsNotEmpty()
  @IsString()
  category: string;

  @IsOptional()
  @IsString()
  description?: string;

  @IsNotEmpty()
  @IsNumber()
  @Min(0)
  price: number;

  @IsOptional()
  @IsString()
  currency?: string;

  @IsNotEmpty()
  @IsNumber()
  @Min(1)
  moq: number;

  @IsNotEmpty()
  @IsNumber()
  @Min(0)
  stockQuantity: number;

  @IsNotEmpty()
  @IsString()
  unit: string;

  @IsNotEmpty()
  @IsNumber()
  @Min(1)
  leadTimeDays: number;
}
