import express from "express"
import cors from "cors"
import dotenv from "dotenv"
import cookieParser from "cookie-parser"
import { createClient } from "redis"

import authRoute from "./route/authRoute.js"
dotenv.config()


const port = process.env.PORT || 3000
const redisUrl = process.env.REDIS_URL
if (!redisUrl) {
    console.log("REDIS_URL is not defined")
    process.exit(1)
}

export const redisClient = createClient({
    url: redisUrl
})

redisClient
    .connect()
    .then(() => console.log("Redis client connected"))
    .catch((err) => console.log(err))
const app = express()
app.use(cors())
app.use(cookieParser())
app.use(express.json())

app.use("/api/auth", authRoute)

app.listen(port, () => {
    console.log(`Server is running on port ${port}`)
})