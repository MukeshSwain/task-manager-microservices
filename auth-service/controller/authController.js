import bcrypt from "bcrypt"
import prisma from "../config/prismaClient.js"
import axios from "axios";
import { signupSchema } from "../config/zod.js";
import {redisClient} from "../index.js"
import sendMail from "../config/sendMail.js";
import crypto from "crypto"
import { getVerifyEmailHtml } from "../config/html.js";

export const signup = async (req, res) => {
    try {
        
        const validation = signupSchema.safeParse(req.body);
        if (!validation.success) {
            const zodError = validation.error;
            let firstErrorMessage = "Validation Error"
            let allErrors = []

            if (zodError?.issues && Array.isArray(zodError.issues)) {
                allErrors = zodError.issues.map((issue) => ({
                    field: issue.path ? issue.path.join():"Unknown",
                    message: issue.message || "Validation error",
                    code: issue.code
                        
                }))
                
            }
            firstErrorMessage = allErrors[0]?.message || "Validation Error";

            return res.status(400).json({
                message: firstErrorMessage,
                errors: allErrors
            });


          
        }

        const { name, email, password, role } = validation.data;
        //Redis rate limit
        const rateLimitKey = `register-rate-limit:${req.ip}:${email}` 
        if (await redisClient.get(rateLimitKey)) {
            return res.status(429).json({ message: "Too many requests, please try again later" });
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
        })
        await redisClient.set(verifyKey, dataStore, { EX: 300 })
        
        const subject = "Verify your email for Account creation"
        const html = getVerifyEmailHtml({email,verifyToken})
        await sendMail({ email, subject, html })
        await redisClient.set(rateLimitKey, "true", { EX: 60 })
        
        res.json({
            message:"If your email is valid, a verification link has been sent. It will expire in 5 minutes"
        })

    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: "Internal server error" });
    }
}

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
        isVerified: true
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
    return res.status(200).json({
      message: "Email verified successfully. Account created.",
      userServiceResponse: response.data,
    });
  } catch (error) {
    console.error("verifyUser error:", error);
    return res.status(500).json({ message: "Internal server error" });
  }
};


export const login = async (req, res) => {
    const { email, password } = req.body;
    try {
        if (!email || !password) {
            return res.status(400).json({message:"All fields are required!"})
        }

        const user = await prisma.auth_User.findUnique({
            where: {
                email:email
            }
        })
        if (!user) {
            return res.status(400).json({message :"User not found!"})
        }

        const isMatch = await bcrypt.compare(password, user.passwordHash);
        if (!isMatch) {
            return res.status(400).json({ message: "User not found!" });
        }

        
        res.status(200).json({
            message: "login successfull",
            
        })
    } catch (error) {
        
    }
}