import express from "express"
import { login, logout, myProfile, onInviteSignup, refreshToken, signup, verifyOtp, verifyUser } from "../controller/authController.js"
import { isAuth } from "../middleware/isAuth.js"


const router = express.Router()
router.post("/signup", signup)
router.post("/login", login)
router.post("/verify/:token", verifyUser)
router.post("/verify-otp", verifyOtp)
router.get("/my-profile", isAuth, myProfile)
router.post("/logout",logout)
router.post("/refresh", refreshToken)
router.post("/signup/invite", onInviteSignup)
export default router