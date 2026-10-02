import { IsString, IsOptional, IsEnum } from 'class-validator';

export class UpdateUserDto {
  @IsOptional()
  @IsString()
  name?: string;

  @IsOptional()
  @IsEnum(['MANUFACTURER', 'SUPPLIER', 'LOGISTICS_PARTNER', 'BUYER', 'ADMIN'])
  role?: string;

  @IsOptional()
  @IsString()
  phoneNumber?: string;

  @IsOptional()
  @IsString()
  companyId?: string;
}
