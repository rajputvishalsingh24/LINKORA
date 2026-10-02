import { Injectable, UnauthorizedException, ConflictException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { PrismaService } from '../prisma/prisma.service';
import { RegisterDto } from './dto/register.dto';
import { LoginDto } from './dto/login.dto';
import * as bcrypt from 'bcrypt';

@Injectable()
export class AuthService {
  constructor(
    private prisma: PrismaService,
    private jwtService: JwtService,
  ) {}

  async register(registerDto: RegisterDto) {
    const existing = await this.prisma.user.findUnique({
      where: { email: registerDto.email },
    });

    if (existing) {
      throw new ConflictException('An account with this email already exists.');
    }

    const passwordHash = await bcrypt.hash(registerDto.password, 10);

    let companyId: string | undefined = undefined;
    if (registerDto.companyName && registerDto.gstNumber) {
      const company = await this.prisma.company.create({
        data: {
          companyName: registerDto.companyName,
          gstNumber: registerDto.gstNumber,
          industry: 'MANUFACTURING',
          address: 'Industrial Zone',
          city: 'Mumbai',
        },
      });
      companyId = company.id;
    }

    const user = await this.prisma.user.create({
      data: {
        name: registerDto.name,
        email: registerDto.email,
        passwordHash,
        role: registerDto.role as any,
        companyId,
      },
      select: {
        id: true,
        email: true,
        name: true,
        role: true,
        companyId: true,
      },
    });

    const token = this.generateToken(user);
    return { user, token };
  }

  async login(loginDto: LoginDto) {
    const user = await this.prisma.user.findUnique({
      where: { email: loginDto.email },
      include: { company: true },
    });

    if (!user) {
      throw new UnauthorizedException('Invalid email or password credentials.');
    }

    const isMatch = await bcrypt.compare(loginDto.password, user.passwordHash);
    if (!isMatch) {
      throw new UnauthorizedException('Invalid email or password credentials.');
    }

    const sanitizedUser = {
      id: user.id,
      email: user.email,
      name: user.name,
      role: user.role,
      companyId: user.companyId,
      company: user.company,
    };

    const token = this.generateToken(sanitizedUser);
    return { user: sanitizedUser, token };
  }

  private generateToken(user: { id: string; email: string; role: string; companyId?: string | null }) {
    const payload = {
      sub: user.id,
      email: user.email,
      role: user.role,
      companyId: user.companyId ?? undefined,
    };
    return this.jwtService.sign(payload);
  }
}
