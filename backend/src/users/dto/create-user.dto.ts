import { IsEmail, IsNotEmpty, IsString, IsEnum, MinLength, IsOptional } from 'class-validator';

export class CreateUserDto {
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
  @IsEnum(['MANUFACTURER', 'SUPPLIER', 'LOGISTICS_PARTNER', 'BUYER', 'ADMIN'])
  role: string;

  @IsOptional()
  @IsString()
  companyId?: string;

  @IsOptional()
  @IsString()
  phoneNumber?: string;
}
