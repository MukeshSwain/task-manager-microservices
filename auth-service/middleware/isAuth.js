import jwt from "jsonwebtoken"
import { redisClient } from "../index.js";
import prisma from "../config/prismaClient.js";
export const isAuth = async (req, res, next) => {
    try {
        const token = req.cookies.accessToken;
        console.log(token);
        
        if (!token) {
            return res.status(401).json({ message: "Unauthorized" });
        }

        const decodeData = jwt.verify(
          token,
          process.env.JWT_ACCESS_TOKEN_SECRET
        );
        if (!decodeData) {
            return res.status(401).json({ message: "Unauthorized" });
        }
        const cacheUser = await redisClient.get(`user:${decodeData.id}`)
        if (cacheUser) {
            req.user = JSON.parse(cacheUser)
            next()
        }

        const user = await prisma.auth_User.findUnique({
          where: { id: decodeData.id },
          select: {
            id: true,
           
            email: true,
            role: true,
            
            
          },
        });
        if (!user) {
            return res.status(401).json({ message: "Unauthorized" });
        }
        await redisClient.setEx(`user:${user.id}`,3600,JSON.stringify(user))
        req.user = user
        next()
    } catch (error) {
        res.status(500).json({ message: "Internal Server Error ln" });
    }
}