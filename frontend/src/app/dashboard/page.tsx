"use client";

import { signOut, useSession } from "next-auth/react";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import apiClient from "@/lib/api-client";
import type { UserProfileResponse } from "@/types/auth";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export default function DashboardPage() {
  const { data: session, status } = useSession();
  const router = useRouter();

  const {
    data: profile,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["profile", session?.user?.uuid],
    queryFn: async () => {
      const token = session?.user?.token;
      if (!token) {
        throw new Error("Not authenticated");
      }
      const { data } = await apiClient.get<UserProfileResponse>("/api/users/me", {
        headers: { Authorization: `Bearer ${token}` },
      });
      return data;
    },
    enabled: status === "authenticated" && !!session?.user?.token,
  });

  const handleSignOut = async () => {
    await signOut({ redirect: false });
    router.replace("/login");
  };

  if (status === "loading") {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
        <p className="text-muted-foreground">Loading...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
      <div className="mx-auto max-w-2xl space-y-6 py-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold">Dashboard</h1>
            <p className="text-muted-foreground">
              Welcome back{session?.user?.username ? `, ${session.user.username}` : ""}
            </p>
          </div>
          <Button variant="outline" onClick={handleSignOut}>
            Sign out
          </Button>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Your profile</CardTitle>
            <CardDescription>Account details from the API</CardDescription>
          </CardHeader>
          <CardContent>
            {status === "loading" || isLoading ? (
              <p className="text-sm text-muted-foreground">Loading profile...</p>
            ) : error ? (
              <p className="text-sm text-destructive">
                {error instanceof Error && error.message === "Network Error"
                  ? "Could not reach the API. Restart the backend after CORS changes, then sign in again."
                  : "Failed to load profile. Sign out and sign in again if the problem persists."}
              </p>
            ) : profile ? (
              <dl className="space-y-3 text-sm">
                <div className="flex justify-between gap-4 border-b pb-2">
                  <dt className="font-medium text-muted-foreground">Username</dt>
                  <dd>{profile.username}</dd>
                </div>
                <div className="flex justify-between gap-4 border-b pb-2">
                  <dt className="font-medium text-muted-foreground">Email</dt>
                  <dd>{profile.email}</dd>
                </div>
                <div className="flex justify-between gap-4 border-b pb-2">
                  <dt className="font-medium text-muted-foreground">UUID</dt>
                  <dd className="font-mono text-xs">{profile.uuid}</dd>
                </div>
                <div className="flex justify-between gap-4 border-b pb-2">
                  <dt className="font-medium text-muted-foreground">Roles</dt>
                  <dd>{profile.roles.join(", ") || "—"}</dd>
                </div>
                <div className="flex justify-between gap-4">
                  <dt className="font-medium text-muted-foreground">Member since</dt>
                  <dd>{new Date(profile.createdAt).toLocaleString()}</dd>
                </div>
              </dl>
            ) : null}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
