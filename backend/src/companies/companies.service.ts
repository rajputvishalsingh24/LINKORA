import { Injectable, ConflictException, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateCompanyDto } from './dto/create-company.dto';
import { UpdateCompanyDto } from './dto/update-company.dto';

@Injectable()
export class CompaniesService {
  constructor(private prisma: PrismaService) {}

  async create(createCompanyDto: CreateCompanyDto) {
    const existing = await this.prisma.company.findUnique({
      where: { gstNumber: createCompanyDto.gstNumber },
    });

    if (existing) {
      throw new ConflictException('A company with this GST number is already registered.');
    }

    return this.prisma.company.create({
      data: {
        companyName: createCompanyDto.companyName,
        industry: createCompanyDto.industry as any,
        gstNumber: createCompanyDto.gstNumber,
        address: createCompanyDto.address,
        city: createCompanyDto.city,
        country: createCompanyDto.country || 'India',
        certifications: createCompanyDto.certifications || ['ISO 9001'],
      },
    });
  }

  async findAll(industry?: string) {
    return this.prisma.company.findMany({
      where: industry ? { industry: industry as any } : {},
      include: {
        _count: {
          select: { products: true, users: true, supplierOrders: true },
        },
      },
      orderBy: { rating: 'desc' },
    });
  }

  async findOne(id: string) {
    const company = await this.prisma.company.findUnique({
      where: { id },
      include: {
        products: true,
        users: { select: { id: true, name: true, role: true, email: true } },
      },
    });

    if (!company) {
      throw new NotFoundException(`Company with ID ${id} not found.`);
    }

    return company;
  }

  async update(id: string, updateCompanyDto: UpdateCompanyDto) {
    await this.findOne(id);
    return this.prisma.company.update({
      where: { id },
      data: {
        ...(updateCompanyDto.companyName && { companyName: updateCompanyDto.companyName }),
        ...(updateCompanyDto.industry && { industry: updateCompanyDto.industry as any }),
        ...(updateCompanyDto.address && { address: updateCompanyDto.address }),
        ...(updateCompanyDto.city && { city: updateCompanyDto.city }),
        ...(updateCompanyDto.isVerified !== undefined && { isVerified: updateCompanyDto.isVerified }),
        ...(updateCompanyDto.certifications && { certifications: updateCompanyDto.certifications }),
      },
    });
  }

  async remove(id: string) {
    await this.findOne(id);
    return this.prisma.company.delete({
      where: { id },
    });
  }
}
