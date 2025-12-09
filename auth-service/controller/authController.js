import bcrypt from "bcrypt";
import prisma from "../config/prismaClient.js";
import axios from "axios";
import { loginSchema, signupSchema } from "../config/zod.js";
import { redisClient } from "../index.js";
import sendMail from "../config/sendMail.js";
import crypto from "crypto";
import { getOtpHtml, getVerifyEmailHtml } from "../config/html.js";
import {
  generateAccesToken,
  generateToken,
  verifyResfreshToken,
} from "../config/generateToken.js";

export const signup = async (req, res) => {
  try {
    const validation = signupSchema.safeParse(req.body);
    if (!validation.success) {
      const zodError = validation.error;
      let firstErrorMessage = "Validation Error";
      let allErrors = [];

      if (zodError?.issues && Array.isArray(zodError.issues)) {
        allErrors = zodError.issues.map((issue) => ({
          field: issue.path ? issue.path.join() : "Unknown",
          message: issue.message || "Validation error",
          code: issue.code,
        }));
      }
      firstErrorMessage = allErrors[0]?.message || "Validation Error";

      return res.status(400).json({
        message: firstErrorMessage,
        errors: allErrors,
      });
    }

    const { name, email, password, role } = validation.data;
    //Redis rate limit
    const rateLimitKey = `register-rate-limit:${req.ip}:${email}`;
    if (await redisClient.get(rateLimitKey)) {
      return res
        .status(429)
        .json({ message: "Too many requests, please try again later" });
    }
    const existUser = await prisma.auth_User.findUnique({
      where: {
        email: email,
      },
    });

    if (!name || !email || !password) {
      return res.status(400).json({ message: "All fields are required" });
    }
    if (existUser) {
      return res.status(400).json({ message: "User already exists" });
    }

    const passwordHash = await bcrypt.hash(password, 10);
    //Token generation for verification
    const verifyToken = crypto.randomBytes(32).toString("hex");
    const verifyKey = `verify:${verifyToken}`;
    const dataStore = JSON.stringify({
      name,
      email,
      password: passwordHash,
      role,
    });
    await redisClient.set(verifyKey, dataStore, { EX: 300 });

    const subject = "Verify your email for Account creation";
    const html = getVerifyEmailHtml({ email, token: verifyToken });
    await sendMail({ email, subject, html });
    await redisClient.set(rateLimitKey, "true", { EX: 60 });

    res.json({
      message:
        "If your email is valid, a verification link has been sent. It will expire in 5 minutes",
      success: true,
    });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Internal server error" });
  }
};

export const verifyUser = async (req, res) => {
  try {
    const { token } = req.params;

    if (!token) {
      return res.status(400).json({ message: "Token is required" });
    }

    const verifyKey = `verify:${token}`;
    const userDataJson = await redisClient.get(verifyKey);

    if (!userDataJson) {
      return res
        .status(400)
        .json({ message: "Verification link is invalid or expired." });
    }

    // Delete verification key (to prevent reuse)
    await redisClient.del(verifyKey);

    const userData = JSON.parse(userDataJson);

    // Double check — avoid duplicate users
    const existingUser = await prisma.auth_User.findUnique({
      where: { email: userData.email },
    });
    if (existingUser) {
      return res.status(400).json({ message: "User already exists." });
    }

    // ✅ Create user in Auth DB
    const newUser = await prisma.auth_User.create({
      data: {
        email: userData.email,
        passwordHash: userData.password,
        role: userData.role.toUpperCase(),
        isVerified: true,
      },
    });

    // ✅ Then create user in User Service
    let response;
    try {
      response = await axios.post("http://localhost:8082/api/users", {
        id: newUser.id,
        name: userData.name,
        email: newUser.email,
        role: newUser.role,
        isEmailVerified: newUser.isVerified,
      });
    } catch (err) {
      console.error("UserService error:", err.response?.data || err.message);
      // Rollback user creation in Auth DB
      await prisma.auth_User.delete({ where: { id: newUser.id } });
      return res
        .status(500)
        .json({ message: "Failed to create user in User Service." });
    }

    // ✅ Success
    res.status(200).json({
      message: "Email verified successfully. Account created.",
      userServiceResponse: response.data,
      success: true,
    });
  } catch (error) {
    console.error("verifyUser error:", error);
    return res.status(500).json({ message: "Internal server error" });
  }
};

export const login = async (req, res) => {
  try {
    const validation = loginSchema.safeParse(req.body);
    if (!validation.success) {
      const zodError = validation.error;
      let firstErrorMessage = "Validation Error";
      let allError = [];
      if (zodError.issues && Array.isArray(zodError.issues)) {
        allError = zodError.issues.map((issue) => ({
          path: issue?.path ? issue.path.join() : "Unknown",
          message: issue.message,
          code: issue.code,
        }));
      }
      firstErrorMessage = allError[0]?.message || "Validation Error";
      return res
        .status(400)
        .json({ message: firstErrorMessage, errors: allError });
    }

    const { email, password } = validation.data;
    if (!email || !password) {
      return res.status(400).json({ message: "All fields are required!" });
    }
    const rateLimitKey = `login-rate-limit:${req.ip}:${email}`;
    if (await redisClient.get(rateLimitKey)) {
      return res
        .status(429)
        .json({ message: "Too many requests. Please try again later." });
    }

    const user = await prisma.auth_User.findUnique({
      where: {
        email: email,
      },
    });
    if (!user) {
      return res.status(400).json({ message: "Invalid credentials!" });
    }

    const isMatch = await bcrypt.compare(password, user.passwordHash);
    if (!isMatch) {
      return res.status(400).json({ message: "Invalid credentials!" });
    }

    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    const otpKey = `otp:${email}`;
    await redisClient.set(otpKey, JSON.stringify(otp), { EX: 300 });
    const subject = "OTP for verification";
    const html = getOtpHtml({ email, otp: otp });
    await sendMail({ email, subject, html });
    await redisClient.set(rateLimitKey, "true", { EX: 60 });

    res.json({
      message:
        "If your email is valid, an OTP has been sent to your email address. Please enter the OTP to verify your account.It will expire in 5 minutes.",
      success: true,
    });
  } catch (error) {
    console.error("login error:", error);
    return res.status(500).json({ message: "Internal server error" });
  }
};

