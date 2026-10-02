import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateProductDto } from './dto/create-product.dto';
import { UpdateProductDto } from './dto/update-product.dto';

@Injectable()
export class ProductsService {
  constructor(private prisma: PrismaService) {}

  async create(createProductDto: CreateProductDto) {
    return this.prisma.product.create({
      data: {
        supplierId: createProductDto.supplierId,
        name: createProductDto.name,
        category: createProductDto.category,
        description: createProductDto.description,
        price: createProductDto.price,
        currency: createProductDto.currency || 'INR',
        moq: createProductDto.moq,
        stockQuantity: createProductDto.stockQuantity,
        unit: createProductDto.unit,
        leadTimeDays: createProductDto.leadTimeDays,
      },
    });
  }

  async findAll(category?: string, supplierId?: string) {
    return this.prisma.product.findMany({
      where: {
        isActive: true,
        ...(category && { category }),
        ...(supplierId && { supplierId }),
      },
      include: {
        supplier: {
          select: {
            id: true,
            companyName: true,
            city: true,
            rating: true,
            isVerified: true,
          },
        },
      },
      orderBy: { createdAt: 'desc' },
    });
  }

  async findOne(id: string) {
    const product = await this.prisma.product.findUnique({
      where: { id },
      include: {
        supplier: true,
      },
    });

    if (!product) {
      throw new NotFoundException(`Product with ID ${id} not found.`);
    }

    return product;
  }

  async update(id: string, updateProductDto: UpdateProductDto) {
    await this.findOne(id);
    return this.prisma.product.update({
      where: { id },
      data: {
        ...(updateProductDto.name && { name: updateProductDto.name }),
        ...(updateProductDto.category && { category: updateProductDto.category }),
        ...(updateProductDto.description && { description: updateProductDto.description }),
        ...(updateProductDto.price !== undefined && { price: updateProductDto.price }),
        ...(updateProductDto.moq !== undefined && { moq: updateProductDto.moq }),
        ...(updateProductDto.stockQuantity !== undefined && { stockQuantity: updateProductDto.stockQuantity }),
        ...(updateProductDto.leadTimeDays !== undefined && { leadTimeDays: updateProductDto.leadTimeDays }),
        ...(updateProductDto.isActive !== undefined && { isActive: updateProductDto.isActive }),
      },
    });
  }

  async remove(id: string) {
    await this.findOne(id);
    return this.prisma.product.update({
      where: { id },
      data: { isActive: false },
    });
  }
}
