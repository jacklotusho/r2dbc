import { auth } from "@/lib/auth";
import { NextResponse } from "next/server";

export default auth((req) => {
  const isLoggedIn = !!req.auth;
  const { pathname, searchParams } = req.nextUrl;

  // Legacy password-reset links from emails (before /reset-password route)
  if (pathname === "/index.html") {
    const url = new URL("/reset-password", req.url);
    const token = searchParams.get("token");
    if (token) {
      url.searchParams.set("token", token);
    }
    return NextResponse.redirect(url);
  }

  if (pathname.startsWith("/dashboard") && !isLoggedIn) {
    return NextResponse.redirect(new URL("/login", req.url));
  }

  if ((pathname === "/login" || pathname === "/register") && isLoggedIn) {
    return NextResponse.redirect(new URL("/dashboard", req.url));
  }
});

export const config = {
  matcher: ["/dashboard/:path*", "/login", "/register", "/index.html"],
};
