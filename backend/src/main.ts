import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { ValidationPipe } from '@nestjs/common';
import { GlobalExceptionFilter } from './common/filters/global-exception.filter';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // Enable CORS for Next.js frontend
  app.enableCors({
    origin: process.env.FRONTEND_URL || 'http://localhost:3000',
    credentials: true,
  });

  // Global Pipelines and Filters
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true, // Strips out properties without decorators
      forbidNonWhitelisted: true, // Throws error if unknown properties are sent
      transform: true, // Automatically transforms payloads to DTO instances
    }),
  );
  app.useGlobalFilters(new GlobalExceptionFilter());

  // Set global API prefix (/api/v1)
  app.setGlobalPrefix('api/v1');

  const port = process.env.PORT || 4000;
  await app.listen(port);
  console.log(`🚀 Linkora Core Backend running at http://localhost:${port}/api/v1`);
}
bootstrap();
