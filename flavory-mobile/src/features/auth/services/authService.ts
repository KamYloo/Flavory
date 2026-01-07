import Auth0, { Credentials } from "react-native-auth0";
import ENV from "../../../config/env";
import { clearAccessToken, setAccessToken } from "../../../api/client";
import { Auth0Credentials } from "../../../types";

const auth0 = new Auth0({
  domain: ENV.AUTH0_DOMAIN,
  clientId: ENV.AUTH0_CLIENT_ID,
});

class AuthService {
  async login(): Promise<Auth0Credentials> {
    try {
      const credentials: Credentials = await auth0.webAuth.authorize({
        scope: "openid profile email offline_access",
        audience: ENV.AUTH0_AUDIENCE,
      });

      setAccessToken(credentials.accessToken);

      return {
        accessToken: credentials.accessToken,
        idToken: credentials.idToken,
        expiresAt: credentials.expiresAt,
        refreshToken: credentials.refreshToken,
      };
    } catch (error) {
      console.error("Login error:", error);
      throw new Error("Failed to login. Please try again.");
    }
  }

  async logout(): Promise<void> {
    try {
      await auth0.webAuth.clearSession();
      clearAccessToken();
    } catch (error) {
      console.error("Logout error:", error);
      clearAccessToken();
      throw new Error("Failed to logout completely.");
    }
  }

  async refreshToken(refreshToken: string): Promise<Auth0Credentials> {
    try {
      const credentials: Credentials = await auth0.auth.refreshToken({
        refreshToken,
      });

      setAccessToken(credentials.accessToken);

      return {
        accessToken: credentials.accessToken,
        idToken: credentials.idToken,
        expiresAt: credentials.expiresAt,
        refreshToken: credentials.refreshToken,
      };
    } catch (error) {
      console.error("Token refresh error:", error);
      throw new Error("Failed to refresh token. Please login again.");
    }
  }

  async getUserInfo(accessToken: string): Promise<any> {
    try {
      const userInfo = await auth0.auth.userInfo({ token: accessToken });
      return userInfo;
    } catch (error) {
      console.error("Get user info error:", error);
      throw new Error("Failed to get user information.");
    }
  }

  isTokenValid(expiresAt: number): boolean {
    const currentTime = Date.now();
    const bufferTime = 5 * 60 * 1000; // 5 minut bufforu
    return expiresAt > currentTime + bufferTime;
  }
}

export default new AuthService();
