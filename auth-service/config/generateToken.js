import jwt from "jsonwebtoken";
import { redisClient } from "../index.js";

export const generateToken = async (id, res)=>{
    const accessToken = jwt.sign({ id },
        process.env.JWT_ACCESS_TOKEN_SECRET,
        { expiresIn: "2m" })
    
    const refreshToken = jwt.sign({ id },
        process.env.JWT_REFRESH_TOKEN_SECRET,
        {expiresIn: "7d"}
    )
    const refreshTokenKey = `refreshToken:${id}`
    await redisClient.setEx(refreshTokenKey, 60 * 60 * 24 * 7, refreshToken)
    res.cookie("accessToken", accessToken, {
        httpOnly: true,
        secure: process.env.NODE_ENV === "production",
        sameSite: process.env.NODE_ENV === "production" ? "strict" : "lax",
        maxAge: 1000 * 60 * 2
    })

    res.cookie("refreshToken", refreshToken, {
        httpOnly: true,
        secure: process.env.NODE_ENV === "production",
        sameSite: process.env.NODE_ENV === "production" ? "strict" : "lax",
        maxAge: 1000 * 60 * 60 * 24 * 7
    })
    return { accessToken, refreshToken }
}


export const verifyResfreshToken = async (refreshToken) => {
    try {
        const decode = jwt.verify(refreshToken, process.env.JWT_REFRESH_TOKEN_SECRET)
        const storedToken = await redisClient.get(`refreshToken:${decode.id}`)
        if (refreshToken === storedToken) {
            return decode;
        }
        return null;
    } catch (error) {
        console.error("Error verifying refresh token:", error);
        return null;
    }
}

export const generateAccesToken = async(id, res) => {
    try {
        const accessToken = jwt.sign({ id }, process.env.JWT_ACCESS_TOKEN_SECRET, { expiresIn: "2m" })
        res.cookie("accessToken", accessToken, {
          httpOnly: true,
          secure: process.env.NODE_ENV === "production",
          sameSite: process.env.NODE_ENV === "production" ? "strict" : "lax",
          maxAge: 1000 * 60 * 2,
        });
    } catch (error) {
        console.error("Error generating access token:", error);
    }
}