import { IsEmail, IsNotEmpty, IsString, IsEnum, MinLength, IsOptional } from 'class-validator';

export enum UserRoleEnum {
  MANUFACTURER = 'MANUFACTURER',
  SUPPLIER = 'SUPPLIER',
  LOGISTICS_PARTNER = 'LOGISTICS_PARTNER',
  BUYER = 'BUYER',
  ADMIN = 'ADMIN',
}

export class RegisterDto {
  @IsNotEmpty()
  @IsString()
  name: string;

  @IsNotEmpty()
  @IsEmail()
  email: string;

  @IsNotEmpty()
  @MinLength(8)
  password: string;

  @IsNotEmpty()
  @IsEnum(UserRoleEnum)
  role: UserRoleEnum;

  @IsOptional()
  @IsString()
  companyName?: string;

  @IsOptional()
  @IsString()
  gstNumber?: string;
}
