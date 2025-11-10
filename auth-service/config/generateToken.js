import jwt from "jsonwebtoken";
import { redisClient } from "../index.js";

export const generateToken = async (id, res)=>{
    const accessToken = jwt.sign({ id },
        process.env.JWT_ACCESS_TOKEN_SECRET,
        { expiresIn: "5m" })
    
    const refreshToken = jwt.sign({ id },
        process.env.JWT_REFRESH_TOKEN_SECRET,
        {expiresIn: "7d"}
    )
    const refreshTokenKey = `refreshToken:${id}`
    await redisClient.setEx(refreshTokenKey, 60 * 60 * 24 * 7, refreshToken)
    res.cookie("accessToken", accessToken, {
        httpOnly: true,
        // secure: true,
        sameSite: "strict",
        maxAge: 1000 * 60 * 5
    })

    res.cookie("refreshToken", refreshToken, {
        httpOnly: true,
        // secure: true,
        sameSite: "none",
        maxAge: 1000 * 60 * 60 * 24 * 7
    })
    return { accessToken, refreshToken }
}