import 'dotenv/config';
import { PrismaClient, AdminRole } from '../src/generated/prisma/client';
import { PrismaPg } from '@prisma/adapter-pg';
import bcrypt from 'bcrypt';

const adapter = new PrismaPg({ connectionString: process.env.DATABASE_URL! });
const prisma = new PrismaClient({ adapter });

async function main() {
  const passwordHash = await bcrypt.hash('Admin@123', 12);

  const admin = await prisma.admin.upsert({
    where: { email: 'admin@rptsd.com' },
    update: {},
    create: {
      email: 'admin@rptsd.com',
      passwordHash,
      name: 'Super Admin',
      role: AdminRole.SUPER_ADMIN,
    },
  });

  console.log('Seeded admin:', admin.email);
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
