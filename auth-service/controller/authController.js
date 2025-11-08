import bcrypt from "bcrypt"
import prisma from "../config/prismaClient.js"
import axios from "axios";

export const signup = async (req, res) => {
    const { name, email, password, role } = req.body;
    
    
    const existUser =await prisma.auth_User.findUnique({
        where: {
            email: email
        }
    })
    
    if(existUser) {
        return res.status(400).json({ message: "User already exists" });
    }

    try {
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