export const verifyOtp = async (req, res) => {
  try {
    const { email, otp } = req.body;
    if (!email || !otp) {
      return res.status(400).json({ message: "All fields are required!" });
    }
    const otpKey = `otp:${email}`;
    const storedOtpString = await redisClient.get(otpKey);
    if (!storedOtpString) {
      return res.status(400).json({ message: "OTP expired!" });
    }
    const storedOtp = JSON.parse(storedOtpString);
    if (storedOtp !== otp) {
      return res.status(400).json({ message: "Invalid OTP!" });
    }
    await redisClient.del(otpKey);
    const user = await prisma.auth_User.findUnique({
      where: {
        email: email,
      },
    });
    if (!user) {
      return res.status(400).json({ message: "User not found!" });
    }
    const tokenData = await generateToken(user.id, res);

    return res.status(200).json({
      message: `Welcome ${user.email}`,
      id: user.id,
      success: true,
    });
  } catch (error) {
    console.error("verifyOtp error:", error);
    return res.status(500).json({ message: "Internal server error" });
  }
};

export const myProfile = async (req, res) => {
  try {
    const user = req.user;
    return res.status(200).json({ user });
  } catch (error) {
    res.status(500).json({ message: "Internal server error" });
  }
};

export const logout = async (req, res) => {
  try {
    const refreshToken = req.cookies?.refreshToken;

    if (!refreshToken) {
      return res.status(400).json({ message: "Refresh token missing" });
    }

    // Verify refresh token
    let decoded;
    try {
      decoded = await verifyResfreshToken(refreshToken);
    } catch (err) {
      // Invalid or expired token — still clear cookies for safety
      res.clearCookie("accessToken");
      res.clearCookie("refreshToken");
      return res.status(401).json({ message: "Invalid or expired token" });
    }
    // Remove refresh token and any cached data
    await Promise.all([
      redisClient.del(`refreshToken:${decoded.id}`),
      redisClient.del(`user:${decoded.id}`),
    ]);

    // Clear cookies
    res.clearCookie("accessToken", {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "strict",
    });
    res.clearCookie("refreshToken", {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "strict",
    });

    return res.status(200).json({
      message: "Logged out successfully",
      success: true,
    });
  } catch (error) {
    console.error("Logout error:", error);
    return res.status(500).json({ message: "Internal server error" });
  }
};

export const refreshToken = async (req, res) => {
  try {
    const refreshToken = req.cookies?.refreshToken;
    if (!refreshToken) {
      return res.status(400).json({ message: "Invalid refresh token" });
    }

    const decoded = await verifyResfreshToken(refreshToken);
    if (!decoded) {
      return res.status(400).json({ message: "Invalid refresh token" });
    }
    await generateAccesToken(decoded.id, res);
    return res.status(200).json({ message: "Token refreshed successfully" });
  } catch (error) {
    console.error("Refresh token error:", error);
    return res.status(500).json({ message: "Internal server error" });
  }
};

export const onInviteSignup = async (req, res) => {
  try {
    const validateData = signupSchema.safeParse(req.body);
    if (!validateData.success) {
      const zodError = validateData.error;
      let firstErrorMessage = "Validation Error";
      let allErrors = [];

      if (zodError?.issues && Array.isArray(zodError.issues)) {
        allErrors = zodError.issues.map((issue) => ({
          field: issue.path ? issue.path.join() : "Unknown",
          message: issue.message || "Validation error",
          code: issue.code,
        }));
      }
      firstErrorMessage = allErrors[0]?.message || "Validation Error";

      return res.status(400).json({
        message: firstErrorMessage,
        errors: allErrors,
      });
    }
    const { name, email, password, role } = validateData.data;
    if (!name || !email || !password || !role) {
      return res.status(400).json({ message: "All fields are required" });
    }
    const existUser = await prisma.auth_User.findUnique({
      where: {
        email: email,
      },
    });
    if (existUser) {
      return res.status(400).json({ message: "User already exists" });
    }
    const passwordHash = await bcrypt.hash(password, 10);

    const newUser = await prisma.auth_User.create({
      data: {
        email,
        passwordHash,
        role: role.toUpperCase(),
        isVerified: true,
      },
    });
    if (!newUser) {
      return res.status(500).json({ message: "Failed to create user" });
    }
    try {
      let response = await axios.post("http://localhost:8082/api/users", {
        id: newUser.id,
        name: name,
        email: newUser.email,
        role: newUser.role,
        isEmailVerified: newUser.isVerified,
      });
    } catch (err) {
      console.error("UserService error:", err.response?.data || err.message);
      // Rollback user creation in Auth DB
      await prisma.auth_User.delete({ where: { id: newUser.id } });
      return res
        .status(500)
        .json({ message: "Failed to create user in User Service." });
    }

    return res.status(201).json({
      message: "User created successfully",
      success: true,
      authId: newUser.id,
    });
  } catch (error) {
    console.error("onInviteSignup error:", error);
    return res.status(500).json({ message: "Internal server error" });
  }
};
