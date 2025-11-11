import express from "express"
import { login, logout, myProfile, refreshToken, signup, verifyOtp, verifyUser } from "../controller/authController.js"
import { isAuth } from "../middleware/isAuth.js"

const router = express.Router()
router.post("/signup", signup)
router.post("/login", login)
router.get("/verify/:token", verifyUser)
router.get("/verify-otp", verifyOtp)
router.get("/my-profile", isAuth, myProfile)
router.get("/logout", logout)
router.get("/refresh",refreshToken)
export default router