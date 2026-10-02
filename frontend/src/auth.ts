import NextAuth from "next-auth";
import CredentialsProvider from "next-auth/providers/credentials";

export const { handlers, signIn, signOut, auth } = NextAuth({
  providers: [
    CredentialsProvider({
      name: "Credentials",
      credentials: {
        email: { label: "Email", type: "email" },
        password: { label: "Password", type: "password" }
      },
      async authorize(credentials) {
        try {
          const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:4000/api/v1'}/auth/login`, {
            method: 'POST',
            body: JSON.stringify(credentials),
            headers: { "Content-Type": "application/json" }
          });

          const data = await res.json();

          if (res.ok && data?.user && data?.token) {
            return {
              ...data.user,
              accessToken: data.token,
            };
          }
          return null;
        } catch (error) {
          console.error("Auth error:", error);
          return null;
        }
      }
    })
  ],
  callbacks: {
    async jwt({ token, user }: any) {
      if (user) {
        token.accessToken = user.accessToken;
        token.role = user.role;
        token.id = user.id;
        token.companyId = user.companyId;
      }
      return token;
    },
    async session({ session, token }: any) {
      session.user.role = token.role as string;
      session.user.id = token.id as string;
      session.user.companyId = token.companyId as string;
      session.accessToken = token.accessToken as string;
      return session;
    }
  },
  pages: {
    signIn: '/login',
  },
  session: { strategy: "jwt" },
});
