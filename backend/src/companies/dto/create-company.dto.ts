import { IsNotEmpty, IsString, IsEnum, IsOptional, IsArray } from 'class-validator';

export enum IndustryEnum {
  MANUFACTURING = 'MANUFACTURING',
  AUTOMOTIVE = 'AUTOMOTIVE',
  ELECTRONICS = 'ELECTRONICS',
  STEEL = 'STEEL',
  FMCG = 'FMCG',
  TEXTILES = 'TEXTILES',
  OTHER = 'OTHER',
}

export class CreateCompanyDto {
  @IsNotEmpty()
  @IsString()
  companyName: string;

  @IsNotEmpty()
  @IsEnum(IndustryEnum)
  industry: IndustryEnum;

  @IsNotEmpty()
  @IsString()
  gstNumber: string;

  @IsNotEmpty()
  @IsString()
  address: string;

  @IsNotEmpty()
  @IsString()
  city: string;

  @IsOptional()
  @IsString()
  country?: string;

  @IsOptional()
  @IsArray()
  certifications?: string[];
}
