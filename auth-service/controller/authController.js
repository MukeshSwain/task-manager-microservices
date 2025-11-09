import bcrypt from "bcrypt"
import prisma from "../config/prismaClient.js"
import axios from "axios";
import { signupSchema } from "../config/zod.js";
import { is } from "zod/locales";

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
        const existUser = await prisma.auth_User.findUnique({
          where: {
            email: email,
          },
        });

        if (existUser) {
          return res.status(400).json({ message: "User already exists" });
        }
        if (!name || !email || !password) {
            return res.status(400).json({ message: "All fields are required" });
        }

        const passwordHash = await bcrypt.hash(password, 10);

        const user = await prisma.auth_User.create({
            data: {
                email: email,
                passwordHash: passwordHash,
                role: role.toUpperCase()
                
            }
        })

        if(!user) {
            return res.status(400).json({ message: "User not created" });
        }
        const response = await axios.post("http://localhost:8082/api/users", {
            email: email,
            name: name,
            role: role,
            id:user.id
        })

        
        return res.status(200).json({ message: response.data });
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: "Internal server error" });
    }
}

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