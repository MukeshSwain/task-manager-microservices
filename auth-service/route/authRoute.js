import express from "express"
import { login, signup, verifyOtp, verifyUser } from "../controller/authController.js"

const router = express.Router()
router.post("/signup", signup)
router.post("/login", login)
router.get("/verify/:token", verifyUser)
router.get("/verify-otp",verifyOtp)

export default router