export const ENV = {
  API_URL: process.env.EXPO_PUBLIC_API_URL,
  AUTH0_DOMAIN: process.env.EXPO_PUBLIC_AUTH0_DOMAIN || "",
  AUTH0_CLIENT_ID: process.env.EXPO_PUBLIC_AUTH0_CLIENT_ID || "",
  AUTH0_AUDIENCE: process.env.EXPO_PUBLIC_AUTH0_AUDIENCE || "",
} as const;

export const validateEnv = () => {
  if (!ENV.API_URL) throw new Error("Brakuje EXPO_PUBLIC_API_URL w .env");
  if (!ENV.AUTH0_DOMAIN)
    throw new Error("Brakuje EXPO_PUBLIC_AUTH0_DOMAIN w .env");
  if (!ENV.AUTH0_CLIENT_ID)
    throw new Error("Brakuje EXPO_PUBLIC_AUTH0_CLIENT_ID w .env");
};

export default ENV;
