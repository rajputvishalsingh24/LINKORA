import { IsString, IsOptional, IsEnum, IsArray, IsBoolean } from 'class-validator';
import { IndustryEnum } from './create-company.dto';

export class UpdateCompanyDto {
  @IsOptional()
  @IsString()
  companyName?: string;

  @IsOptional()
  @IsEnum(IndustryEnum)
  industry?: IndustryEnum;

  @IsOptional()
  @IsString()
  address?: string;

  @IsOptional()
  @IsString()
  city?: string;

  @IsOptional()
  @IsBoolean()
  isVerified?: boolean;

  @IsOptional()
  @IsArray()
  certifications?: string[];
}
