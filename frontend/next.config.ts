import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async redirects() {
    return [
      {
        source: "/index.html",
        destination: "/reset-password",
        permanent: false,
      },
    ];
  },
};

export default nextConfig;


